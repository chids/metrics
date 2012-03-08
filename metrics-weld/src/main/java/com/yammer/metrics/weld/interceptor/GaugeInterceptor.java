package com.yammer.metrics.weld.interceptor;

import java.lang.reflect.Method;

import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import com.yammer.metrics.annotation.Gauge;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.weld.annotation.WeldGauge;

/**
 * A method interceptor which creates a {@link Gauge} for the declaring class with the given name (or the method's name,
 * if none was provided), and which measures the rate at which the annotated method is invoked.
 */
@Interceptor
@WeldGauge
public class GaugeInterceptor extends AbstractInterceptor {

	@Override
	protected Object performIntercept(final InvocationContext ctx) throws Exception {
		final MetricName name = forMethod(ctx.getMethod(), ctx.getMethod().getDeclaringClass());
		this.registry.newGauge(name, new com.yammer.metrics.core.Gauge<Object>() {
			@Override
			public Object value() {
				try {
					return ctx.getMethod().invoke(ctx.getTarget());
				} catch (Exception e) {
					e.printStackTrace();
					return new RuntimeException(e);
				}
			}
		});
		return ctx.proceed();
	}
	
	public static MetricName forMethod(final Method method, final Class<?> klass) {
		final Gauge annotation = method.getAnnotation(Gauge.class);
		if (method.getParameterTypes().length == 0) {
			final String group = MetricName.chooseGroup(annotation.group(), klass);
			final String type = MetricName.chooseType(annotation.type(), klass);
			final String name = MetricName.chooseName(annotation.name(), method);
			return new MetricName(group, type, name);
		}
		throw new IllegalArgumentException("Method " + method.getName()
				+ " is annotated with @Gauge but requires parameters");
	}
}