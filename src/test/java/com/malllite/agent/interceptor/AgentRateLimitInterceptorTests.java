package com.malllite.agent.interceptor;

import com.malllite.common.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AgentRateLimitInterceptorTests {

    @Test
    void blocksRequestsAfterConfiguredLimit() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-04-24T08:00:00Z"), ZoneOffset.UTC);
        AgentRateLimitInterceptor interceptor = new AgentRateLimitInterceptor(true, 2, 60, fixedClock);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        assertDoesNotThrow(() -> interceptor.preHandle(request, null, new Object()));
        assertDoesNotThrow(() -> interceptor.preHandle(request, null, new Object()));
        assertThrows(TooManyRequestsException.class, () -> interceptor.preHandle(request, null, new Object()));
    }
}
