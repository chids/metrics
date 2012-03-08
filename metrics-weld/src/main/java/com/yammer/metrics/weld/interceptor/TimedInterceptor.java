package com.yammer.metrics.weld.interceptor;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import com.yammer.metrics.annotation.Timed;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.weld.annotation.WeldTimed;

/**
 * A method {@link Interceptor} which creates a {@link Timer} for the declaring class with the given name (or the
 * method's name, if none was provided), and which measures the rate at which the annotated method is invoked.
 */
@Interceptor
@WeldTimed
public class TimedInterceptor extends AbstractInterceptor {

	@Override
	protected Object performIntercept(InvocationContext ctx) throws Exception {
		final Timer timer = createMetric(ctx.getMethod(), ctx.getMethod().getDeclaringClass(), registry);
		final long startTime = System.nanoTime();
		try {
			return ctx.proceed();
		} finally {
			timer.update(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
		}
	}

	public static <T> Timer createMetric(Method method, Class<?> klass, MetricsRegistry registry) {
		final Timed annotation = method.getAnnotation(Timed.class);
		final MetricName name = forMethod(method, klass);
		return registry.newTimer(name, annotation.durationUnit(), annotation.rateUnit());
	}

	public static MetricName forMethod(Method method, Class<?> klass) {
		final Timed annotation = method.getAnnotation(Timed.class);
		final String group = MetricName.chooseGroup(annotation.group(), klass);
		final String type = MetricName.chooseType(annotation.type(), klass);
		final String name = MetricName.chooseName(annotation.name(), method);
		return new MetricName(group, type, name);
	}
}
