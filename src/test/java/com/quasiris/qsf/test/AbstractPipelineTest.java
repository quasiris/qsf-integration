package com.quasiris.qsf.test;

import com.quasiris.qsf.util.JsonUtil;

/**
 * Created by mki on 25.11.17.
 */
public class AbstractPipelineTest {


    public void print(Object object) {
        try {
            System.out.println(JsonUtil.toPrettyString(object));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
