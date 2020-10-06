package com.quasiris.qsf.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonSubstitutor {

    private final Map<String, Object> valueMap;

    public JsonSubstitutor(Map<String, Object> valueMap) {
        this.valueMap = valueMap;
    }
    
    public JsonNode replace(JsonNode node) throws JsonBuilderException {
        if(this.valueMap == null) {
            return node;
        }
        Iterator<Map.Entry<String, JsonNode>> it = node.fields();
        List<String> removeFromNode = new ArrayList<>();
        Map<String, JsonNode> addToNode = new HashMap();
        List<JsonNode> addToArrayNode = new ArrayList<>();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> next = it.next();
            if(next.getKey().startsWith("$")) {
                JsonNode oldNode = node.get(next.getKey());
                Object value = valueMap.get(next.getKey());
                if(value instanceof EmptyNode) {
                    removeFromNode.add(next.getKey());
                } else if(value instanceof ObjectNode) {
                    ObjectNode objectNode = (ObjectNode) value;
                    if(!objectNode.fields().hasNext()) {
                        throw new JsonBuilderException("The object for " + next.getKey() + " must contain at least one field.");
                    }
                    Map.Entry<String, JsonNode> field = objectNode.fields().next();
                    addToNode.put(field.getKey(), field.getValue());
                    removeFromNode.add(next.getKey());
                } else if(value != null) {
                    addToNode.put(value.toString(), oldNode);
                    removeFromNode.add(next.getKey());
                }
            }

            if(next.getValue().isValueNode() &&
                    next.getValue().textValue() != null &&
                    next.getValue().textValue().startsWith("$")) {
                ObjectMapper mapper = new ObjectMapper();
                String key = next.getValue().textValue();
                Object value = valueMap.get(key);
                if(value != null) {
                    JsonNode newValue = mapper.valueToTree(value);
                    next.setValue(newValue);
                }
            } else {
                replace(next.getValue());
            }
        }

        if(node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            Iterator<JsonNode> arrayIterator = arrayNode.iterator();
            while (arrayIterator.hasNext()) {
                JsonNode next = arrayIterator.next();
                if (next.isValueNode() && next.textValue() != null && next.textValue().startsWith("$")) {
                    ObjectMapper mapper = new ObjectMapper();
                    String key = next.textValue();
                    Object value = valueMap.get(key);
                    if (value != null) {
                        JsonNode newValue = mapper.valueToTree(value);
                        addToArrayNode.addAll(jsonNodeToList(newValue));
                    }
                } else {
                    next = replace(next);
                    addToArrayNode.add(next);
                }
            }
            arrayNode.removeAll();
            for(JsonNode jsonNode : addToArrayNode) {
                arrayNode.add(jsonNode);
            }
        }

        for(String remove : removeFromNode) {
            ((ObjectNode) node).remove(remove);
        }
        for(Map.Entry<String, JsonNode> entry : addToNode.entrySet()) {
            ((ObjectNode) node).set(entry.getKey(), entry.getValue());
        }
        return node;
    }

    private List<JsonNode> jsonNodeToList(JsonNode jsonNode) {
        List<JsonNode> ret = new ArrayList<>();
        if(jsonNode.isArray()) {
            ArrayNode newValueArray = (ArrayNode) jsonNode;
            Iterator<JsonNode> itt = newValueArray.iterator();
            while (itt.hasNext()) {
                ret.add(itt.next());
            }
        } else {
            ret.add(jsonNode);
        }
        return  ret;
    }
}
