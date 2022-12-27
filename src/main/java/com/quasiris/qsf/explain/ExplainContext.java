package com.quasiris.qsf.explain;

import java.util.ArrayList;

public class ExplainContext {

    private Explain current;

    private Explain root;

    private boolean explain = false;


    public Explain<ExplainPipeline> pipeline(String id) {
        Explain<ExplainPipeline> explainPipeline = new Explain();
        explainPipeline.setExplainObject(new ExplainPipeline());
        if(!explain) {
            return explainPipeline;
        }

        explainPipeline.setId(id);
        explainPipeline.setName(id);
        explainPipeline.setType("pipeline");
        explainPipeline.setDataType(ExplainDataType.STRING);
        this.root = explainPipeline;
        return explainPipeline;
    }
    public Explain<ExplainFilter> filter(String id) {
        Explain<ExplainFilter> explainFilter = new Explain();
        explainFilter.setExplainObject(new ExplainFilter());
        if(!explain) {
            return explainFilter;
        }
        explainFilter.setId(id);
        explainFilter.setName(id);
        explainFilter.setType("filter");
        explainFilter.setDataType(ExplainDataType.STRING);
        addChild(explainFilter);
        this.current = explainFilter;
        return explainFilter;
    }

    public void explain(String id, String value) {
        explain("u", id, value);
    }

    public void explain(String type, String id, String value) {
        if(!explain) {
            return;
        }
        Explain explain = new Explain();
        explain.setId(id);
        explain.setName(id);
        explain.setType(type);
        explain.setExplainObject(value);
        explain.setDataType(ExplainDataType.STRING);
        addChild(explain);
    }

    public void explainJson(String id, Object value) {
        if(!explain) {
            return;
        }
        Explain explain = new Explain();
        explain.setId(id);
        explain.setName(id);
        explain.setExplainObject(value);
        explain.setDataType(ExplainDataType.JSON);
        addChild(explain);
    }

    public synchronized void addChild(Explain explain) {
        if(!this.explain) {
            return;
        }
        if(getCurrent().getChildren() == null) {
            getCurrent().setChildren(new ArrayList<>());
        }
        getCurrent().getChildren().add(explain);
    }

    public Explain getCurrent() {
        if(!explain) {
            return null;
        }
        if(current == null) {
            current = getRoot();
        }
        return current;
    }

    public void setCurrent(Explain current) {
        this.current = current;
    }

    public Explain getRoot() {
        if(!explain) {
            return null;
        }
        if(root == null) {
            root = new Explain();
            root.setId("root");
            root.setName("root");
        }
        return root;
    }

    public void setRoot(Explain root) {
        this.root = root;
    }

    public boolean isExplain() {
        return explain;
    }

    public void setExplain(boolean explain) {
        this.explain = explain;
    }
}
