package com.yammer.metrics.weld.interceptor;

import java.lang.reflect.Method;

import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.weld.annotation.WeldMetered;

/**
 * A method interceptor which creates a {@link Meter} for the declaring class with the given name (or the method's name,
 * if none was provided), and which measures the rate at which the annotated method is invoked.
 */
@Interceptor
@WeldMetered
public class MeteredInterceptor extends AbstractInterceptor {

	@Override
	protected Object performIntercept(InvocationContext ctx) throws Exception {
		final Meter meter = createMetric(ctx.getMethod(), ctx.getMethod().getDeclaringClass(), registry);
		meter.mark();
		return ctx.proceed();
	}

	public static <T> Meter createMetric(Method method, Class<?> klass, MetricsRegistry registry) {
		final Metered annotation = method.getAnnotation(Metered.class);
		final MetricName name = MeteredInterceptor.forMethod(method, klass);
		return registry.newMeter(name, annotation.eventType(), annotation.rateUnit());
	}

	public static MetricName forMethod(Method method, Class<?> klass) {
		final Metered annotation = method.getAnnotation(Metered.class);
		final String group = MetricName.chooseGroup(annotation.group(), klass);
		final String type = MetricName.chooseType(annotation.type(), klass);
		final String name = MetricName.chooseName(annotation.name(), method);
		return new MetricName(group, type, name);
	}
}