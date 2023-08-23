package com.quasiris.qsf.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Facet implements Serializable {

    private String id;
    private String name;

    private String type = "terms";

    private Integer size;

    private String sortBy;

    private String sortOrder = "asc";

    private String include;

    private String exclude;

    private Facet children;

    private FilterOperator operator = FilterOperator.AND;

    private List<BaseSearchFilter> facetFilters = new ArrayList<>();

    private List<String> excludeTags;
    private List<String> tags;

    private Map<String, Object> parameters;

    public List<String> getExcludeTags() {
        return excludeTags;
    }

    public void setExcludeTags(List<String> excludeTags) {
        this.excludeTags = excludeTags;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    /**
     * Getter for property 'children'.
     *
     * @return Value for property 'children'.
     */
    public Facet getChildren() {
        return children;
    }

    /**
     * Setter for property 'children'.
     *
     * @param children Value to set for property 'children'.
     */
    public void setChildren(Facet children) {
        this.children = children;
    }

    /**
     * Getter for property 'include'.
     *
     * @return Value for property 'include'.
     */
    public String getInclude() {
        return include;
    }

    /**
     * Setter for property 'include'.
     *
     * @param include Value to set for property 'include'.
     */
    public void setInclude(String include) {
        this.include = include;
    }

    /**
     * Getter for property 'exclude'.
     *
     * @return Value for property 'exclude'.
     */
    public String getExclude() {
        return exclude;
    }

    /**
     * Setter for property 'exclude'.
     *
     * @param exclude Value to set for property 'exclude'.
     */
    public void setExclude(String exclude) {
        this.exclude = exclude;
    }

    /**
     * Getter for property 'operator'.
     *
     * @return Value for property 'operator'.
     */
    public FilterOperator getOperator() {
        return operator;
    }

    /**
     * Setter for property 'operator'.
     *
     * @param operator Value to set for property 'operator'.
     */
    public void setOperator(FilterOperator operator) {
        this.operator = operator;
    }

    @Override
    public String toString() {
        return "Facet{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", size=" + size +
                ", sortBy='" + sortBy + '\'' +
                ", sortOrder='" + sortOrder + '\'' +
                '}';
    }


    public static final class Builder {
        private String id;
        private String name;
        private String type = "terms";
        private Integer size;
        private String sortBy;
        private String sortOrder = "asc";

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder size(Integer size) {
            this.size = size;
            return this;
        }

        public Builder sortBy(String sortBy) {
            this.sortBy = sortBy;
            return this;
        }

        public Builder sortOrder(String sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public Facet build() {
            Facet facet = new Facet();
            facet.setId(id);
            facet.setName(name);
            facet.setType(type);
            facet.setSize(size);
            facet.setSortBy(sortBy);
            facet.setSortOrder(sortOrder);
            return facet;
        }
    }

    /**
     * Getter for property 'facetFilters'.
     *
     * @return Value for property 'facetFilters'.
     */
    public List<BaseSearchFilter> getFacetFilters() {
        return facetFilters;
    }

    /**
     * Setter for property 'facetFilters'.
     *
     * @param facetFilters Value to set for property 'facetFilters'.
     */
    public void setFacetFilters(List<BaseSearchFilter> facetFilters) {
        this.facetFilters = facetFilters;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(String key, Object value) {
        if(this.parameters == null) {
            this.parameters = new HashMap<>();
        }
        parameters.put(key, value);
    }


}
