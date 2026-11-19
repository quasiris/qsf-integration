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

    /**
     * Same as {@link #queryString()}, but the query is escaped for the lucene query syntax.
     * Reserved characters like / or : are searched literally instead of beeing interpreted.
     *
     * @return the profile
     */
    public static String queryStringEscaped() {
        return "classpath://com/quasiris/qsf/elastic/profiles/query-string-escaped-profile.json";
    }


}
