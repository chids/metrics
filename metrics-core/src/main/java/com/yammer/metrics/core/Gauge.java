package com.yammer.metrics.core;

import com.yammer.metrics.core.Measurement.Extractor;


/**
 * A gauge metric is an instantaneous reading of a particular value. To instrument a queue's depth,
 * for example:<br>
 * <pre><code>
 * final Queue&lt;String&gt; queue = new ConcurrentLinkedQueue&lt;String&gt;();
 * final Gauge&lt;Integer&gt; queueDepth = new Gauge&lt;Integer&gt;() {
 *     public Integer value() {
 *         return queue.size();
 *     }
 * };
 * </code></pre>
 *
 * @param <T> the type of the metric's value
 */
public abstract class Gauge<T> implements Metric {
    /**
     * Returns the metric's current value.
     *
     * @return the metric's current value
     */
    public abstract T getValue();

    @Override
    public <U> void processWith(MetricProcessor<U> processor, MetricName name, U context) throws Exception {
        processor.processGauge(name, this, context);
    }

    @Override
    public Object apply(Extractor extractor) {
        return extractor.get(this);
    }
}