package io.github.bsayli.licensing.agent.cli.model;

import java.util.List;

public record ApiResponse<T>(int status, String message, T data, List<ApiError> errors) {}
