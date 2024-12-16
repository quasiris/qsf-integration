package com.quasiris.qsf.mapping;

import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.query.SearchFilter;

import java.util.*;

public class ParameterMapper {


    private List<ParameterMappingDTO> mappings;

    private Map<String, ? super Object> data = new HashMap<>();

    private Map<String, ? super Object> mappedData = null;

    private PipelineContainer pipelineContainer;

    public ParameterMapper(SimpleParameterMappingDTO mapping, PipelineContainer pipelineContainer) {
        initData(pipelineContainer);
        this.mappings = map2list(mapping);
    }
    public ParameterMapper(List<ParameterMappingDTO> mappings, PipelineContainer pipelineContainer) {
        initData(pipelineContainer);
        this.mappings = mappings;
    }

    public ParameterMapper(Map<String, ParameterMappingDTO> mappings, PipelineContainer pipelineContainer) {
        initData(pipelineContainer);
        this.mappings = map2list(mappings);
    }

    private void initData(PipelineContainer pipelineContainer) {
        data.put("searchQuery", pipelineContainer.getSearchQuery());
        data.put("searchResults", pipelineContainer.getSearchResults());
        data.put("context", pipelineContainer.getContext());
        data.put("custom", new HashMap<>());

        this.pipelineContainer = pipelineContainer;
    }

    public List<ParameterMappingDTO> map2list(Map<String, ParameterMappingDTO> map) {
        List<ParameterMappingDTO> ret = new ArrayList<>();
        for(Map.Entry<String, ParameterMappingDTO> entry : map.entrySet()) {
            entry.getValue().setTo(entry.getKey());
            ret.add(entry.getValue());
        }
        return ret;
    }
    public List<ParameterMappingDTO> map2list(SimpleParameterMappingDTO mapping) {
        List<ParameterMappingDTO> ret = new ArrayList<>();
        for(Map.Entry<String, String> entry : mapping.getMapping().entrySet()) {
            ParameterMappingDTO dto = new ParameterMappingDTO();
            dto.setTo(entry.getKey());
            dto.setFrom(entry.getValue());
            ret.add(dto);
        }
        return ret;
    }


    private Map<String, ? super Object> map() {
        if(mappedData == null) {
            Map<String, ? super Object> ret = new HashMap<>();
            ret.putAll(getCustomData());
            for (ParameterMappingDTO mapping : mappings) {
                Object value = getValue(mapping.getFrom());
                if(value != null) {
                    ret.put(mapping.getTo(), value);
                }
            }
            mappedData = ret;
        }
        return mappedData;
    }

    public Object get(String parameter) {
        return map().get(parameter);
    }

    public Object get(String parameter, Object defaultValue) {
        Object value = get(parameter);
        if(value == null) {
            return defaultValue;
        }
        return value;
    }

    public List<Object> getList(String parameter) {
        Object value = get(parameter);
        if(value == null) {
            return null;
        }
        if(value instanceof List) {
            return (List) value;
        }
        List<Object> l = new ArrayList<>();
        l.add(value);
        return l;
    }
    public List<Object> getList(String parameter, List<Object> defaultValue) {
        List<Object> value = getList(parameter);
        if(value == null) {
            return defaultValue;
        }
        return value;
    }

    public Map<String, ? super Object> getMappedData() {
        return  map();
    }

    Object getValue(String from) {
        if("searchQuery.q".equals(from)) {
            return pipelineContainer.getSearchQuery().getQ();
        }
        if("searchQuery.rows".equals(from)) {
            return pipelineContainer.getSearchQuery().getRows();
        }
        if(from.startsWith("searchQuery.filters")) {
            if(!(from.endsWith(".value") || from.endsWith(".values"))) {
                from = from + ".value";
            }
            String[] splitted = from.split("\\.");
            String filterId = String.join(".", Arrays.copyOfRange(splitted, 2, splitted.length-1));
            SearchFilter searchFilter = pipelineContainer.getSearchQuery().getSearchFilterById(filterId);
            if(searchFilter != null) {

                String filterObject = splitted[splitted.length-1];

                if("value".startsWith(filterObject)) {
                    return searchFilter.getValues().get(0);
                }
                if("values".startsWith(filterObject)) {
                    return searchFilter.getValues();
                }
                return searchFilter.getValues().get(0);
            }
        }
        if(from.startsWith("searchQuery.parameters")) {
            String[] splitted = from.split("\\.");
            String parameterId = String.join(".", Arrays.copyOfRange(splitted, 2, splitted.length));
            if(pipelineContainer.getSearchQuery().getParameters() != null) {
                Object value = pipelineContainer.getSearchQuery().getParameters().get(parameterId);
                return value;
            } else {
                return null;
            }
        }
        if(from.startsWith("context.")) {
            String[] splitted = from.split("\\.");
            String contextId = String.join(".", Arrays.copyOfRange(splitted, 1, splitted.length));
            if(pipelineContainer.getContext() != null) {
                Object value = pipelineContainer.getContext().get(contextId);
                return value;
            } else {
                return null;
            }
        }
        if(from.startsWith("custom.")) {
            String[] splitted = from.split("\\.");
            String contextId = String.join(".", Arrays.copyOfRange(splitted, 1, splitted.length));
            if(pipelineContainer.getContext() != null) {
                Object value = pipelineContainer.getContext().get(contextId);
                return value;
            } else {
                return null;
            }
        }
        return null;
    }

    public Map<String, Object> getCustomData() {
        Map<String, Object> custom = (Map<String, Object>) data.get("custom");
        if(custom == null) {
            custom = new HashMap<>();
        }
        return custom;
    }

    public void addCustomData(String key, Object value) {
        Map<String, Object> custom = getCustomData();
        custom.put(key, value);
    }

    public void addCustomData(Map<String, Object> data) {
        Map<String, Object> custom = getCustomData();
        custom.putAll(data);
    }
}
