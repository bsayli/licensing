package io.github.bsayli.licensing.client.common.core;

import io.github.bsayli.apicontract.envelope.ServiceResponse;
import io.github.bsayli.licensing.client.common.problem.ApiClientException;
import io.github.bsayli.licensing.client.common.problem.ApiProblemException;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class ApiClientExecutor {

    public <T> ServiceResponse<T> handle(String operation, Supplier<ServiceResponse<T>> supplier) {
        try {
            return supplier.get();
        } catch (ApiProblemException e) {
            throw new ApiClientException(operation, e);
        }
    }
}