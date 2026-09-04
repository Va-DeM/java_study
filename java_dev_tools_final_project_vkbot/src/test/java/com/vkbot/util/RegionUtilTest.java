package com.vkbot.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionUtilTest {

    @Test
    void getAllRegions_hasEightyFiveActiveSubjects() {
        // 79 подряд (коды 1-79) + 10 отдельных кодов новых/сохранившихся регионов.
        assertEquals(89, RegionUtil.getAllRegions().size());
    }

    @Test
    void getRegionName_matchesRealVehicleCodesForCitiesAndOkrugs() {
        // Коды совпадают с реальными автомобильными кодами регионов РФ (те же,
        // что использует API opendata.trudvsem.ru в пути /vacancies/region/{code}).
        assertEquals("г. Москва", RegionUtil.getRegionName("77"));
        assertEquals("г. Санкт-Петербург", RegionUtil.getRegionName("78"));
        assertEquals("Еврейская автономная область", RegionUtil.getRegionName("79"));
        assertEquals("Ненецкий автономный округ", RegionUtil.getRegionName("83"));
        assertEquals("Ханты-Мансийский автономный округ — Югра", RegionUtil.getRegionName("86"));
        assertEquals("Чукотский автономный округ", RegionUtil.getRegionName("87"));
        assertEquals("Ямало-Ненецкий автономный округ", RegionUtil.getRegionName("89"));
        assertEquals("Республика Крым", RegionUtil.getRegionName("91"));
        assertEquals("г. Севастополь", RegionUtil.getRegionName("92"));
    }

    @Test
    void regionExists_falseForHistoricalMergedOkrugCodes() {
        // 80, 81, 84, 85, 88 — упразднённые автономные округа, вошедшие в состав
        // других регионов после реформ 2000-х; отдельного кода больше нет.
        assertFalse(RegionUtil.regionExists("80"));
        assertFalse(RegionUtil.regionExists("81"));
        assertFalse(RegionUtil.regionExists("84"));
        assertFalse(RegionUtil.regionExists("85"));
        assertFalse(RegionUtil.regionExists("88"));
    }

    @Test
    void getRegionName_returnsPlaceholderForUnknownCode() {
        assertEquals("Не найден", RegionUtil.getRegionName("80"));
        assertEquals("Не найден", RegionUtil.getRegionName("nonsense"));
    }

    @Test
    void regionExists_trueForRepublicsAndKrais() {
        assertTrue(RegionUtil.regionExists("1"));
        assertTrue(RegionUtil.regionExists("22"));
        assertTrue(RegionUtil.regionExists("41"));
        assertTrue(RegionUtil.regionExists("95"));
    }

    @Test
    void getRegionCode_findsCodeByExactNameCaseInsensitive() {
        assertEquals("78", RegionUtil.getRegionCode("г. Санкт-Петербург"));
        assertEquals("78", RegionUtil.getRegionCode("Г. САНКТ-ПЕТЕРБУРГ"));
    }

    @Test
    void getRegionCode_returnsNullForUnknownName() {
        assertNull(RegionUtil.getRegionCode("Атлантида"));
    }

    @Test
    void getAllRegions_returnsDefensiveCopy() {
        RegionUtil.getAllRegions().clear();

        assertEquals(89, RegionUtil.getAllRegions().size());
    }
}
