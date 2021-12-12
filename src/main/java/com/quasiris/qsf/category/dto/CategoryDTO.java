package com.quasiris.qsf.category.dto;

import java.util.Objects;

public class CategoryDTO {

    private String id;
    private String name;
    private Integer position;
    private Long count;

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
     * Getter for property 'position'.
     *
     * @return Value for property 'position'.
     */
    public Integer getPosition() {
        return position;
    }

    /**
     * Setter for property 'position'.
     *
     * @param position Value to set for property 'position'.
     */
    public void setPosition(Integer position) {
        this.position = position;
    }

    /**
     * Getter for property 'count'.
     *
     * @return Value for property 'count'.
     */
    public Long getCount() {
        return count;
    }

    /**
     * Setter for property 'count'.
     *
     * @param count Value to set for property 'count'.
     */
    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryDTO that = (CategoryDTO) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CategoryDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", position=" + position +
                '}';
    }
}
