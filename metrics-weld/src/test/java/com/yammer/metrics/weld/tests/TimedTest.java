package com.yammer.metrics.weld.tests;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Test;

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.Timer;

public class TimedTest extends AbstractWeldInterceptorTest {

	@Inject
	private InstrumentedWithTimed instance;
	
    @Test
    public void aTimedAnnotatedMethod() throws Exception {

        instance.doAThing();

        final Metric metric = registry.allMetrics().get(new MetricName(InstrumentedWithTimed.class,
                                                                       "things"));

        assertMetricSetup(metric);

        assertThat("Guice creates a timer which records invocation length",
                   ((Timer) metric).count(),
                   is(1L));

        assertThat("Guice creates a timer with the given rate unit",
                   ((Timer) metric).rateUnit(),
                   is(TimeUnit.MINUTES));

        assertThat("Guice creates a timer with the given duration unit",
                   ((Timer) metric).durationUnit(),
                   is(TimeUnit.MICROSECONDS));
    }

    @Test
    public void aTimedAnnotatedMethodWithDefaultScope() throws Exception {

        instance.doAThing();

        final Metric metric = registry.allMetrics().get(new MetricName(InstrumentedWithTimed.class,
                                                                       "doAThingWithDefaultScope"));

        assertMetricSetup(metric);
    }

    @Test
    public void aTimedAnnotatedMethodWithProtectedScope() throws Exception {

        instance.doAThing();

        final Metric metric = registry.allMetrics().get(new MetricName(InstrumentedWithTimed.class,
                                                                       "doAThingWithProtectedScope"));

        assertMetricSetup(metric);
    }

    @Test
    public void aTimedAnnotatedMethodWithCustomGroupTypeAndName() throws Exception {

        instance.doAThingWithCustomGroupTypeAndName();

        final Metric metric = registry.allMetrics().get(new MetricName("timed", "t", "n"));

        assertMetricSetup(metric);
    }

    private void assertMetricSetup(final Metric metric) {
        assertThat("Guice creates a metric",
                   metric,
                   is(notNullValue()));

        assertThat("Guice creates a timer",
                   metric,
                   is(instanceOf(Timer.class)));
    }
}
