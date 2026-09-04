package com.vkbot.business.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vkbot.business.model.Vacancy;
import com.vkbot.exception.ApiException;
import com.vkbot.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class VacancyAggregatorApi {

    private static final String BASE_URL = "https://opendata.trudvsem.ru/api/v1/vacancies";
    private static final int DEFAULT_LIMIT = 100;
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    public List<Vacancy> searchVacancies(String regionCode, String keyword, Integer experienceFrom,
                                         Long minSalary, int offset, int limit) throws ApiException {
        try {
            String url = buildUrl(regionCode, keyword, experienceFrom, offset, limit);
            log.info("Fetching vacancies from: {}", url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ApiException("API returned HTTP status code: " + response.statusCode());
            }

            List<Vacancy> vacancies = parseVacancies(response.body(), minSalary);

            // Фильтрация по опыту уже после получения данных (т.к. в API нет такого параметра)
            if (experienceFrom != null && experienceFrom > 0) {
                List<Vacancy> filtered = new ArrayList<>();
                for (Vacancy v : vacancies) {
                    Long exp = v.getExperienceFrom();
                    if (exp != null && exp >= experienceFrom) {
                        filtered.add(v);
                    }
                }
                return filtered;
            }

            return vacancies;
        } catch (Exception e) {
            log.error("Error fetching vacancies: {}", e.getMessage(), e);
            throw new ApiException("Failed to fetch vacancies: " + e.getMessage(), e);
        }
    }

    public List<Vacancy> searchAllVacancies(String regionCode, String keyword, Integer experienceFrom) throws ApiException {
        List<Vacancy> allVacancies = new ArrayList<>();
        int offset = 0;
        Integer total = null;

        while (true) {
            try {
                String url = buildUrl(regionCode, keyword, experienceFrom, offset, DEFAULT_LIMIT);
                log.info("Fetching batch: {}", url);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(java.net.URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    log.warn("HTTP status {} received, stopping pagination.", response.statusCode());
                    break;
                }

                String body = response.body();
                JsonNode root = objectMapper.readTree(body);

                // Проверка внутреннего статуса API
                String apiStatus = root.path("status").asText();
                if (!"200".equals(apiStatus)) {
                    String errorMsg = root.path("meta").path("error").asText("Unknown API error");
                    throw new ApiException("API internal status: " + apiStatus + ", Error: " + errorMsg);
                }

                if (total == null) {
                    JsonNode metaNode = root.path("meta");
                    if (metaNode.has("total")) {
                        total = metaNode.get("total").asInt();
                        log.info("Total vacancies found: {}", total);
                    } else {
                        total = 10_000; // безопасный лимит
                        log.warn("Total not found in meta, using safe limit of {}", total);
                    }
                }

                if (offset >= total) {
                    log.info("Reached end of pagination (offset {} >= total {}).", offset, total);
                    break;
                }

                List<Vacancy> batch = parseVacancies(body, null);

                if (batch.isEmpty()) {
                    log.info("No more vacancies returned, stopping.");
                    break;
                }

                allVacancies.addAll(batch);

                if (batch.size() < DEFAULT_LIMIT) {
                    log.info("Batch size {} < limit {}, assuming last page.", batch.size(), DEFAULT_LIMIT);
                    break;
                }

                offset += DEFAULT_LIMIT;

            } catch (ApiException e) {
                throw e;
            } catch (Exception e) {
                log.error("Error fetching batch at offset {}: {}", offset, e.getMessage());
                break;
            }
        }

        // Фильтрация по опыту после сбора всех страниц
        if (experienceFrom != null && experienceFrom > 0) {
            List<Vacancy> filtered = new ArrayList<>();
            for (Vacancy v : allVacancies) {
                Long exp = v.getExperienceFrom();
                if (exp != null && exp >= experienceFrom) {
                    filtered.add(v);
                }
            }
            return filtered;
        }

        return allVacancies;
    }

    // Package-private (не private) — чтобы тесты могли проверить построение URL и
    // разбор JSON напрямую, без реального HTTP-запроса к агрегатору.
    String buildUrl(String regionCode, String keyword, Integer experienceFrom, int offset, int limit) {
        StringBuilder url = new StringBuilder(BASE_URL);

        if (regionCode != null && !regionCode.isBlank()) {
            url.append("/region/").append(regionCode);
        }

        url.append("?offset=").append(offset);
        url.append("&limit=").append(limit);

        if (experienceFrom != null && experienceFrom > 0) {
            url.append("&experienceFrom=").append(experienceFrom);
        }

        if (keyword != null && !keyword.isBlank()) {
            try {
                url.append("&text=").append(URLEncoder.encode(keyword, StandardCharsets.UTF_8));
            } catch (Exception e) {
                log.warn("Failed to encode keyword: {}", keyword, e);
            }
        }

        // modifiedFrom убираем отсюда — если нужен, передавай отдельным параметром и добавляй явно
        return url.toString();
    }

    List<Vacancy> parseVacancies(String jsonResponse, Long minSalary) throws Exception {
        List<Vacancy> vacancies = new ArrayList<>();
        JsonNode root = objectMapper.readTree(jsonResponse);

        JsonNode results = root.get("results");
        if (results == null || !results.has("vacancies")) {
            return vacancies;
        }

        JsonNode vacanciesArray = results.get("vacancies");
        if (!vacanciesArray.isArray()) {
            return vacancies;
        }

        for (JsonNode itemNode : vacanciesArray) {
            try {
                Vacancy vacancy = parseVacancy(itemNode);
                if (vacancy != null) {
                    if (minSalary == null || !hasInsufficientSalary(vacancy, minSalary)) {
                        vacancies.add(vacancy);
                    }
                }
            } catch (Exception e) {
                log.warn("Error parsing single vacancy node", e);
            }
        }

        return vacancies;
    }

    private Vacancy parseVacancy(JsonNode node) {
        JsonNode vacancyNode = node.get("vacancy");
        if (vacancyNode == null || !vacancyNode.isObject()) {
            return null;
        }

        String id = getTextOrNull(vacancyNode, "id");
        if (id == null || id.isBlank()) {
            log.warn("Vacancy without ID found, skipping.");
            return null;
        }

        Vacancy vacancy = Vacancy.builder()
                .id(id)
                .title(getTextOrNull(vacancyNode, "job-name"))
                .url(getTextOrNull(vacancyNode, "vac_url"))
                .region(getTextOrNull(vacancyNode.path("region"), "name"))
                .regionCode(getTextOrNull(vacancyNode.path("region"), "region_code"))
                .companyName(getTextOrNull(vacancyNode.path("company"), "name"))
                .salaryFrom(getIntOrNull(vacancyNode, "salary_min"))
                .salaryTo(getIntOrNull(vacancyNode, "salary_max"))
                .currency(getTextOrNull(vacancyNode, "currency"))
                .experienceFrom(getIntOrNull(vacancyNode.path("requirement"), "experience"))
                .education(getTextOrNull(vacancyNode.path("requirement"), "education"))
                .contacts(extractContacts(vacancyNode))
                .description(getTextOrNull(vacancyNode, "jobDescription"))
                .build();

        String createDateStr = getTextOrNull(vacancyNode, "creation-date");
        if (createDateStr != null && !createDateStr.isEmpty()) {
            try {
                String isoWithTime = createDateStr.contains("T")
                        ? createDateStr
                        : createDateStr + "T00:00:00Z";
                vacancy.setPublishedDate(
                        OffsetDateTime.parse(isoWithTime).toLocalDateTime()
                );
            } catch (Exception e) {
                log.warn("Failed to parse creation-date: {}", createDateStr, e);
            }
        }

        return vacancy;
    }

    private String extractContacts(JsonNode vacancyNode) {
        JsonNode contactsNode = vacancyNode.path("contact_list");
        if (!contactsNode.isArray()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (JsonNode contact : contactsNode) {
            String type = getTextOrNull(contact, "contact_type");
            String value = getTextOrNull(contact, "contact_value");
            if (type != null && value != null) {
                if (sb.length() > 0) sb.append("; ");
                sb.append(type).append(": ").append(value);
            }
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    private String getTextOrNull(JsonNode node, String field) {
        if (node == null) return null;
        JsonNode fieldNode = node.get(field);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : null;
    }

    private Long getIntOrNull(JsonNode node, String field) {
        if (node == null) return null;
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }
        return fieldNode.asLong();
    }

    private boolean hasInsufficientSalary(Vacancy vacancy, Long minSalary) {
        Long salaryFrom = vacancy.getSalaryFrom();
        if (salaryFrom == null) {
            return true;
        }
        return salaryFrom < minSalary;
    }
}