package com.quasiris.qsf.explain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

public class ExplainContext {

    private static final Logger log = LoggerFactory.getLogger(ExplainContext.class);
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

    public void explainThrowable(String id, Throwable throwable) {
        if (!explain) {
            return;
        }
        ExplainThrowable explainThrowable = new ExplainThrowable();
        explainThrowable.setStackTrace(getStackTrace(throwable));
        explainThrowable.setMessage(throwable.getMessage());
        Explain explain = new Explain();
        explain.setId(id);
        explain.setName(id);
        explain.setExplainObject(explainThrowable);
        explain.setDataType(ExplainDataType.JSON);
        addChild(explain);
    }

    private String getStackTrace(Throwable throwable) {
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
            return sw.toString();
        } catch (IOException e) {
            log.warn("explainThrowable: Could not get throwable stack trace");
        }
        return null;
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
