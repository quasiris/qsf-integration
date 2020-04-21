package com.quasiris.qsf.pipeline.filter.elastic;

public class Profiles {

    public static String analyze() {
        return "classpath://com/quasiris/qsf/elastic/profiles/field-analyze-profile.json";
    }

    public static String matchAll() {
        return "classpath://com/quasiris/qsf/elastic/profiles/match-all-profile.json";
    }

    public static String queryString() {
        return "classpath://com/quasiris/qsf/elastic/profiles/query-string-profile.json";
    }


}
