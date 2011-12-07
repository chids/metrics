package com.yammer.metrics.util;

import java.util.concurrent.atomic.AtomicBoolean;

import com.yammer.metrics.core.CounterMetric;
import com.yammer.metrics.core.GaugeMetric;
import com.yammer.metrics.core.HistogramMetric;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsProcessor;
import com.yammer.metrics.core.TimerMetric;

/**
 * A {@link MetricPredicate} is used to determine whether a metric should be included when sorting
 * and filtering metrics. This is especially useful for limited metric reporting.
 */
public class MetricPredicate implements MetricsProcessor<AtomicBoolean> {

    /**
     * A predicate which matches all inputs.
     */
    public static final MetricPredicate ALL = new MetricPredicate();

    /**
     * Returns {@code true} if the metric matches the predicate. Override to customize behaviour for all metric types or
     * override one or more of the methods for individual types:
     * <ul>
     * <li>{@link MetricPredicate#counterMatches(MetricName, CounterMetric)}</li>
     * <li>{@link MetricPredicate#gaugeMatches(MetricName, GaugeMetric)}</li>
     * <li>{@link MetricPredicate#histogramMatches(MetricName, HistogramMetric)}</li>
     * <li>{@link MetricPredicate#meterMatches(MetricName, Metered)}</li>
     * <li>{@link MetricPredicate#timerMatches(MetricName, TimerMetric)}</li>
     * </ul>
     * 
     * @param name
     *            the name of the metric
     * @param metric
     *            the metric itself
     * @return {@code true} if the predicate applies, {@code false} otherwise
     * 
     */
    public boolean matches(MetricName name, Metric metric) {
        final AtomicBoolean result = new AtomicBoolean(true);
        try {
            metric.processWith(this, name, result);
        }
        catch(Exception ignored) {
            result.set(false);
        }
        return result.get();
    }

    public boolean meterMatches(MetricName name, Metered metered) {
        return true;
    }

    public boolean counterMatches(MetricName name, CounterMetric counter) {
        return true;
    }

    public boolean gaugeMatches(MetricName name, GaugeMetric<?> gauge) {
        return true;
    }

    public boolean histogramMatches(MetricName name, HistogramMetric histogram) {
        return true;
    }

    public boolean timerMatches(MetricName name, TimerMetric timer) {
        return true;
    }

    @Override
    public final void processCounter(MetricName name, CounterMetric counter, AtomicBoolean result) throws Exception {
        result.set(counterMatches(name, counter));
    }

    @Override
    public final void processGauge(MetricName name, GaugeMetric<?> gauge, AtomicBoolean result) throws Exception {
        result.set(gaugeMatches(name, gauge));
    }

    @Override
    public final void processHistogram(MetricName name, HistogramMetric histogram, AtomicBoolean result) throws Exception {
        result.set(histogramMatches(name, histogram));
    }

    @Override
    public final void processMeter(MetricName name, Metered meter, AtomicBoolean result) throws Exception {
        result.set(meterMatches(name, meter));
    }

    @Override
    public final void processTimer(MetricName name, TimerMetric timer, AtomicBoolean result) throws Exception {
        result.set(timerMatches(name, timer));
    }
}