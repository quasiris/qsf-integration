package com.quasiris.qsf.config;

import java.util.*;

public class QsfSearchConfigUtil {


    public static QsfSearchConfigDTO initSearchConfig() {
        QsfSearchConfigDTO qsfSearchConfigDTO = new QsfSearchConfigDTO();
        initDisplay(qsfSearchConfigDTO);
        initFacet(qsfSearchConfigDTO);
        initFilter(qsfSearchConfigDTO);
        initSort(qsfSearchConfigDTO);
        initPaging(qsfSearchConfigDTO);
        initVariant(qsfSearchConfigDTO);
        return qsfSearchConfigDTO;
    }


    public static List<DisplayMappingDTO> getDisplayMapping(QsfSearchConfigDTO qsfSearchConfigDTO) {
        if(hasDisplayMapping(qsfSearchConfigDTO)) {
            return qsfSearchConfigDTO.getDisplay().getMapping();
        }

        return null;


    }
    public static boolean hasDisplayMapping(QsfSearchConfigDTO qsfSearchConfigDTO) {
        if(qsfSearchConfigDTO == null) {
            return false;
        }
        if(qsfSearchConfigDTO.getDisplay() == null) {
            return false;
        }
        if(qsfSearchConfigDTO.getDisplay().getMapping() == null) {
            return false;
        }
        return true;
    }

    public static void initDisplay(QsfSearchConfigDTO qsfSearchConfigDTO) {
        if(qsfSearchConfigDTO.getDisplay() == null) {
            qsfSearchConfigDTO.setDisplay(new DisplayDTO());
        }
    }


    public static void initFacet(QsfSearchConfigDTO qsfSearchConfigDTO) {

        if(qsfSearchConfigDTO.getFacet() == null) {
            qsfSearchConfigDTO.setFacet(new FacetDTO());
        }
        if(qsfSearchConfigDTO.getFacet().getFacets() == null) {
            qsfSearchConfigDTO.getFacet().setFacets(new ArrayList<>());
        }
    }

    public static void initFilter(QsfSearchConfigDTO qsfSearchConfigDTO) {

        if(qsfSearchConfigDTO.getFilter() == null) {
            qsfSearchConfigDTO.setFilter(new FilterDTO());
        }

        if(qsfSearchConfigDTO.getFilter().getMultiSelectFilter() == null) {
            qsfSearchConfigDTO.getFilter().setMultiSelectFilter(false);
        }
        if(qsfSearchConfigDTO.getFilter().getFilterRules() == null) {
            qsfSearchConfigDTO.getFilter().setFilterRules(new HashMap<>());
        }
        if(qsfSearchConfigDTO.getFilter().getFilterMapping() == null) {
            qsfSearchConfigDTO.getFilter().setFilterMapping(new HashMap<>());
        }
        if(qsfSearchConfigDTO.getFilter().getDefinedRangeFilterMapping() == null) {
            qsfSearchConfigDTO.getFilter().setDefinedRangeFilterMapping(new HashMap<>());
        }

    }

    public static void initDisplayMapping(QsfSearchConfigDTO qsfSearchConfigDTO) {
        initDisplay(qsfSearchConfigDTO);
        if(qsfSearchConfigDTO.getDisplay().getMapping() == null) {
            qsfSearchConfigDTO.getDisplay().setMapping(new ArrayList<>());
        }
    }

    public static void initSort(QsfSearchConfigDTO qsfSearchConfigDTO) {

        if(qsfSearchConfigDTO.getSort() == null) {
            qsfSearchConfigDTO.setSort(new SortDTO());
        }
    }

    public static void initPaging(QsfSearchConfigDTO qsfSearchConfigDTO) {

        if(qsfSearchConfigDTO.getPaging() == null) {
            qsfSearchConfigDTO.setPaging(new PagingDTO());
        }
    }
    public static void initVariant(QsfSearchConfigDTO qsfSearchConfigDTO) {

        if(qsfSearchConfigDTO.getVariant() == null) {
            qsfSearchConfigDTO.setVariant(new VariantDTO());
        }
    }

    public static void addVariantMapping(QsfSearchConfigDTO qsfSearchConfigDTO, String from, String to) {
        initVariant(qsfSearchConfigDTO);

        List<DisplayMappingDTO> variantMapping = qsfSearchConfigDTO.getVariant().getMapping();
        if(variantMapping == null) {
            variantMapping = new ArrayList<>();
            qsfSearchConfigDTO.getVariant().setMapping(variantMapping);
        }
        DisplayMappingDTO displayMappingDTO = new DisplayMappingDTO();
        displayMappingDTO.setFrom(from);
        displayMappingDTO.setTo(to);
        variantMapping.add(displayMappingDTO);
    }

    public static void addVariantOptions(QsfSearchConfigDTO qsfSearchConfigDTO, String option) {
        initVariant(qsfSearchConfigDTO);
        Set<String> options = qsfSearchConfigDTO.getVariant().getOptions();
        if(options == null) {
            options = new HashSet<>();
            qsfSearchConfigDTO.getVariant().setOptions(options);
        }
        options.add(option);
    }
}
