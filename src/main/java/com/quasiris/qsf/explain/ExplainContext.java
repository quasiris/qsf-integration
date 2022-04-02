package com.quasiris.qsf.explain;

import java.util.ArrayList;

public class ExplainContext {

    private Explain current;

    private Explain root;

    private boolean explain = false;


    public Explain pipeline(String id) {
        if(!explain) {
            return new Explain();
        }
        Explain explain = new Explain();
        explain.setId(id);
        explain.setName(id);
        explain.setType("pipeline");
        explain.setDataType(ExplainDataType.STRING);
        this.root = explain;
        return explain;
    }
    public Explain filter(String id) {
        if(!explain) {
            return new Explain();
        }
        Explain explain = new Explain();
        explain.setId(id);
        explain.setName(id);
        explain.setType("filter");
        explain.setDataType(ExplainDataType.STRING);
        addChild(explain);
        this.current = explain;
        return explain;
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

    public void addChild(Explain explain) {
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
