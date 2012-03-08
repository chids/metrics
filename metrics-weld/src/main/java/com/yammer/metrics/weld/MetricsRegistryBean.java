package com.yammer.metrics.weld;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Singleton;

import com.yammer.metrics.core.MetricsRegistry;

/**
 * A bean holding a sole instance of a {@link MetricsRegistry}
 */
public class MetricsRegistryBean implements Bean<MetricsRegistry> {

	private final InjectionTarget<MetricsRegistry> it;
	private final MetricsRegistry instance;

	public MetricsRegistryBean(MetricsRegistry instance, BeanManager bm) {
		this.instance = instance;
		final AnnotatedType<MetricsRegistry> at = bm.createAnnotatedType(MetricsRegistry.class);
		it = bm.createInjectionTarget(at);
	}

	@Override
	public MetricsRegistry create(CreationalContext<MetricsRegistry> ctx) {
		return this.instance;
	}

	@Override
	public void destroy(MetricsRegistry instance, CreationalContext<MetricsRegistry> ctx) {
		it.preDestroy(instance);
		it.dispose(instance);
		ctx.release();
		instance.shutdown();
		System.err.println("DESTROY ON: " + instance + " (singleton instance is: " + this.instance + ")");
	}

	@Override
	public Class<?> getBeanClass() {
		return MetricsRegistry.class;
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints() {
		return it.getInjectionPoints();
	}

	@Override
	public String getName() {
		return MetricsRegistry.class.getSimpleName();
	}

	@Override
	@SuppressWarnings("serial")
	public Set<Annotation> getQualifiers() {
		return new HashSet<Annotation>() {
			{
				add(new AnnotationLiteral<Default>() {});
				add(new AnnotationLiteral<Any>() {});
			}
		};
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return Singleton.class;
	}

	@Override
	public Set<Class<? extends Annotation>> getStereotypes() {
		return Collections.emptySet();
	}

	@Override
	@SuppressWarnings("serial")
	public Set<Type> getTypes() {
		return new HashSet<Type>() {
			{
				add(MetricsRegistry.class);
				add(Object.class);
			}
		};
	}

	@Override
	public boolean isAlternative() {
		return false;
	}

	@Override
	public boolean isNullable() {
		return false;
	}
}