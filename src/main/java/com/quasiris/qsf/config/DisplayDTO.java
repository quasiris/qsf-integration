package com.quasiris.qsf.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayDTO {
    private List<DisplayMappingDTO> mapping;

    private Map<String, String> scoreMapping = new HashMap<>();

    public List<DisplayMappingDTO> getMapping() {
        return mapping;
    }

    public void setMapping(List<DisplayMappingDTO> mapping) {
        this.mapping = mapping;
    }

    public Map<String, String> getScoreMapping() {
        return scoreMapping;
    }

    public void setScoreMapping(Map<String, String> scoreMapping) {
        this.scoreMapping = scoreMapping;
    }
}
