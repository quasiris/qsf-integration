package com.quasiris.qsf.text;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Split the value
 */
public class Splitter {

    /**
     * Split the value to a set by comma, and trim the values.
     * @param value the value to split.
     * @return a set of the splitted values.
     */
    public static Set<String> splitToSet(String value) {
        return new HashSet<>(splitToList(value, ","));
    }

    /**
     * Split the value to a list by comma, and trim the values.
     * @param value the value to split.
     * @return a list of the splitted values.
     */
    public static List<String> splitToList(String value) {
        return splitToList(value, ",");
    }


    /**
     * Split the value to a list by the seperator, and trim the values.
     * @param value the value
     * @param seperator the seperator
     * @return a list of the splitted values.
     */
    public static List<String> splitToList(String value, String seperator) {
        List<String> ret = new ArrayList<>();
        if(Strings.isNullOrEmpty(value)) {
            return ret;
        }
        return Arrays.asList(value.split(Pattern.quote(seperator)));
    }
}
