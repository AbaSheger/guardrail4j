package io.github.abasheger.guardrail4j.spel;

import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class SpelExpressionResolver {

    private static final Logger log = LoggerFactory.getLogger(SpelExpressionResolver.class);

    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer paramNameDiscoverer = new DefaultParameterNameDiscoverer();

    public String resolve(String value, Method method, Object[] args) {
        if (!value.startsWith("#")) {
            return value;
        }
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            String[] paramNames = paramNameDiscoverer.getParameterNames(method);
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length && i < args.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }
            for (int i = 0; i < args.length; i++) {
                context.setVariable("p" + i, args[i]);
            }
            Object result = parser.parseExpression(value).getValue(context);
            return result != null ? result.toString() : value;
        } catch (Exception e) {
            log.warn("Guardrail4J SpEL resolution failed for '{}', using raw value. Cause: {}",
                    value, e.getMessage());
            return value;
        }
    }
}
