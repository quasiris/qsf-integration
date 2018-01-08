package com.quasiris.qsf.response;

import java.util.Comparator;

/**
 * Created by mki on 31.12.17.
 */
public class FacetValueCountComparator implements Comparator<FacetValue> {
    @Override
    public int compare(FacetValue v1, FacetValue v2) {
        if(v1 == null || v2 == null) {
            return 0;
        }
        return v1.getCount().compareTo(v2.getCount());
    }
}
