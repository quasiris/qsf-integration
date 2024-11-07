package com.quasiris.qsf.query.builder;

import com.quasiris.qsf.query.BaseSearchFilter;
import com.quasiris.qsf.query.Facet;
import com.quasiris.qsf.query.FilterOperator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FacetBuilder {
    private String id;
    private String name;
    private String fieldName;
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
    private Map<String, Object> parameters = new HashMap<>();

    private FacetBuilder() {}

    public static FacetBuilder create() {
        return new FacetBuilder();
    }

    public FacetBuilder id(String id) {
        this.id = id;
        return this;
    }

    public FacetBuilder name(String name) {
        this.name = name;
        return this;
    }

    public FacetBuilder fieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    public FacetBuilder type(String type) {
        this.type = type;
        return this;
    }

    public FacetBuilder size(Integer size) {
        this.size = size;
        return this;
    }

    public FacetBuilder sortBy(String sortBy) {
        this.sortBy = sortBy;
        return this;
    }

    public FacetBuilder sortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    public FacetBuilder include(String include) {
        this.include = include;
        return this;
    }

    public FacetBuilder exclude(String exclude) {
        this.exclude = exclude;
        return this;
    }

    public FacetBuilder children(Facet children) {
        this.children = children;
        return this;
    }

    public FacetBuilder operator(FilterOperator operator) {
        this.operator = operator;
        return this;
    }

    public FacetBuilder facetFilters(List<BaseSearchFilter> facetFilters) {
        this.facetFilters = facetFilters;
        return this;
    }

    public FacetBuilder excludeTags(List<String> excludeTags) {
        this.excludeTags = excludeTags;
        return this;
    }

    public FacetBuilder tags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    public FacetBuilder parameters(Map<String, Object> parameters) {
        this.parameters = parameters;
        return this;
    }

    public FacetBuilder addParameter(String key, Object value) {
        if (this.parameters == null) {
            this.parameters = new HashMap<>();
        }
        this.parameters.put(key, value);
        return this;
    }

    public Facet build() {
        if(name == null) {
            name = id;
        }
        if(fieldName == null) {
            fieldName = name;
        }
        Facet facet = new Facet();
        facet.setId(id);
        facet.setName(name);
        facet.setFieldName(fieldName);
        facet.setType(type);
        facet.setSize(size);
        facet.setSortBy(sortBy);
        facet.setSortOrder(sortOrder);
        facet.setInclude(include);
        facet.setExclude(exclude);
        facet.setChildren(children);
        facet.setOperator(operator);
        facet.setFacetFilters(facetFilters);
        facet.setExcludeTags(excludeTags);
        facet.setTags(tags);
        facet.setParameters(parameters);
        return facet;
    }
}
