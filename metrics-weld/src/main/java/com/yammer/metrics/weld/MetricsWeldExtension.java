package com.yammer.metrics.weld;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.jboss.weld.extensions.annotated.AnnotatedTypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Gauge;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.weld.annotation.WeldExceptionMetered;
import com.yammer.metrics.weld.annotation.WeldGauge;
import com.yammer.metrics.weld.annotation.WeldMetered;
import com.yammer.metrics.weld.annotation.WeldTimed;
import com.yammer.metrics.weld.interceptor.ExceptionMeteredInterceptor;
import com.yammer.metrics.weld.interceptor.MeteredInterceptor;
import com.yammer.metrics.weld.interceptor.TimedInterceptor;

/**
 * A Weld {@link Extension} that:
 * 
 * 1. Inspects all methods on all objects and looks for the existence of any metric annotations in which case it
 * decorates the method with it's Weld counterpart to hook up the appropriate interceptor.
 * 
 * 2. Registeres an instance of {@link MetricsRegistryBean} with Weld in order to guarantee that all injections of
 * {@link MetricsRegistry} uses the same instance.
 * 
 */
public class MetricsWeldExtension implements Extension {

	private static final Logger log = LoggerFactory.getLogger(MetricsWeldExtension.class);

	// FIXME: We'll need to be able to customize the creation of the registry somehow
	private final MetricsRegistry registry = new MetricsRegistry();

	@SuppressWarnings("serial")
	private final Map<Class<? extends Annotation>, WeldMetricsFactory> mappings = Collections
			.unmodifiableMap(new HashMap<Class<? extends Annotation>, WeldMetricsFactory>() {
				{
					put(Timed.class, new WeldMetricsFactory(WeldTimed.class) {

						@Override
						protected <T> void createMetric(AnnotatedMethod<T> method, Class<?> klass) {
							TimedInterceptor.createMetric(method.getJavaMember(), klass, registry);
						}

					});
					put(Metered.class, new WeldMetricsFactory(WeldMetered.class) {

						@Override
						protected <T> void createMetric(AnnotatedMethod<T> method, Class<?> klass) {
							MeteredInterceptor.createMetric(method.getJavaMember(), klass, registry);
						}
					});
					put(Gauge.class, new WeldMetricsFactory(WeldGauge.class) {

						@Override
						protected <T> void createMetric(AnnotatedMethod<T> method, Class<?> klass) {
							// Gauges can only be cretaed with a reference to the instance on which
							// they are to be invoked, hence we can't do anything here.
						}
					});
					put(ExceptionMetered.class, new WeldMetricsFactory(WeldExceptionMetered.class) {

						@Override
						protected <T> void createMetric(AnnotatedMethod<T> method, Class<?> klass) {
							ExceptionMeteredInterceptor.createMetric(method.getJavaMember(), klass, registry);
						}
					});
				}
			});

	void afterBeanDiscovery(@Observes AfterBeanDiscovery state, BeanManager manager) {
		state.addBean(new MetricsRegistryBean(this.registry, manager));
	}

	<T> void processAnnotatedType(@Observes final ProcessAnnotatedType<T> type) {
		final Class<T> klass = type.getAnnotatedType().getJavaClass();
		log.info("Inspecting methods in {}", klass.getName());
		final AnnotatedTypeBuilder<T> builder = AnnotatedTypeBuilder.newInstance(type.getAnnotatedType());
		builder.readAnnotationsFromUnderlyingType();
		for (final AnnotatedMethod<? super T> method : type.getAnnotatedType().getMethods()) {
			for (final Entry<Class<? extends Annotation>, WeldMetricsFactory> entry : mappings.entrySet()) {
				if (method.isAnnotationPresent(entry.getKey())) {
					final WeldMetricsFactory factory = entry.getValue();
					builder.addToMethod(method.getJavaMember(), factory.createAnnotation());
					factory.createMetric(method);
				}
			}
		}
		type.setAnnotatedType(builder.create());
	}
}