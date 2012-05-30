package com.yammer.metrics.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A {@link MetricPredicate} is used to determine whether a metric should be included when sorting
 * and filtering metrics. This is especially useful for limited metric reporting.
 * 
 * A predicate which matches all inputs.
 */
public class MetricPredicate {

    public static final MetricPredicate ALL = new MetricPredicate();
    private final Measurement[] measurements;

    public MetricPredicate(Measurement... measurements) {
        this.measurements = (measurements == null) ? Measurement.values() : measurements;
    }

    /**
     * Returns {@code true} if the metric matches the predicate.
     *
     * @param name   the name of the metric
     * @param metric the metric itself
     * @return {@code true} if the predicate applies, {@code false} otherwise
     */
    public boolean matches(MetricName name, Metric metric) {
        return true;
    }

    public Map<Measurement, Object> filter(Metric metric) {
        final Map<Measurement, Object> result = new LinkedHashMap<Measurement, Object>();
        for (final Measurement measurement : this.measurements) {
            final Object value = metric.apply(measurement.extractor);
            if (value != null) {
                result.put(measurement, value);
            }
        }
        return Collections.unmodifiableMap(result);
    }
}
