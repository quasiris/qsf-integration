package com.quasiris.qsf.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static void addInnerhitsGroupMapping(QsfSearchConfigDTO searchConfigDTO, String from, String to) {
        Map<String, List<String>> groupInnerhitsMapping =  searchConfigDTO.getDisplay().getGroupInnerhitsMapping();
        if(groupInnerhitsMapping == null) {
            groupInnerhitsMapping = new HashMap<>();
            searchConfigDTO.getDisplay().setGroupInnerhitsMapping(groupInnerhitsMapping);
        }
        List<String> mapping = groupInnerhitsMapping.get(from);
        if(mapping == null) {
            mapping = new ArrayList<>();
        }
        mapping.add(to);

        groupInnerhitsMapping.put(from, mapping);
    }
}
