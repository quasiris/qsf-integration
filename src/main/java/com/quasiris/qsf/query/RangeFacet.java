package com.quasiris.qsf.query;

import java.util.List;

public class RangeFacet extends Facet {

    public RangeFacet() {
        this.setType("range");
    }
    private List<Range> ranges;

    public List<Range> getRanges() {
        return ranges;
    }

    public void setRanges(List<Range> ranges) {
        this.ranges = ranges;
    }
}
