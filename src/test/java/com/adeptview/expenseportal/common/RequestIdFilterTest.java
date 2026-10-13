package com.adeptview.expenseportal.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @Test
    void givesARequestWithoutAnIdANewOne() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> seenWhileHandling = new AtomicReference<>();

        filter.doFilter(new MockHttpServletRequest("GET", "/api/claims"), response,
                (request, ignored) -> seenWhileHandling.set(MDC.get(RequestIdFilter.MDC_KEY)));

        assertThat(response.getHeader(RequestIdFilter.HEADER)).matches("[0-9a-f]{12}")
                .isEqualTo(seenWhileHandling.get());
        assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();
    }

    @Test
    void keepsAWellFormedIncomingId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/claims");
        request.addHeader(RequestIdFilter.HEADER, "gateway-7f3a9c2e");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> { });

        assertThat(response.getHeader(RequestIdFilter.HEADER)).isEqualTo("gateway-7f3a9c2e");
    }

    @Test
    void replacesAMalformedIncomingId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/claims");
        request.addHeader(RequestIdFilter.HEADER, "abc\nforged log line");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> { });

        assertThat(response.getHeader(RequestIdFilter.HEADER)).matches("[0-9a-f]{12}");
    }
}
