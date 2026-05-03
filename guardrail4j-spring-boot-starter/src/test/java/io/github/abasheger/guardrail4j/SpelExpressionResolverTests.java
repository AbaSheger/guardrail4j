package io.github.abasheger.guardrail4j;

import io.github.abasheger.guardrail4j.spel.SpelExpressionResolver;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpelExpressionResolverTests {

    private final SpelExpressionResolver resolver = new SpelExpressionResolver();

    @Test
    void staticValuePassesThroughUnchanged() throws Exception {
        Method method = TestMethods.class.getMethod("withUserId", String.class, String.class);
        assertEquals("static-user", resolver.resolve("static-user", method, new Object[]{"arg0", "arg1"}));
    }

    @Test
    void spelResolvesParameterByName() throws Exception {
        Method method = TestMethods.class.getMethod("withUserId", String.class, String.class);
        String result = resolver.resolve("#userId", method, new Object[]{"user-123", "tenant-456"});
        assertEquals("user-123", result);
    }

    @Test
    void spelResolvesTenantByName() throws Exception {
        Method method = TestMethods.class.getMethod("withUserId", String.class, String.class);
        String result = resolver.resolve("#tenantId", method, new Object[]{"user-123", "tenant-456"});
        assertEquals("tenant-456", result);
    }

    @Test
    void spelResolvesParameterByPositionalFallback() throws Exception {
        Method method = TestMethods.class.getMethod("withUserId", String.class, String.class);
        String result = resolver.resolve("#p0", method, new Object[]{"positional-user", "ignored"});
        assertEquals("positional-user", result);
    }

    @Test
    void invalidSpelExpressionFallsBackToRawValue() throws Exception {
        Method method = TestMethods.class.getMethod("withUserId", String.class, String.class);
        String result = resolver.resolve("#nonExistentParam", method, new Object[]{"user-123", "tenant-456"});
        assertEquals("#nonExistentParam", result);
    }

    static class TestMethods {
        public void withUserId(String userId, String tenantId) {}
    }
}
