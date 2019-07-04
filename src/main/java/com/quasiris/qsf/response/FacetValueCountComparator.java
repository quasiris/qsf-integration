package com.quasiris.qsf.response;

import java.util.Comparator;

/**
 * Created by mki on 31.12.17.
 */
public class FacetValueCountComparator implements Comparator<FacetValue> {

    private boolean ascending = true;

    public FacetValueCountComparator() {
    }

    public FacetValueCountComparator(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public int compare(FacetValue v1, FacetValue v2) {
        if(v1 == null || v2 == null) {
            return 0;
        }
        if(ascending) {
            return v1.getCount().compareTo(v2.getCount());
        } else {
            return v2.getCount().compareTo(v1.getCount());
        }
    }
}
