package io.github.bsayli.licensing.client.common.problem;

import java.io.Serial;

public final class ApiClientException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String operation;
    private final ApiProblemException problem;

    public ApiClientException(String operation, ApiProblemException problem) {
        super("[" + operation + "] " + problem.getMessage(), problem);
        this.operation = operation;
        this.problem = problem;
    }

    public String getOperation() {
        return operation;
    }

    public ApiProblemException getProblem() {
        return problem;
    }
}