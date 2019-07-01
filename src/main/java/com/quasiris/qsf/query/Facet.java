package com.quasiris.qsf.query;

public class Facet {

    private String id;
    private String name;

    private String type = "terms";

    private Integer size;

    private String sortBy;

    private String sortOrder = "asc";

    private String include;

    private String exclude;

    private Facet subFacet;

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

    public Facet getSubFacet() {
        return subFacet;
    }

    public void setSubFacet(Facet subFacet) {
        this.subFacet = subFacet;
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
}
