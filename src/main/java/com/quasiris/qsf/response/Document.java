package com.quasiris.qsf.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mki on 11.11.16.
 */
public class Document {

    private String id;

    private Map<String, Object> document = new HashMap<>();

    public Document() {
    }

    public Document(String id) {
        this.id = id;
    }

    public Document(String id, Map<String, Object> document) {
        this.id = id;
        this.document = document;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getDocument() {
        return document;
    }

    public void setDocument(Map<String, Object> document) {
        this.document = document;
    }

    public String getFieldValue(String fieldName) {
        Object value = document.get(fieldName);
        if(value == null) {
            return null;
        }
        if(value instanceof List) {
            List values = (List) value;
            if(((List) value).size() > 0) {
                return String.valueOf(values.get(0));
            } else {
                return null;
            }
        }
        return String.valueOf(value);
    }

    public List<String> getValues(String fieldName) {
        Object value = getFieldValueAsObject(fieldName);
        List<String> ret = new ArrayList<>();
        if(value == null) {
            return ret;
        }

        if(value instanceof List) {
            List values = (List) value;
            for(Object v : values) {
                ret.add(v.toString());
            }
        } else {
            ret.add(value.toString());
        }
        return ret;
    }


    public Object getFieldValueAsObject(String fieldName) {
        return document.get(fieldName);
    }

    public Long getFieldValueAsLong(String fieldName) {
        String value = getFieldValue(fieldName);
        if(value == null) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    public Integer getFieldValueAsInteger(String fieldName) {
        String value = getFieldValue(fieldName);
        if(value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    public String getFieldValue(String fieldName, String defaultValue) {
        String value = getFieldValue(fieldName);
        if(value == null) {
            return defaultValue;
        }
        return value;
    }

    public int getFieldCount() {
        return document.keySet().size();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Document{");
        if(id != null) {
            builder.append("id="+id+", ");
        }
        builder.append("document="+document);
        builder.append("}");

        return builder.toString();
    }

    public void addValue(String name, Object value) {
        Object values = this.getDocument().get(name);
        if(values == null) {
            this.getDocument().put(name, value);
        } else if(values instanceof List) {
            ((List) values).add(value);
        } else {
            List list = new ArrayList();
            list.add(values);
            list.add(value);
            this.getDocument().put(name, list);
        }
    }

    public void setValue(String name, Object value) {
        this.getDocument().put(name, value);
    }

    public void setValueIfNotNull(String name, Object value) {
        if(value == null) {
            return;
        }
        setValue(name, value);
    }
}
