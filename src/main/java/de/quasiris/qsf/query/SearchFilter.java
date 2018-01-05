package de.quasiris.qsf.query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mki on 11.11.16.
 */
public class SearchFilter<T> {

    private Operator operator = Operator.OR;

    private boolean exclude = true;

    private String id;

    private String name;

    private List<T> values = new ArrayList<T>();


    public List<T> getValues() {
        return values;
    }

    public void setValues(List<T> values) {
        this.values = values;
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

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public boolean isExclude() {
        return exclude;
    }

    public void setExclude(boolean exclude) {
        this.exclude = exclude;
    }

    @Override
    public String toString() {
        return "SearchFilter{" +
                "operator=" + operator +
                ", exclude=" + exclude +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", values=" + values +
                '}';
    }
}
