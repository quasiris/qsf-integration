package de.quasiris.qsf.response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mki on 11.11.16.
 */
public class Document {

    private Map<String, Object> document = new HashMap<>();

    public Map<String, Object> getDocument() {
        return document;
    }

    public void setDocument(Map<String, Object> document) {
        this.document = document;
    }

    public String getFieldValue(String fieldName) {
        Object value = document.get(fieldName);
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

    public int getFieldCount() {
        return document.keySet().size();
    }

    @Override
    public String toString() {
        return "Document{" +
                "document=" + document +
                '}';
    }
}
