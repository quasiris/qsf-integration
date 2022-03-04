package com.quasiris.qsf.pipeline.filter.elastic.spellcheck;

import java.util.ArrayList;
import java.util.List;

public enum SpellCheckTokenType {


    GENERATED("generated"),
    MANAGED("managed"),
    VARIANT("variant"),
    UNKNOWN("unknown"),
    IGNORED("ignored"),
    CORRECT("correct"),
    EQUALS("equals"),
    MISSPELLING("misspelling");

    SpellCheckTokenType(String code) {
        this.code = code;
    }

    private String code;


    public String getCode() {
        return code;
    }

    public static SpellCheckTokenType create(String code) {
        return valueOf(code.toUpperCase());
    }
    public static List<SpellCheckTokenType> creates(List<String> codes) {
        List<SpellCheckTokenType> ret = new ArrayList<>();
        for(String code: codes) {
            ret.add(create(code));
        }
        return ret;
    }

    public boolean matchesOneOf(List<SpellCheckTokenType> types) {
        for(SpellCheckTokenType type: types) {
            if(this == type) {
                return true;
            }
        }
        return false;
    }
    public static boolean matchesOneOf(List<SpellCheckTokenType> left, SpellCheckTokenType... right) {
        for(SpellCheckTokenType one : right) {
            if(one.matchesOneOf(left)) {
                return true;
            }
        }
        return false;
    }

}
