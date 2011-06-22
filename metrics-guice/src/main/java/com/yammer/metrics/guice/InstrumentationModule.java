package com.yammer.metrics.guice;

import java.lang.reflect.Method;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

/**
 * A Guice module which instruments methods annotated with the {@link Metered}
 * and {@link Timed} annotations.
 *
 * @see Gauge
 * @see Metered
 * @see Timed
 * @see MeteredInterceptor
 * @see TimedInterceptor
 * @see GaugeInjectionListener
 */
public class InstrumentationModule extends AbstractModule implements MetricsBaptizer
{
    @Override
    protected void configure() {
        bindListener(Matchers.any(), new MeteredListener(this));
        bindListener(Matchers.any(), new TimedListener(this));
        bindListener(Matchers.any(), new GaugeListener(this));
    }

    /**
     * Overrride this method to implement a custom naming scheme for the metrics
     * created through annotations. 
     */
    @Override
    public String giveNameTo(final String annotationName, final Method method) {
        return annotationName.isEmpty() ? method.getName() : annotationName;
    }
}