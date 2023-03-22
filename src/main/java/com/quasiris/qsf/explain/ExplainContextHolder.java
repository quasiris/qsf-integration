package com.quasiris.qsf.explain;

final public class ExplainContextHolder {
    private static final ThreadLocal<ExplainContext> contextHolder = new ThreadLocal();

    public static void clearContext() {
        ExplainContext actual = contextHolder.get();
        if (actual != null && actual.isClearOnNewPipeline()) {
            contextHolder.remove();
        }
    }

    public static void forceClearContext() {
        contextHolder.remove();
    }

    public static ExplainContext getContext() {
        ExplainContext explainContext = contextHolder.get();
        if (explainContext == null) {
            contextHolder.set(new ExplainContext());
        }
        return contextHolder.get();
    }

    public ExplainContext createEmptyContext() {
        return new ExplainContext();
    }


}
