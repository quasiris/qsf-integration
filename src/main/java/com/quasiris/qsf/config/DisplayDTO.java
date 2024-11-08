package com.quasiris.qsf.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayDTO {
    private List<DisplayMappingDTO> mapping;

    // mapping for innerhits to a field, if there are multiple inner hits, that belong to one field
    private Map<String, String> innerhitsMapping = new HashMap<>();

    private Map<String, List<String>> groupInnerhitsMapping;

    public List<DisplayMappingDTO> getMapping() {
        return mapping;
    }

    public void setMapping(List<DisplayMappingDTO> mapping) {
        this.mapping = mapping;
    }

    public Map<String, String> getInnerhitsMapping() {
        return innerhitsMapping;
    }

    public void setInnerhitsMapping(Map<String, String> innerhitsMapping) {
        this.innerhitsMapping = innerhitsMapping;
    }

    public Map<String, List<String>> getGroupInnerhitsMapping() {
        return groupInnerhitsMapping;
    }

    public void setGroupInnerhitsMapping(Map<String, List<String>> groupInnerhitsMapping) {
        this.groupInnerhitsMapping = groupInnerhitsMapping;
    }
}
