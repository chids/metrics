package com.yammer.metrics.core;


public interface MetricsProcessor<T>
{
    public abstract void processMeter(MetricName name, Metered meter, T context) throws Exception;

    public abstract void processCounter(MetricName name, CounterMetric counter, T context) throws Exception;

    public abstract void processHistogram(MetricName name, HistogramMetric histogram, T context) throws Exception;

    public abstract void processTimer(MetricName name, TimerMetric timer, T context) throws Exception;

    public abstract void processGauge(MetricName name, GaugeMetric<?> gauge, T context) throws Exception;

}