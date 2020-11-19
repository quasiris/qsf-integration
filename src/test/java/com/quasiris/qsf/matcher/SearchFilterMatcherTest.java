package com.quasiris.qsf.matcher;

import com.quasiris.qsf.query.SearchFilter;
import com.quasiris.qsf.query.SearchFilterBuilder;
import com.quasiris.qsf.test.converter.NullValueConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchFilterMatcherTest {


    @Test
    public void testSimpleStringListValue() {
        SearchFilterMatcher matcher = new SearchFilterMatcher();
        SearchFilter searchFilter = SearchFilterBuilder.create().withId("farbe").value("grün").value("gelb").build();

        assertTrue(matcher.matches(searchFilter, Arrays.asList("grün", "green")));
        assertTrue(matcher.matches(searchFilter, Arrays.asList("Grün", "green")));
        assertFalse(matcher.matches(searchFilter, Arrays.asList("blau", "blue")));
        assertFalse(matcher.matches(searchFilter, Arrays.asList("Blau", "blue")));
    }

    @Test
    public void testSimpleStringValue() {
        SearchFilterMatcher matcher = new SearchFilterMatcher();
        SearchFilter searchFilter = SearchFilterBuilder.create().withId("farbe").value("grün").value("gelb").build();

        assertTrue(matcher.matches(searchFilter, "grün"));
        assertTrue(matcher.matches(searchFilter, "Grün"));
        assertFalse(matcher.matches(searchFilter, "blau"));
        assertFalse(matcher.matches(searchFilter, "Blau"));
    }

    @Test
    public void testSimpleStringValueContains() {
        SearchFilterMatcher matcher = new SearchFilterMatcher();
        SearchFilter searchFilter = SearchFilterBuilder.create().withId("farbe").value("grün").value("gelb").build();

        assertTrue(matcher.matches(searchFilter, "grün (metallic)"));
        assertTrue(matcher.matches(searchFilter, "Grün (metallic)"));
        assertFalse(matcher.matches(searchFilter, "blau"));
        assertFalse(matcher.matches(searchFilter, "Blau"));

    }

    @Test
    public void testRangeValueLowerIncludedWithLists() {
        SearchFilterMatcher matcher = new SearchFilterMatcher();
        SearchFilter searchFilter = SearchFilterBuilder.create().withId("speicher").rangeFilter(128.0, null).build();

        assertFalse(matcher.matchesRangeValue(searchFilter, Arrays.asList(126.0 ,127.0)));
        assertTrue(matcher.matchesRangeValue(searchFilter, Arrays.asList(126.0 ,128.0)));
        assertTrue(matcher.matchesRangeValue(searchFilter, Arrays.asList(126.0 ,129.0)));
    }


    @Test
    public void testRangeValueLowerIncluded() {
        SearchFilterMatcher matcher = new SearchFilterMatcher();
        SearchFilter searchFilter = SearchFilterBuilder.create().withId("speicher").rangeFilter(128.0, null).build();

        assertFalse(matcher.matchesRangeValue(searchFilter, 127.0));
        assertTrue(matcher.matchesRangeValue(searchFilter, 128.0));
        assertTrue(matcher.matchesRangeValue(searchFilter, 129.0));
    }

    @Test
    public void testRangeValueLowerExcluded() {
        SearchFilterMatcher matcher = new SearchFilterMatcher();
        SearchFilter searchFilter = SearchFilterBuilder.create().withId("speicher").withLowerBoundExclude().rangeFilter(128.0, null).build();

        assertFalse(matcher.matchesRangeValue(searchFilter, 127.0));
        assertFalse(matcher.matchesRangeValue(searchFilter, 128.0));
        assertTrue(matcher.matchesRangeValue(searchFilter, 129.0));
    }

    @Test
    public void testRangeValueUpperIncluded() {
        SearchFilterMatcher matcher = new SearchFilterMatcher();
        SearchFilter searchFilter = SearchFilterBuilder.create().withId("speicher").rangeFilter(null, 128.0).build();

        assertTrue(matcher.matchesRangeValue(searchFilter, 127.0));
        assertTrue(matcher.matchesRangeValue(searchFilter, 128.0));
        assertFalse(matcher.matchesRangeValue(searchFilter, 129.0));
    }

    @Test
    public void testRangeValueUpperExcluded() {
        SearchFilterMatcher matcher = new SearchFilterMatcher();
        SearchFilter searchFilter = SearchFilterBuilder.create().withId("speicher").withUpperBoundExclude().rangeFilter(null, 128.0).build();

        assertTrue(matcher.matchesRangeValue(searchFilter, 127.0));
        assertFalse(matcher.matchesRangeValue(searchFilter, 128.0));
        assertFalse(matcher.matchesRangeValue(searchFilter, 129.0));
    }

    @Test
    public void testRangeValueUpperLowerExcluded() {
        SearchFilterMatcher matcher = new SearchFilterMatcher();
        SearchFilter searchFilter = SearchFilterBuilder.create().withId("speicher").
                withLowerBoundExclude().
                withUpperBoundExclude().
                rangeFilter(128.0, 256.0).
                build();

        assertFalse(matcher.matchesRangeValue(searchFilter, 127.0));
        assertFalse(matcher.matchesRangeValue(searchFilter, 128.0));
        assertTrue(matcher.matchesRangeValue(searchFilter, 129.0));
        assertTrue(matcher.matchesRangeValue(searchFilter, 255.0));
        assertFalse(matcher.matchesRangeValue(searchFilter, 256.0));
        assertFalse(matcher.matchesRangeValue(searchFilter, 257.0));
    }

    @Test
    public void testRangeValueUpperLowerIncluded() {
        SearchFilterMatcher matcher = new SearchFilterMatcher();
        SearchFilter searchFilter = SearchFilterBuilder.create().withId("speicher").
                withLowerBoundInclude().
                withUpperBoundInclude().
                rangeFilter(128.0, 256.0).
                build();

        assertFalse(matcher.matchesRangeValue(searchFilter, 127.0));
        assertTrue(matcher.matchesRangeValue(searchFilter, 128.0));
        assertTrue(matcher.matchesRangeValue(searchFilter, 129.0));
        assertTrue(matcher.matchesRangeValue(searchFilter, 255.0));
        assertTrue(matcher.matchesRangeValue(searchFilter, 256.0));
        assertFalse(matcher.matchesRangeValue(searchFilter, 257.0));
    }

    @Test
    public void testRangeValueUpperIncludeLowerExclude() {
        SearchFilterMatcher matcher = new SearchFilterMatcher();
        SearchFilter searchFilter = SearchFilterBuilder.create().withId("speicher").
                withLowerBoundInclude().
                withUpperBoundExclude().
                rangeFilter(128.0, 256.0).
                build();

        assertFalse(matcher.matchesRangeValue(searchFilter, 127.0));
        assertTrue(matcher.matchesRangeValue(searchFilter, 128.0));
        assertTrue(matcher.matchesRangeValue(searchFilter, 129.0));
        assertTrue(matcher.matchesRangeValue(searchFilter, 255.0));
        assertFalse(matcher.matchesRangeValue(searchFilter, 256.0));
        assertFalse(matcher.matchesRangeValue(searchFilter, 257.0));
    }


    @Test
    public void testRangeValueUpperExcludeLowerInclude() {
        SearchFilterMatcher matcher = new SearchFilterMatcher();
        SearchFilter searchFilter = SearchFilterBuilder.create().withId("speicher").
                withLowerBoundExclude().
                withUpperBoundInclude().
                rangeFilter(128.0, 256.0).
                build();

        assertFalse(matcher.matchesRangeValue(searchFilter, 127.0));
        assertFalse(matcher.matchesRangeValue(searchFilter, 128.0));
        assertTrue(matcher.matchesRangeValue(searchFilter, 129.0));
        assertTrue(matcher.matchesRangeValue(searchFilter, 255.0));
        assertTrue(matcher.matchesRangeValue(searchFilter, 256.0));
        assertFalse(matcher.matchesRangeValue(searchFilter, 257.0));
    }

    @DisplayName("Test null value. Always return false when the value is null")
    @ParameterizedTest(name = "{index} => min=''{0}'' max=''{1}''")
    @CsvSource({
            "null, null",
            "100.0, null",
            "null, 200.0",
            "100.0, 200.0",
    })
    public void testRangeValueNull (
            @ConvertWith(NullValueConverter.class) String min,
            @ConvertWith(NullValueConverter.class) String max
    ) {
        SearchFilterMatcher matcher = new SearchFilterMatcher();
        SearchFilter searchFilter = SearchFilterBuilder.create().
                withId("speicher").
                withUpperBoundExclude().
                rangeFilter(toDouble(min), toDouble(max)).
                build();
        assertFalse(matcher.matchesRangeValue(searchFilter, toDouble(null)));
    }

    private Double toDouble(String value) {
        if(value == null) {
            return null;
        }
        return Double.valueOf(value);
    }

    @Test
    public void testRangeValueMinMaxNull() {
        SearchFilterMatcher matcher = new SearchFilterMatcher();
        Double min = null;
        Double max = null;
        SearchFilter searchFilter = SearchFilterBuilder.create().withId("speicher").withUpperBoundExclude().rangeFilter(min, max).build();
        assertTrue(matcher.matchesRangeValue(searchFilter, 127.0));
    }
}