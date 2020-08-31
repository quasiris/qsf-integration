package com.quasiris.qsf.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;

public class JsonBuilder {


    private ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);;

    private JsonNode root;
    private JsonNode current;
    private Stack<JsonNode> stash = new Stack<>();
    private Map<String, JsonNode> stashMap = new HashMap<>();


    public JsonBuilder root() throws JsonBuilderException {
        this.current = root;
        return this;
    }

    public JsonBuilder paths(String paths) throws JsonBuilderException {
        try {
            String[] pathesSplit = paths.split(Pattern.quote("/"));
            for (String path : pathesSplit) {
                path(path);
            }
            return this;
        } catch (JsonBuilderException e) {
            throw new JsonBuilderException("The path " + paths + " does not exists.");
        }
    }

    public JsonBuilder path(String path) throws JsonBuilderException {
        JsonNode jsonNode = current.get(path);
        if(jsonNode == null) {
            throw new JsonBuilderException("The path " + path + " does not exists.");
        }
        current = jsonNode;
        return this;
    }

    public JsonBuilder stash(String key) throws JsonBuilderException {
        this.stashMap.put(key, current);
        return this;
    }

    public JsonBuilder unstash(String key) throws JsonBuilderException {
        this.current = this.stashMap.get(key);
        return this;
    }

    public JsonBuilder stash() throws JsonBuilderException {
        this.stash.push(current);
        return this;
    }
    public JsonBuilder unstash() throws JsonBuilderException {
        this.current = this.stash.peek();
        return this;
    }

    public JsonBuilder addPojo(Object pojo)  throws JsonBuilderException {
        JsonNode jsonNode = mapper.valueToTree(pojo);
        return addJson(jsonNode);
    }

    public JsonBuilder pojo(Object pojo)  throws JsonBuilderException {
        JsonNode jsonNode = mapper.valueToTree(pojo);
        if(root == null) {
            root = jsonNode;
            current = root;
            return this;
        }

        String key = jsonNode.fieldNames().next();
        return json(key, jsonNode.get(key));
    }

    public JsonBuilder pojo(String fieldName, Object pojo)  throws JsonBuilderException {
        JsonNode jsonNode = mapper.valueToTree(pojo);
        return json(fieldName, jsonNode);
    }

    public JsonBuilder json(JsonNode jsonNode)  throws JsonBuilderException {
        String key = jsonNode.fieldNames().next();
        return json(key, jsonNode.get(key));
    }

    public JsonBuilder json(String fieldName, JsonNode jsonNode)  throws JsonBuilderException {
        if(root == null) {
            root = mapper.createObjectNode();
            current = root;
        }
        if(current.isObject()) {
            ((ObjectNode) current).set(fieldName, jsonNode);
        }
        if(current.isArray()) {
            throw new JsonBuilderException("The current node is an array. An object node is expected.");
        }
        return this;
    }

    public JsonBuilder addJson(JsonNode jsonNode)  throws JsonBuilderException {
        if(root == null) {
            root = mapper.createArrayNode();
            current = root;
        }
        if(current.isObject()) {
            throw new JsonBuilderException("The current node is an object. An array node is expected.");
        }
        if(current.isArray()) {
            ((ArrayNode) current).add(jsonNode);
        }
        return this;
    }

    public JsonBuilder addString(String string)  throws JsonBuilderException {
        try {
            JsonNode jsonNode = mapper.readTree(string);
            addJson(jsonNode);
        } catch (IOException e) {
            throw new JsonBuilderException(e);
        }
        return this;
    }

    public JsonBuilder string( String string)  throws JsonBuilderException {
        try {
            JsonNode jsonNode = mapper.readTree(string);
            this.root = jsonNode;
            this.current = jsonNode;
            return this;
        } catch (IOException e) {
            throw new JsonBuilderException(e);
        }
    }


    public JsonBuilder string(String fieldName, String string)  throws JsonBuilderException {
        if(root == null) {
            root = mapper.createObjectNode();
            current = root;
        }
        try {
            JsonNode jsonNode = mapper.readTree(string);
            return json(fieldName, jsonNode);
        } catch (IOException e) {
            throw new JsonBuilderException(e);
        }
    }

    public JsonBuilder object(String fieldName, Object value) throws JsonBuilderException {

        if(value == null || fieldName == null) {
            return this;
        }
        JsonNode jsonNode = mapper.valueToTree(value);
        return json(fieldName, jsonNode);
    }

    public JsonBuilder addValue( Object value) throws JsonBuilderException {
        JsonNode jsonNode = mapper.valueToTree(value);
        addJson(jsonNode);
        return this;
    }

    public JsonBuilder object(String fieldName) throws JsonBuilderException {

        if(root == null) {
            this.root = mapper.createObjectNode();
            this.current = this.root;
        }
        ObjectNode objectNode = mapper.createObjectNode();

        if(current.isArray()) {
            throw new JsonBuilderException("The current node is an array. An object node is expected.");
        }
        if(current.isObject()) {
            ((ObjectNode) current).set(fieldName, objectNode);
        }
        current = objectNode;
        return this;
    }


    public JsonBuilder object() throws JsonBuilderException {
        ObjectNode objectNode = mapper.createObjectNode();
        if(root == null) {
            this.root = objectNode;
            this.current = objectNode;
            return this;
        }

        if(current.isArray()) {
            ((ArrayNode) current).add(objectNode);
        }
        if(current.isObject()) {
            throw new JsonBuilderException("The current node is an object. A array node is expected.");
        }
        current = objectNode;
        return this;
    }


    public JsonBuilder array() throws JsonBuilderException {
        if(root != null) {
            throw new JsonBuilderException("Can not add a array node.");
        }
        ArrayNode arrayNode = mapper.createArrayNode();
        this.root = arrayNode;
        this.current = arrayNode;
        return this;
    }


    public JsonBuilder array(String fieldName) throws JsonBuilderException {
        if(root == null) {
            root = mapper.createObjectNode();
            current = root;
        }

        ArrayNode arrayNode = mapper.createArrayNode();
        if(current.isObject()) {
            ((ObjectNode) current).set(fieldName, arrayNode);
        } else {
            throw new JsonBuilderException("The current node is an array. A object node is expected.");
        }
        current = arrayNode;
        return this;
    }

    public JsonBuilder replace(Map<String, Object> valueMap) throws JsonBuilderException {
        JsonSubstitutor jsonSubstitutor = new JsonSubstitutor(valueMap);
        this.root = jsonSubstitutor.replace(this.root);
        this.current = this.root;
        return this;
    }

    public String writeAsString() throws JsonProcessingException {
        return mapper.writeValueAsString(this.root);
    }

    public JsonNode getCurrent() {
        return current;
    }

    public JsonNode get() {
        return root;
    }
}
