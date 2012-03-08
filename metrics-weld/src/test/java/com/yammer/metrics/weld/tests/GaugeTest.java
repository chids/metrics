package com.yammer.metrics.weld.tests;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Set;

import javax.inject.Inject;

import org.junit.Test;

import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;

public class GaugeTest extends AbstractWeldInterceptorTest {

	@Inject
	private InstrumentedWithGauge instance;
	
    @Test
    @SuppressWarnings("unchecked")
    public void aGaugeAnnotatedMethod() throws Exception {
        instance.doAThing();

        final Metric metric = registry.allMetrics().get(new MetricName(InstrumentedWithGauge.class,
                                                                       "things"));

        assertThat("Guice creates a metric",
                   metric,
                   is(notNullValue()));

        assertThat("Guice creates a gauge",
                   metric,
                   is(instanceOf(Gauge.class)));

        assertThat("Guice creates a gauge with the given value",
                   ((Gauge<String>) metric).value(),
                   is("poop"));
    }


    @Test
    @SuppressWarnings("unchecked")
    public void aGaugeAnnotatedMethodWithDefaultName() throws Exception {
        instance.doAnotherThing();

        final Metric metric = registry.allMetrics().get(new MetricName(InstrumentedWithGauge.class,
                                                                       "doAnotherThing"));

        assertThat("Guice creates a metric",
                   metric,
                   is(notNullValue()));

        assertThat("Guice creates a gauge",
                   metric,
                   is(instanceOf(Gauge.class)));

        assertThat("Guice creates a gauge with the given value",
                   ((Gauge<String>) metric).value(),
                   is("anotherThing"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void aGaugeAnnotatedMethodWithGroupTypeAndName() throws Exception {
        instance.doAThingWithGroupTypeAndName();

        Set<MetricName> keySet = registry.allMetrics().keySet();
        final Metric metric = registry.allMetrics().get(new MetricName("gauge", "t", "n"));

        assertThat("Guice creates a metric",
                   metric,
                   is(notNullValue()));

        assertThat("Guice creates a gauge",
                   metric,
                   is(instanceOf(Gauge.class)));

        assertThat("Guice creates a gauge with the given value",
                   ((Gauge<String>) metric).value(),
                   is("anotherThingWithGroupTypeAndName"));
    }
}