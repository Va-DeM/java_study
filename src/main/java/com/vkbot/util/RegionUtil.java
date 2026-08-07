package com.vkbot.util;

import java.util.HashMap;
import java.util.Map;

public class RegionUtil {
    // Коды соответствуют официальным автомобильным кодам регионов РФ (те же коды
    // использует API-агрегатор opendata.trudvsem.ru в пути /vacancies/region/{code}).
    // Коды 80, 81, 84, 85, 88, 96, 97, 98 и т.п. не входят в список — это упразднённые
    // автономные округа, вошедшие в состав других регионов, отдельного кода не имеют.
    private static final Map<String, String> REGIONS = new HashMap<>();

    static {
        REGIONS.put("1", "Республика Адыгея (Адыгея)");
        REGIONS.put("2", "Республика Башкортостан");
        REGIONS.put("3", "Республика Бурятия");
        REGIONS.put("4", "Республика Алтай");
        REGIONS.put("5", "Республика Дагестан");
        REGIONS.put("6", "Республика Ингушетия");
        REGIONS.put("7", "Кабардино-Балкарская Республика");
        REGIONS.put("8", "Республика Калмыкия");
        REGIONS.put("9", "Карачаево-Черкесская Республика");
        REGIONS.put("10", "Республика Карелия");
        REGIONS.put("11", "Республика Коми");
        REGIONS.put("12", "Республика Марий Эл");
        REGIONS.put("13", "Республика Мордовия");
        REGIONS.put("14", "Республика Саха (Якутия)");
        REGIONS.put("15", "Республика Северная Осетия — Алания");
        REGIONS.put("16", "Республика Татарстан (Татарстан)");
        REGIONS.put("17", "Республика Тыва");
        REGIONS.put("18", "Удмуртская Республика");
        REGIONS.put("19", "Республика Хакасия");
        REGIONS.put("20", "Чеченская Республика");
        REGIONS.put("21", "Чувашская Республика — Чувашия");
        REGIONS.put("22", "Алтайский край");
        REGIONS.put("23", "Краснодарский край");
        REGIONS.put("24", "Красноярский край");
        REGIONS.put("25", "Приморский край");
        REGIONS.put("26", "Ставропольский край");
        REGIONS.put("27", "Хабаровский край");
        REGIONS.put("28", "Амурская область");
        REGIONS.put("29", "Архангельская область");
        REGIONS.put("30", "Астраханская область");
        REGIONS.put("31", "Белгородская область");
        REGIONS.put("32", "Брянская область");
        REGIONS.put("33", "Владимирская область");
        REGIONS.put("34", "Волгоградская область");
        REGIONS.put("35", "Вологодская область");
        REGIONS.put("36", "Воронежская область");
        REGIONS.put("37", "Ивановская область");
        REGIONS.put("38", "Иркутская область");
        REGIONS.put("39", "Калининградская область");
        REGIONS.put("40", "Калужская область");
        REGIONS.put("41", "Камчатский край");
        REGIONS.put("42", "Кемеровская область — Кузбасс");
        REGIONS.put("43", "Кировская область");
        REGIONS.put("44", "Костромская область");
        REGIONS.put("45", "Курганская область");
        REGIONS.put("46", "Курская область");
        REGIONS.put("47", "Ленинградская область");
        REGIONS.put("48", "Липецкая область");
        REGIONS.put("49", "Магаданская область");
        REGIONS.put("50", "Московская область");
        REGIONS.put("51", "Мурманская область");
        REGIONS.put("52", "Нижегородская область");
        REGIONS.put("53", "Новгородская область");
        REGIONS.put("54", "Новосибирская область");
        REGIONS.put("55", "Омская область");
        REGIONS.put("56", "Оренбургская область");
        REGIONS.put("57", "Орловская область");
        REGIONS.put("58", "Пензенская область");
        REGIONS.put("59", "Пермский край");
        REGIONS.put("60", "Псковская область");
        REGIONS.put("61", "Ростовская область");
        REGIONS.put("62", "Рязанская область");
        REGIONS.put("63", "Самарская область");
        REGIONS.put("64", "Саратовская область");
        REGIONS.put("65", "Сахалинская область");
        REGIONS.put("66", "Свердловская область");
        REGIONS.put("67", "Смоленская область");
        REGIONS.put("68", "Тамбовская область");
        REGIONS.put("69", "Тверская область");
        REGIONS.put("70", "Томская область");
        REGIONS.put("71", "Тульская область");
        REGIONS.put("72", "Тюменская область");
        REGIONS.put("73", "Ульяновская область");
        REGIONS.put("74", "Челябинская область");
        REGIONS.put("75", "Забайкальский край");
        REGIONS.put("76", "Ярославская область");
        REGIONS.put("77", "г. Москва");
        REGIONS.put("78", "г. Санкт-Петербург");
        REGIONS.put("79", "Еврейская автономная область");
        REGIONS.put("83", "Ненецкий автономный округ");
        REGIONS.put("86", "Ханты-Мансийский автономный округ — Югра");
        REGIONS.put("87", "Чукотский автономный округ");
        REGIONS.put("89", "Ямало-Ненецкий автономный округ");
        REGIONS.put("90", "Запорожская область");
        REGIONS.put("91", "Республика Крым");
        REGIONS.put("92", "г. Севастополь");
        REGIONS.put("93", "Донецкая Народная Республика");
        REGIONS.put("94", "Луганская Народная Республика");
        REGIONS.put("95", "Херсонская область");
    }

    public static String getRegionName(String code) {
        return REGIONS.getOrDefault(code, "Не найден");
    }

    public static String getRegionCode(String name) {
        return REGIONS.entrySet().stream()
                .filter(e -> e.getValue().equalsIgnoreCase(name))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public static Map<String, String> getAllRegions() {
        return new HashMap<>(REGIONS);
    }

    public static boolean regionExists(String code) {
        return REGIONS.containsKey(code);
    }
}
