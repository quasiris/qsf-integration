package com.quasiris.qsf.query;

import java.io.Serializable;

public abstract class BaseSearchFilter implements Serializable {
    public abstract String getFilterClass();
}
