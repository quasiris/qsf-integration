package com.quasiris.qsf.search.util;

import com.quasiris.qsf.query.SearchQuery;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchQueryUtilTest {

    @Test
    public void isVariantEnabledFalse() {
        SearchQuery searchQuery = createSearchQuery();
        SearchQueryUtil.disableVariant(searchQuery);
        boolean enabled = SearchQueryUtil.isVariantEnabled(searchQuery);
        assertFalse(enabled);
    }


    SearchQuery createSearchQuery() {
        return new SearchQuery();
    }

}