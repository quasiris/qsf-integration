package com.quasiris.qsf.query;

/**
 * Created by mki on 11.11.16.
 */
public class Sort {

    private String sort;

    private String direction;

    private String field;


    public Sort() {
    }

    public Sort(String sort) {
        this.sort = sort;
    }

    public Sort(String field, String direction) {
        this.direction = direction;
        this.field = field;
    }

    public String getSort() {
        return sort;
    }

    /**
     * Getter for property 'direction'.
     *
     * @return Value for property 'direction'.
     */
    public String getDirection() {
        return direction;
    }

    /**
     * Setter for property 'direction'.
     *
     * @param direction Value to set for property 'direction'.
     */
    public void setDirection(String direction) {
        this.direction = direction;
    }

    /**
     * Getter for property 'field'.
     *
     * @return Value for property 'field'.
     */
    public String getField() {
        return field;
    }

    /**
     * Setter for property 'field'.
     *
     * @param field Value to set for property 'field'.
     */
    public void setField(String field) {
        this.field = field;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    @Override
    public String toString() {
        return "Sort{" +
                "sort='" + sort + '\'' +
                ", direction='" + direction + '\'' +
                ", field='" + field + '\'' +
                '}';
    }
}
