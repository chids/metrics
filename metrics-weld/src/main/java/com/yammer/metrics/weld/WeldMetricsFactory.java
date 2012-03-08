package com.yammer.metrics.weld;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.enterprise.inject.spi.AnnotatedMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.core.Metric;

/**
 * A factory that creates {@link Metric}s based on a specific {@link Annotation}. Used to map the core metrics
 * annotations to their Weld counterparts.
 * 
 * The actual work is done in {@link MetricsWeldExtension}.
 */
public abstract class WeldMetricsFactory {

	private static final Logger log = LoggerFactory.getLogger(WeldMetricsFactory.class);

	private final Class<? extends Annotation> type;

	public WeldMetricsFactory(Class<? extends Annotation> type) {
		this.type = type;
	}

	public final Annotation createAnnotation() {
		return new Annotation() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return type;
			}
		};
	}

	public final <T> void createMetric(AnnotatedMethod<T> method) {
		final Class<?> klass = method.getDeclaringType().getJavaClass();
		final Method actualMethod = method.getJavaMember();
		createMetric(method, klass);
		log.info("{}: added {} for {}.{}", new Object[] { klass.getSimpleName(), type.getSimpleName(),
				actualMethod.getDeclaringClass().getSimpleName(), actualMethod.getName() });
	}

	protected abstract <T> void createMetric(AnnotatedMethod<T> method, Class<?> klass);
}