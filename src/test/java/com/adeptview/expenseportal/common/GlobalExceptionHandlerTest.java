package com.adeptview.expenseportal.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @AfterEach
    void clearContext() {
        MDC.clear();
    }

    @Test
    void answersAnUnexpectedErrorWithAReferenceId() {
        MDC.put(RequestIdFilter.MDC_KEY, "7f3a9c2e1b4d");

        ResponseEntity<ApiError> response = handler.unexpected(new IllegalStateException("simulated"),
                new MockHttpServletRequest("POST", "/api/claims"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo(new ApiError("Something went wrong", "7f3a9c2e1b4d"));
    }

    @Test
    void passesAnExpectedErrorMessageThrough() {
        MDC.put(RequestIdFilter.MDC_KEY, "0a1b2c3d4e5f");

        ResponseEntity<ApiError> response = handler.notFound(new NotFoundException("Claim 1099 not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(new ApiError("Claim 1099 not found", "0a1b2c3d4e5f"));
    }
}
