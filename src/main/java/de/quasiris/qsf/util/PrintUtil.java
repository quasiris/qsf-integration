package de.quasiris.qsf.util;

import com.google.common.base.Joiner;

import java.util.Map;

/**
 * Created by mki on 30.12.17.
 */
public class PrintUtil {


    public static void printKeyValue(StringBuilder printer, String indent, String key, Object value) {
        printer.append(indent).append("\t").append(key).append(": ").append(getValue(value)).append("\n");
    }

    public static void printMap(StringBuilder printer, String indent, String key, Map<String, ?> map) {
        indent = indent + "\t";
        printer.append(indent).append(key).append(": \n");
        for(Map.Entry<String, ?> fieldMappingEntry : map.entrySet()) {
            String value = getValue(fieldMappingEntry.getValue());
            PrintUtil.printKeyValue(printer, indent, fieldMappingEntry.getKey(), value);
        }
    }

    public static String getValue(Object value) {
        String ret = null;
        if(value instanceof Iterable) {
            Iterable iterableValue = (Iterable) value;
            ret = Joiner.on(",").join(iterableValue);
        } else {
            ret = String.valueOf(value);
        }
        return ret;
    }
}
