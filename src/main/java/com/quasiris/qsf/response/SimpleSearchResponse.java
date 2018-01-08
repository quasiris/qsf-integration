package com.quasiris.qsf.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mki on 03.12.17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimpleSearchResponse extends HashMap<String,Map<String, Object>> {


}
