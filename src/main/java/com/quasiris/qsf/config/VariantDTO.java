package com.quasiris.qsf.config;

import java.util.Set;

public class VariantDTO {

    private String variantId;
    protected Set<String> innerHitsSourceFields;

    protected String variantSort;
    protected Integer variantSize;

    public Set<String> getInnerHitsSourceFields() {
        return innerHitsSourceFields;
    }

    public void setInnerHitsSourceFields(Set<String> innerHitsSourceFields) {
        this.innerHitsSourceFields = innerHitsSourceFields;
    }

    public String getVariantId() {
        return variantId;
    }

    public void setVariantId(String variantId) {
        this.variantId = variantId;
    }

    public Integer getVariantSize() {
        return variantSize;
    }

    public void setVariantSize(Integer variantSize) {
        this.variantSize = variantSize;
    }

    public String getVariantSort() {
        return variantSort;
    }

    public void setVariantSort(String variantSort) {
        this.variantSort = variantSort;
    }
}
