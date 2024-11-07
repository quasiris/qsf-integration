package com.quasiris.qsf.config;

import java.util.ArrayList;

public class QsfSearchConfigUtil {


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

    public static void initDisplayMapping(QsfSearchConfigDTO qsfSearchConfigDTO) {
        initDisplay(qsfSearchConfigDTO);
        if(qsfSearchConfigDTO.getDisplay().getMapping() == null) {
            qsfSearchConfigDTO.getDisplay().setMapping(new ArrayList<>());
        }
    }
}
