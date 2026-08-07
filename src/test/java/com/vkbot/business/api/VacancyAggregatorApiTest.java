package com.vkbot.business.api;

import com.vkbot.business.model.Vacancy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VacancyAggregatorApiTest {

    private final VacancyAggregatorApi api = new VacancyAggregatorApi();

    // ---- buildUrl ----

    @Test
    void buildUrl_withoutFiltersOnlyHasOffsetAndLimit() {
        String url = api.buildUrl(null, null, null, 0, 50);

        assertEquals("https://opendata.trudvsem.ru/api/v1/vacancies?offset=0&limit=50", url);
    }

    @Test
    void buildUrl_regionGoesIntoPathNotQuery() {
        String url = api.buildUrl("78", null, null, 0, 50);

        assertTrue(url.contains("/vacancies/region/78?"), url);
        assertFalse(url.contains("region=78"));
    }

    @Test
    void buildUrl_blankRegionIsIgnored() {
        String url = api.buildUrl("  ", null, null, 0, 50);

        assertFalse(url.contains("/region/"));
    }

    @Test
    void buildUrl_positiveExperienceIsIncludedAsQueryParam() {
        String url = api.buildUrl(null, null, 3, 0, 50);

        assertTrue(url.contains("&experienceFrom=3"), url);
    }

    @Test
    void buildUrl_zeroOrNullExperienceIsOmitted() {
        assertFalse(api.buildUrl(null, null, 0, 0, 50).contains("experienceFrom"));
        assertFalse(api.buildUrl(null, null, null, 0, 50).contains("experienceFrom"));
    }

    @Test
    void buildUrl_keywordIsUrlEncoded() {
        String url = api.buildUrl(null, "java разработчик", null, 0, 50);

        assertTrue(url.contains("&text=java+"), url);
        assertFalse(url.contains(" "));
    }

    @Test
    void buildUrl_blankKeywordIsOmitted() {
        assertFalse(api.buildUrl(null, "   ", null, 0, 50).contains("text="));
    }

    // ---- parseVacancies ----

    @Test
    void parseVacancies_mapsAllFieldsFromFullPayload() throws Exception {
        String json = """
                {
                  "status": "200",
                  "results": {
                    "vacancies": [
                      {
                        "vacancy": {
                          "id": "abc-123",
                          "job-name": "Java Developer",
                          "vac_url": "https://trudvsem.ru/vacancy/abc-123",
                          "region": {"name": "Москва", "region_code": "77"},
                          "company": {"name": "ООО Ромашка"},
                          "salary_min": 80000,
                          "salary_max": 120000,
                          "currency": "RUR",
                          "requirement": {"experience": 3, "education": "Высшее"},
                          "contact_list": [
                            {"contact_type": "phone", "contact_value": "+70000000000"},
                            {"contact_type": "email", "contact_value": "hr@romashka.ru"}
                          ],
                          "jobDescription": "Разработка на Java",
                          "creation-date": "2025-01-15"
                        }
                      }
                    ]
                  }
                }
                """;

        List<Vacancy> vacancies = api.parseVacancies(json, null);

        assertEquals(1, vacancies.size());
        Vacancy v = vacancies.get(0);
        assertEquals("abc-123", v.getId());
        assertEquals("Java Developer", v.getTitle());
        assertEquals("https://trudvsem.ru/vacancy/abc-123", v.getUrl());
        assertEquals("Москва", v.getRegion());
        assertEquals("77", v.getRegionCode());
        assertEquals("ООО Ромашка", v.getCompanyName());
        assertEquals(80000L, v.getSalaryFrom());
        assertEquals(120000L, v.getSalaryTo());
        assertEquals("RUR", v.getCurrency());
        assertEquals(3L, v.getExperienceFrom());
        assertEquals("Высшее", v.getEducation());
        assertTrue(v.getContacts().contains("phone: +70000000000"));
        assertTrue(v.getContacts().contains("email: hr@romashka.ru"));
        assertEquals("Разработка на Java", v.getDescription());
        assertNotNull(v.getPublishedDate());
    }

    @Test
    void parseVacancies_missingResultsNodeReturnsEmptyList() throws Exception {
        assertTrue(api.parseVacancies("{\"status\":\"200\"}", null).isEmpty());
    }

    @Test
    void parseVacancies_resultsWithoutVacanciesArrayReturnsEmptyList() throws Exception {
        String json = "{\"results\": {}}";

        assertTrue(api.parseVacancies(json, null).isEmpty());
    }

    @Test
    void parseVacancies_vacanciesNotAnArrayReturnsEmptyList() throws Exception {
        String json = "{\"results\": {\"vacancies\": \"not-an-array\"}}";

        assertTrue(api.parseVacancies(json, null).isEmpty());
    }

    @Test
    void parseVacancies_itemWithoutVacancyWrapperIsSkipped() throws Exception {
        String json = """
                {"results": {"vacancies": [ {"notVacancy": {}} ]}}
                """;

        assertTrue(api.parseVacancies(json, null).isEmpty());
    }

    @Test
    void parseVacancies_itemWithoutIdIsSkipped() throws Exception {
        String json = """
                {"results": {"vacancies": [ {"vacancy": {"job-name": "No id here"}} ]}}
                """;

        assertTrue(api.parseVacancies(json, null).isEmpty());
    }

    @Test
    void parseVacancies_oneMalformedItemDoesNotBreakOthers() throws Exception {
        String json = """
                {"results": {"vacancies": [
                  {"vacancy": {"id": "bad", "creation-date": "not-a-date"}},
                  {"vacancy": {"id": "good", "job-name": "Fine"}}
                ]}}
                """;

        List<Vacancy> vacancies = api.parseVacancies(json, null);

        assertEquals(2, vacancies.size());
        Vacancy bad = vacancies.stream().filter(v -> v.getId().equals("bad")).findFirst().orElseThrow();
        assertNull(bad.getPublishedDate());
    }

    @Test
    void parseVacancies_dateOnlyCreationDateIsParsed() throws Exception {
        String json = """
                {"results": {"vacancies": [
                  {"vacancy": {"id": "v1", "creation-date": "2025-06-01"}}
                ]}}
                """;

        Vacancy v = api.parseVacancies(json, null).get(0);

        assertEquals(2025, v.getPublishedDate().getYear());
        assertEquals(6, v.getPublishedDate().getMonthValue());
    }

    @Test
    void parseVacancies_excludesVacancyBelowMinSalary() throws Exception {
        String json = """
                {"results": {"vacancies": [
                  {"vacancy": {"id": "v1", "salary_min": 30000}},
                  {"vacancy": {"id": "v2", "salary_min": 90000}}
                ]}}
                """;

        List<Vacancy> vacancies = api.parseVacancies(json, 50000L);

        assertEquals(1, vacancies.size());
        assertEquals("v2", vacancies.get(0).getId());
    }

    @Test
    void parseVacancies_noContactsResultsInNullContactsField() throws Exception {
        String json = """
                {"results": {"vacancies": [ {"vacancy": {"id": "v1"}} ]}}
                """;

        assertNull(api.parseVacancies(json, null).get(0).getContacts());
    }
}
