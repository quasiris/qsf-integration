package com.quasiris.qsf.pipeline.filter.elastic.spellcheck;

public class SpellcheckUtils {

    public static boolean fuzzyEquals(String left, String right) {
        if(left == null && right == null) {
            return true;
        }
        if(left == null || right == null) {
            return false;
        }
        left = normalize(left);
        right = normalize(right);
        return left.equals(right);
    }

    public static String normalize(String value) {
        if(value == null) {
            return null;
        }
        value = value.toLowerCase();
        value = value.trim();
        return value;
    }
}
