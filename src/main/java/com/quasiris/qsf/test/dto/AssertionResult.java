package com.quasiris.qsf.test.dto;

public class AssertionResult {


    public AssertionResult() {
        this.status = Status.SUCCESS;
    }

    public AssertionResult(String message) {
        this.status = Status.SUCCESS;
        this.message = message;
    }

    private Status status;

    private String name;
    private String message;

    private String stderr;

    private String stdout;


    /**
     * Getter for property 'status'.
     *
     * @return Value for property 'status'.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Setter for property 'status'.
     *
     * @param status Value to set for property 'status'.
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Getter for property 'message'.
     *
     * @return Value for property 'message'.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Setter for property 'message'.
     *
     * @param message Value to set for property 'message'.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Getter for property 'stderr'.
     *
     * @return Value for property 'stderr'.
     */
    public String getStderr() {
        return stderr;
    }

    /**
     * Setter for property 'stderr'.
     *
     * @param stderr Value to set for property 'stderr'.
     */
    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    /**
     * Getter for property 'stdout'.
     *
     * @return Value for property 'stdout'.
     */
    public String getStdout() {
        return stdout;
    }

    /**
     * Setter for property 'stdout'.
     *
     * @param stdout Value to set for property 'stdout'.
     */
    public void setStdout(String stdout) {
        this.stdout = stdout;
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

    @Override
    public String toString() {
        if(getName() != null) {
            return getName();
        }
        return "AssertionResult{" +
                "status=" + status +
                ", name='" + name + '\'' +
                ", message='" + message + '\'' +
                ", stderr='" + stderr + '\'' +
                ", stdout='" + stdout + '\'' +
                '}';

    }
}
