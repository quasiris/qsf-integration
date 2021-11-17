package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.pipeline.filter.mapper.FacetKeyMapper;

public class FacetMapping {

    private String id;
    private String name;
    private String type;
    private FacetKeyMapper facetKeyMapper;

    /**
     * Getter for property 'id'.
     *
     * @return Value for property 'id'.
     */
    public String getId() {
        return id;
    }

    /**
     * Setter for property 'id'.
     *
     * @param id Value to set for property 'id'.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter for property 'name'.
     *
     * @return Value for property 'name'.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for property 'name'.
     *
     * @param name Value to set for property 'name'.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for property 'facetKeyMapper'.
     *
     * @return Value for property 'facetKeyMapper'.
     */
    public FacetKeyMapper getFacetKeyMapper() {
        return facetKeyMapper;
    }

    /**
     * Setter for property 'facetKeyMapper'.
     *
     * @param facetKeyMapper Value to set for property 'facetKeyMapper'.
     */
    public void setFacetKeyMapper(FacetKeyMapper facetKeyMapper) {
        this.facetKeyMapper = facetKeyMapper;
    }

    /**
     * Getter for property 'type'.
     *
     * @return Value for property 'type'.
     */
    public String getType() {
        return type;
    }

    /**
     * Setter for property 'type'.
     *
     * @param type Value to set for property 'type'.
     */
    public void setType(String type) {
        this.type = type;
    }
}
