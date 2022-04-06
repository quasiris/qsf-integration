package com.quasiris.qsf.util;

import com.quasiris.qsf.query.BaseSearchFilter;
import com.quasiris.qsf.query.SearchFilter;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SearchFilters {

    public static boolean exists(String id, List<BaseSearchFilter> searchFilters) {

        SearchFilter searchFilter = get(id, searchFilters);

        return searchFilter != null;
    }
    public static SearchFilter get(String id, List<BaseSearchFilter> searchFilters) {

        SearchFilter searchFilter = searchFilters.stream().
                filter(f -> f instanceof SearchFilter).
                map(f -> (SearchFilter)f).
                filter(f -> f.getId().startsWith(id)).
                findFirst().
                orElse(null);

        return searchFilter;
    }


    public static List<BaseSearchFilter> remove(List<BaseSearchFilter> searchFilters, String idToRemove) {
        return remove(searchFilters, Collections.singleton(idToRemove));
    }
    public static List<BaseSearchFilter> remove(List<BaseSearchFilter> searchFilters, Set<String> idsToRemove) {
        List<BaseSearchFilter> copy = copy(searchFilters);
        return copy.stream().filter(f -> f instanceof SearchFilter).
                map(f -> (SearchFilter)f).
                filter(f -> !idsToRemove.contains(f.getId())).
                collect(Collectors.toList());
    }

    public static List<BaseSearchFilter> copy(List<BaseSearchFilter> searchFilters) {
        return SerializationUtils.deepCopyList(searchFilters);
    }
}
