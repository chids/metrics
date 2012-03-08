package com.yammer.metrics.weld.interceptor;

import java.lang.reflect.Method;

import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.weld.annotation.WeldExceptionMetered;

/**
 * A method interceptor which creates a {@link Meter} for the declaring class with the given name (or the method's name,
 * if none was provided), and which measures the rate at which the annotated method is invoked.
 */
@Interceptor
@WeldExceptionMetered
public class ExceptionMeteredInterceptor extends AbstractInterceptor {

	@Override
	protected Object performIntercept(InvocationContext ctx) throws Exception {
		try {
			return ctx.proceed();
		} catch (final Exception e) {
			final ExceptionMetered annotation = ctx.getMethod().getAnnotation(ExceptionMetered.class);
			if (annotation.cause().isAssignableFrom(e.getClass())) {
				final Meter meter = createMetric(ctx.getMethod(), ctx.getMethod().getDeclaringClass(), registry);
				meter.mark();
			}
			throw e;
		}
	}

	public static <T> Meter createMetric(Method method, Class<?> klass, MetricsRegistry registry) {
		final ExceptionMetered annotation = method.getAnnotation(ExceptionMetered.class);
		final MetricName name = ExceptionMeteredInterceptor.forMethod(method, klass);
		return registry.newMeter(name, annotation.eventType(), annotation.rateUnit());
	}

	public static MetricName forMethod(Method method, Class<?> klass) {
		final ExceptionMetered annotation = method.getAnnotation(ExceptionMetered.class);
		final String group = MetricName.chooseGroup(annotation.group(), klass);
		final String type = MetricName.chooseType(annotation.type(), klass);
		final String name = determineName(annotation, method);
		return new MetricName(group, type, name);
	}

	private static String determineName(ExceptionMetered annotation, Method method) {
		if (annotation.name().isEmpty()) {
			return method.getName() + ExceptionMetered.DEFAULT_NAME_SUFFIX;
		}
		return annotation.name();
	}
}
