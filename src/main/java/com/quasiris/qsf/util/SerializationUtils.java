package com.quasiris.qsf.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SerializationUtils {
    public static <T extends Serializable> T deepCopy(T obj) {
        return org.apache.commons.lang3.SerializationUtils.clone(obj);
    }

    public static <T extends Serializable> List<T> deepCopyList(List<T> filters) {
        List<T> copy = new ArrayList<>();
        for (T filter : filters) {
            T clone = org.apache.commons.lang3.SerializationUtils.clone(filter);
            copy.add(clone);
        }
        return copy;
    }
}
