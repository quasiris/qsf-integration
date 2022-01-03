package com.quasiris.qsf.util;

import com.quasiris.qsf.query.BaseSearchFilter;
import com.quasiris.qsf.query.FilterDataType;
import com.quasiris.qsf.query.FilterType;
import com.quasiris.qsf.query.SearchFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SerializationUtilsTest {
    @DisplayName("Test deepcopy filters")
    @Test
    void deepCopyList() {
        // given
        List<BaseSearchFilter> filters = new ArrayList<>();
        SearchFilter rangeFilter = new SearchFilter();
        rangeFilter.setId("price");
        rangeFilter.setName("price");
        rangeFilter.setFilterType(FilterType.RANGE);
        rangeFilter.setFilterDataType(FilterDataType.NUMBER);
        rangeFilter.setRangeValue("1", "100");
        filters.add(rangeFilter);

        // when
        List<BaseSearchFilter> baseSearchFilters = SerializationUtils.deepCopyList(filters);

        // then
        assertEquals(1, baseSearchFilters.size());
    }
}