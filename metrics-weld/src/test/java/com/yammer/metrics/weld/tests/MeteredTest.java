package com.yammer.metrics.weld.tests;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.junit.Test;

import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;

public class MeteredTest extends AbstractWeldInterceptorTest {

	@Inject
	private InstrumentedWithMetered instance;

    @Test
    public void aMeteredAnnotatedMethod() throws Exception {

        instance.doAThing();

        final Metric metric = registry.allMetrics()
                                      .get(new MetricName(InstrumentedWithMetered.class,
                                                          "things"));

        assertMetricIsSetup(metric);

        assertThat("Guice creates a meter which gets marked",
                   ((Meter) metric).count(),
                   is(1L));

        assertThat("Guice creates a meter with the given event type",
                   ((Meter) metric).eventType(),
                   is("poops"));

        assertThat("Guice creates a meter with the given rate unit",
                   ((Meter) metric).rateUnit(),
                   is(TimeUnit.MINUTES));
    }

    @Test
    public void aMeteredAnnotatedMethodWithDefaultScope() throws Exception {

        final Metric metric = registry.allMetrics()
                                      .get(new MetricName(InstrumentedWithMetered.class,
                                                          "doAThingWithDefaultScope"));
        assertMetricIsSetup(metric);

        assertThat("Metric intialises to zero",
                   ((Meter) metric).count(),
                   is(0L));

        instance.doAThingWithDefaultScope();

        assertThat("Metric is marked",
                   ((Meter) metric).count(),
                   is(1L));
    }

    @Test
    public void aMeteredAnnotatedMethodWithProtectedScope() throws Exception {

        final Metric metric = registry.allMetrics()
                                      .get(new MetricName(InstrumentedWithMetered.class,
                                                          "doAThingWithProtectedScope"));

        assertMetricIsSetup(metric);

        assertThat("Metric intialises to zero",
                   ((Meter) metric).count(),
                   is(0L));

        instance.doAThingWithProtectedScope();

        assertThat("Metric is marked",
                   ((Meter) metric).count(),
                   is(1L));
    }

    @Test
    public void aMeteredAnnotatedMethodWithGroupTypeAndName() throws Exception {

        final Metric metric = registry.allMetrics().get(new MetricName("metered", "t", "n"));

        assertMetricIsSetup(metric);

        assertThat("Metric intialises to zero",
                   ((Meter) metric).count(),
                   is(0L));

        instance.doAThingWithGroupTypeAndName();

        assertThat("Metric is marked",
                   ((Meter) metric).count(),
                   is(1L));
    }

    private static void assertMetricIsSetup(final Metric metric) {
        assertThat("Guice creates a metric",
                   metric,
                   is(notNullValue()));

        assertThat("Guice creates a meter",
                   metric,
                   is(instanceOf(Meter.class)));
    }
}
