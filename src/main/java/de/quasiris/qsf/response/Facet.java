package de.quasiris.qsf.response;

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

    private Long count;

    private Long resultCount;

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

    @Override
    public String toString() {
        return "Facet{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", count=" + count +
                ", resultCount=" + resultCount +
                ", values=" + values +
                '}';
    }
}
