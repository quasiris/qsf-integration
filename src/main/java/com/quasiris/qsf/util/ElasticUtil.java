package com.quasiris.qsf.util;

/**
 * Created by mki on 7.7.18.
 */
public class ElasticUtil {

    // https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html
    public static final String[] ELASTIC_RESERVED_CHARS = {"\\","+","-","=","&&","||",">","<","!","(",")","{","}","[","]","^","\"","~","*","?",":","/"," OR "," AND "};
    public static final String[] ELASTIC_ESCAPED_CHARS = {"\\\\","\\+","\\-","\\=","\\&&","\\||","\\>","\\<","\\!","\\(","\\)","\\{","\\}","\\[","\\]","\\^","\\\"","\\~","\\*","\\?","\\:","\\/"," \\OR "," \\AND "};

    // https://www.elastic.co/guide/en/elasticsearch/reference/current/regexp-syntax.html
    // . ? + * | { } [ ] ( ) " \
    // # @ & < >  ~
    public static final String[] ELASTIC_REGEX_RESERVED_CHARS = {"\\",".","?","+","*","|","{","}","[","]","(",")","\"","#","@","&","<",">","~"};
    public static final String[] ELASTIC_REGEX_ESCAPED_CHARS = {"\\\\","\\.","\\?","\\+","\\*","\\|","\\{","\\}","\\[","\\]","\\(","\\)","\\\"","\\#","\\@","\\&","\\<","\\>","\\~"};

    public static String escape(String value) {
        return escape(value, ELASTIC_RESERVED_CHARS, ELASTIC_ESCAPED_CHARS);
    }

    public static String escapeRegex(String value) {
        return escape(value, ELASTIC_REGEX_RESERVED_CHARS, ELASTIC_REGEX_ESCAPED_CHARS);
    }


    private static String escape(String value, String[] metaCharacters, String[] metaCharactersEscaped) {
        if(value != null) {
            for (int i = 0; i < metaCharacters.length; i++) {
                if (value.contains(metaCharacters[i])) {
                    value = value.replace(metaCharacters[i], metaCharactersEscaped[i]);
                }
            }
        }
        return value;
    }
}
