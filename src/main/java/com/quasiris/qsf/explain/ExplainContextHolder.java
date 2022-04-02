package com.quasiris.qsf.explain;

final public class ExplainContextHolder {
    private static final ThreadLocal<ExplainContext> contextHolder = new ThreadLocal();


    public void clearContext() {
        contextHolder.remove();
    }
    public static ExplainContext getContext() {
        System.out.println("thread tbl: " + Thread.currentThread().getName());
        ExplainContext explainContext = contextHolder.get();
        if(explainContext == null) {
            contextHolder.set(new ExplainContext());
        }
        return contextHolder.get();
    }
    public ExplainContext createEmptyContext() {
        return new ExplainContext();
    }


}
