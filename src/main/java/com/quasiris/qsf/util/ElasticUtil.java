package com.quasiris.qsf.util;

/**
 * Created by mki on 7.7.18.
 */
public class ElasticUtil {

    // https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html
    public static final String[] ELASTIC_RESERVED_CHARS = {"\\","+","-","=","&&","||",">","<","!","(",")","{","}","[","]","^","\"","~","*","?",":","/","OR","AND"};

    public static String escape(String value) {
        return escape(value, ELASTIC_RESERVED_CHARS);
    }

    public static String escape(String value, String[] metaCharacters) {
        for (int i = 0 ; i < metaCharacters.length ; i++){
            if(value.contains(metaCharacters[i])){
                value = value.replace(metaCharacters[i],"\\"+metaCharacters[i]);
            }

        }
        return value;
    }
}
