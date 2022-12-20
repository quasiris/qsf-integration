package com.quasiris.qsf.pipeline.helper;

import com.quasiris.qsf.pipeline.Pipeline;
import com.quasiris.qsf.pipeline.filter.Filter;

public class ExecLocationIdHelper {

    public static String addIndexAndFilterId(String srcPrefix, int index, Filter filter){
        String retValue = addIndex(srcPrefix, index);
        retValue = addFilterId(retValue, filter);
        return retValue;
    }
    public static String addIndexAndPipelineId(String srcPrefix, int index, Pipeline filter){
        String retValue = addIndex(srcPrefix, index);
        retValue = addPipelineId(retValue, filter);
        return retValue;
    }
    public static String addFilterId(String srcPrefix, Filter filter){
        return srcPrefix + "->" + "(filter:" + filter.getId() + ")";
    }
    public static String addPipelineId(String srcPrefix, Pipeline pipeline){
        return srcPrefix + "->" + "(pipeline:" + pipeline.getId() + ")";
    }
    public static String addIndex(String srcPrefix, int index){
        return srcPrefix + "[" + index + "]";
    }
}
