package com.quasiris.qsf.config;

import java.util.List;
import java.util.Set;

public class VariantDTO {

    private String variantId;

    protected String variantSort;
    protected Integer variantSize;

    private String variantResultField;
    private String variantCountField;

    private List<DisplayMappingDTO> mapping;

    private Set<String> options;


    public List<DisplayMappingDTO> getMapping() {
        return mapping;
    }

    public void setMapping(List<DisplayMappingDTO> mapping) {
        this.mapping = mapping;
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

    public String getVariantResultField() {
        return variantResultField;
    }

    public void setVariantResultField(String variantResultField) {
        this.variantResultField = variantResultField;
    }

    public Set<String> getOptions() {
        return options;
    }

    public void setOptions(Set<String> options) {
        this.options = options;
    }

    public String getVariantCountField() {
        return variantCountField;
    }

    public void setVariantCountField(String variantCountField) {
        this.variantCountField = variantCountField;
    }
}
