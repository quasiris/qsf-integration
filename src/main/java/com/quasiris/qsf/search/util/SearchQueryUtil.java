package com.quasiris.qsf.search.util;

import com.quasiris.qsf.dto.query.*;
import com.quasiris.qsf.query.SearchFilter;
import com.quasiris.qsf.query.SearchQuery;

import java.util.Objects;

public class SearchQueryUtil {

    public static boolean isVariantEnabled(SearchQuery searchQuery) {
        if(searchQuery.getResult() == null) {
            return true;
        }
        if(searchQuery.getResult().getVariant() == null) {
           return true;
        }

        if(searchQuery.getResult().getVariant().getEnabled() == null) {
           return true;
        }

        return searchQuery.getResult().getVariant().getEnabled();
    }

    public static boolean isFacetEnabled(SearchQuery searchQuery) {
        if(searchQuery.getResult() == null) {
            return true;
        }
        if(searchQuery.getResult().getFacet() == null) {
           return true;
        }

        if(searchQuery.getResult().getFacet().getEnabled() == null) {
           return true;
        }

        return searchQuery.getResult().getFacet().getEnabled();
    }

    public static void disableFacets(SearchQuery searchQuery) {
        if(searchQuery.getResult() == null) {
            searchQuery.setResult(new ResultDTO());
        }
        if(searchQuery.getResult().getFacet() == null) {
            searchQuery.getResult().setFacet(new FacetDTO());
        }
        searchQuery.getResult().getFacet().setEnabled(Boolean.FALSE);
    }

    public static void disableSpellcheck(SearchQuery searchQuery) {
        if(searchQuery.getResult() == null) {
            searchQuery.setResult(new ResultDTO());
        }
        if(searchQuery.getResult().getSpellcheck() == null) {
            searchQuery.getResult().setSpellcheck(new SpellcheckDTO());
        }
        searchQuery.getResult().getSpellcheck().setEnabled(Boolean.FALSE);
    }

    public static void disablePaging(SearchQuery searchQuery) {
        if(searchQuery.getResult() == null) {
            searchQuery.setResult(new ResultDTO());
        }
        if(searchQuery.getResult().getPaging() == null) {
            searchQuery.getResult().setPaging(new PagingDTO());
        }

        searchQuery.getResult().getPaging().setEnabled(Boolean.FALSE);
    }
    public static void disableVariant(SearchQuery searchQuery) {
        if(searchQuery.getResult() == null) {
            searchQuery.setResult(new ResultDTO());
        }
        if(searchQuery.getResult().getVariant() == null) {
            searchQuery.getResult().setVariant(new VariantDTO());
        }

        searchQuery.getResult().getVariant().setEnabled(Boolean.FALSE);
    }

    public static boolean hasTag(SearchQuery searchQuery, String tagName) {
        if (searchQuery != null && searchQuery.getTags() != null) {
            return searchQuery.getTags().stream().anyMatch(t -> Objects.equals(tagName, t));
        }
        return false;
    }

    public static String getParameterAsString(String parameterName, SearchQuery searchQuery, String defaultValue) {
        if(searchQuery == null || searchQuery.getParameters() == null) {
            return defaultValue;
        }
        Object p = searchQuery.getParameters().get(parameterName);
        if(p == null) {
            return defaultValue;
        }
        return p.toString();

    }
    public static String getParameterAsString(String parameterName, SearchQuery searchQuery) {
        return getParameterAsString(parameterName, searchQuery, null);

    }

    public static String getFilterValue(String filterId, SearchQuery searchQuery) {
        SearchFilter searchFilter = searchQuery.getSearchFilterById(filterId);
        if(searchFilter == null) {
            return null;
        }
        if(searchFilter.getValues() != null && !searchFilter.getValues().isEmpty()) {
            return searchFilter.getValues().get(0);
        }
        return null;
    }
}
