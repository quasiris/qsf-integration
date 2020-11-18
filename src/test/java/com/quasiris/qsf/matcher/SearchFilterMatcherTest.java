package com.quasiris.qsf.matcher;

import com.quasiris.qsf.query.SearchFilter;
import com.quasiris.qsf.query.SearchFilterBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchFilterMatcherTest {


    @Test
    public void testSimpleStringValue() {
        SearchFilterMatcher matcher = new SearchFilterMatcher();
        SearchFilter searchFilter = SearchFilterBuilder.create().withId("farbe").value("grün").value("gelb").build();

        assertTrue(matcher.matches(searchFilter, "grün"));
        assertFalse(matcher.matches(searchFilter, "blau"));

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
}