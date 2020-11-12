package com.quasiris.qsf.test.converter;

import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Convert the string 'null' to a null object
 */
public class NullValueConverter extends SimpleArgumentConverter {
    @Override
    protected Object convert(Object source, Class<?> targetType) throws ArgumentConversionException {
        if(source == null) {
            return null;
        }
        if("null".equals(source)) {
            return null;
        }
        return source;
    }
}