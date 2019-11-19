package com.quasiris.qsf.exception;

public class Debug {

    private String id;
    private String name;
    private DebugType type = DebugType.JSON;
    private Object debugObject;

    /**
     * Getter for property 'id'.
     *
     * @return Value for property 'id'.
     */
    public String getId() {
        return id;
    }

    /**
     * Setter for property 'id'.
     *
     * @param id Value to set for property 'id'.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter for property 'name'.
     *
     * @return Value for property 'name'.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for property 'name'.
     *
     * @param name Value to set for property 'name'.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for property 'debugObject'.
     *
     * @return Value for property 'debugObject'.
     */
    public Object getDebugObject() {
        return debugObject;
    }

    /**
     * Setter for property 'debugObject'.
     *
     * @param debugObject Value to set for property 'debugObject'.
     */
    public void setDebugObject(Object debugObject) {
        this.debugObject = debugObject;
    }

    /**
     * Getter for property 'type'.
     *
     * @return Value for property 'type'.
     */
    public DebugType getType() {
        return type;
    }

    /**
     * Setter for property 'type'.
     *
     * @param type Value to set for property 'type'.
     */
    public void setType(DebugType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Debug{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", debugObject=" + debugObject +
                '}';
    }
}
