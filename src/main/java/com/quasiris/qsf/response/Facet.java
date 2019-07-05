package com.quasiris.qsf.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mki on 12.11.17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Facet {

    private String name;

    private String id;

    private String filterName;

    private Long count;

    private Long resultCount;

    private Boolean selected;

    private List<FacetValue> values = new ArrayList<>();


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<FacetValue> getValues() {
        return values;
    }

    public void setValues(List<FacetValue> values) {
        this.values = values;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Long getResultCount() {
        return resultCount;
    }

    public void setResultCount(Long resultCount) {
        this.resultCount = resultCount;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    /**
     * Getter for property 'selected'.
     *
     * @return Value for property 'selected'.
     */
    public Boolean getSelected() {
        return selected;
    }

    /**
     * Setter for property 'selected'.
     *
     * @param selected Value to set for property 'selected'.
     */
    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public FacetValue getFacetValueByValue(String value) {
        for(FacetValue facetValue : getValues()) {
            if(facetValue.getValue().equals(value)) {
                return facetValue;
            }
        }
        return null;
    }

    public Long getFacetCountByValue(String value) {
        FacetValue facetValue = getFacetValueByValue(value);
        if(facetValue == null) {
            return 0L;
        }
        return facetValue.getCount();
    }


    @Override
    public String toString() {
        return "Facet{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", filterName='" + filterName + '\'' +
                ", count=" + count +
                ", resultCount=" + resultCount +
                ", values=" + values +
                '}';
    }
}
