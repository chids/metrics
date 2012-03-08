package com.yammer.metrics.weld.interceptor;

import java.lang.reflect.Method;
import java.util.Arrays;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.core.MetricsRegistry;

public abstract class AbstractInterceptor {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	@Inject
	protected MetricsRegistry registry;

	@AroundInvoke
	public final Object intercept(InvocationContext ctx) throws Exception {
		if(log.isInfoEnabled()) {
			final Method method = ctx.getMethod();
			log.info("Intercepting invocation of {}.{} annotated with: {}", new Object[] {method.getDeclaringClass().getSimpleName(), method.getName(), Arrays.toString(method.getAnnotations())});
		}
		return performIntercept(ctx);
	}

	protected abstract Object performIntercept(InvocationContext ctx) throws Exception;
	
}