package com.quasiris.qsf.pipeline.filter.mapper.color;

import com.quasiris.qsf.commons.util.JsonUtil;
import com.quasiris.qsf.commons.util.ParameterUtils;
import com.quasiris.qsf.dto.config.ColorPickerFacetConfigDTO;
import com.quasiris.qsf.dto.response.FacetValue;
import com.quasiris.qsf.json.JsonBuilder;
import com.quasiris.qsf.pipeline.filter.mapper.DefaultFacetFilterMapper;
import com.quasiris.qsf.pipeline.filter.mapper.FacetFilterMapper;

import java.util.HashMap;
import java.util.Map;

public class ColorFacetFilterMapper extends DefaultFacetFilterMapper implements FacetFilterMapper {


    private ColorPickerFacetConfigDTO colorMappingDTO;

    public ColorFacetFilterMapper(Map<String, Object> parameters) {
        colorMappingDTO = loadColorPickerFacetConfigDTO(parameters);
    }

    @Override
    public void map(FacetValue value) {
        super.map(value);
        String color = value.getValue().toString();
        String hex = colorMappingDTO.getColorMappings().get(color.toLowerCase().trim());
        if(hex == null) {
            hex = "#FFFFFF";
        }


        Map<String, Object> properties  =value.getProperties();
        if(properties == null) {
            properties = new HashMap<>();
            value.setProperties(properties);
        }

        properties.put("color", hex);
    }

    public static ColorPickerFacetConfigDTO loadColorPickerFacetConfigDTO(Map<String, Object> parameters) {
        try {

            ColorPickerFacetConfigDTO colorPickerFacetDefaultConfig = JsonBuilder.create().
                    classpath("com/quasiris/qsf/facet/default-color-picker-facet-config.json").
                    get(ColorPickerFacetConfigDTO.class);

            ColorPickerFacetConfigDTO colorPickerFacetConfig = ParameterUtils.getParameter(parameters, "config", null, ColorPickerFacetConfigDTO.class);
            if (colorPickerFacetConfig != null && colorPickerFacetConfig.getColorMappings() != null) {
                colorPickerFacetDefaultConfig.getColorMappings().putAll(colorPickerFacetConfig.getColorMappings());
            }
            return colorPickerFacetDefaultConfig;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
