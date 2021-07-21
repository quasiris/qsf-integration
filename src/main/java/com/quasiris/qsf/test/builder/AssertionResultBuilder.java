package com.quasiris.qsf.test.builder;

import com.quasiris.qsf.test.dto.AssertionResult;
import com.quasiris.qsf.test.dto.Status;

public final class AssertionResultBuilder {
    private Status status;
    private String name;
    private String message;
    private String stderr;
    private String stdout;

    private AssertionResultBuilder() {
    }

    public static AssertionResultBuilder create() {
        return new AssertionResultBuilder();
    }

    public AssertionResultBuilder failed() {
        this.status = Status.FAILED;
        return this;
    }

    public AssertionResultBuilder success() {
        this.status = Status.SUCCESS;
        return this;
    }

    public AssertionResultBuilder status(Status status) {
        this.status = status;
        return this;
    }

    public AssertionResultBuilder name(String name) {
        this.name = name;
        return this;
    }

    public AssertionResultBuilder message(String message) {
        this.message = message;
        return this;
    }

    public AssertionResultBuilder stderr(String stderr) {
        this.stderr = stderr;
        return this;
    }

    public AssertionResultBuilder stdout(String stdout) {
        this.stdout = stdout;
        return this;
    }

    public AssertionResult build() {
        AssertionResult assertionResult = new AssertionResult();
        assertionResult.setStatus(status);
        assertionResult.setName(name);
        assertionResult.setMessage(message);
        assertionResult.setStderr(stderr);
        assertionResult.setStdout(stdout);
        return assertionResult;
    }
}
