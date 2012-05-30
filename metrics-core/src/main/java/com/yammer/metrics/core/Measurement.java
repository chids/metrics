package com.yammer.metrics.core;

import com.yammer.metrics.stats.Snapshot;

public enum Measurement {

    Value(new Value(), "value"),
    Sum(new Sum(), "sum"),
    Count(new Count(), "count"),
    Min(new Min(), "min"),
    Max(new Max(), "max"),
    Mean(new Mean(), "mean"),
    StdDev(new StdDev(), "stddev"),
    Median(new Median(), "median"),
    RateMean(new RateMean(), "mean rate"),
    Rate1m(new Rate1m(), "1 min rate"),
    Rate5m(new Rate5m(), "5 min rate"),
    Rate15m(new Rate15m(), "15 min rate"),
    p75(new P75(), "75%"),
    p95(new P95(), "95%"),
    p98(new P98(), "98%"),
    p99(new P99(), "99%"),
    p999(new P999(), "99.9%");

    public final Extractor extractor;
    public final String name;

    private Measurement(Extractor extractor, String name) {
        this.extractor = extractor;
        this.name = name;
    }

    public static class Extractor {
        Number get(Counter metric) {
            return null;
        }

        <V> V get(Gauge<V> metric) {
            return null;
        }

        Number get(Meter metric) {
            return null;
        }

        Number get(Timer metric) {
            return null;
        }

        Number get(Histogram metric) {
            return null;
        }
    }

    public static class Value extends Extractor {

        @Override
        <V> V get(Gauge<V> metric) {
            return metric.getValue();
        }
    }

    public static abstract class Metereds<T extends Number> extends Extractor {
        @Override
        T get(Meter metric) {
            return get((Metered)metric);
        }

        @Override
        T get(Timer metric) {
            return get((Metered)metric);
        }

        abstract T get(Metered metered);
    }

    public static abstract class Summarizables extends Extractor {
        @Override
        Double get(Histogram metric) {
            return get((Summarizable)metric);
        }

        @Override
        Double get(Timer metric) {
            return get((Summarizable)metric);
        }

        abstract Double get(Summarizable sum);
    }

    public static abstract class Snapshots extends Extractor {
        @Override
        Double get(Histogram metric) {
            return get(metric.getSnapshot());
        }

        @Override
        Double get(Timer metric) {
            return get(metric.getSnapshot());
        }

        abstract Double get(Snapshot snapshot);
    }

    public static class P75 extends Snapshots {
        @Override
        Double get(Snapshot metric) {
            return metric.get75thPercentile();
        }
    }

    public static class P95 extends Snapshots {
        @Override
        Double get(Snapshot metric) {
            return metric.get95thPercentile();
        }
    }

    public static class P98 extends Snapshots {
        @Override
        Double get(Snapshot metric) {
            return metric.get98thPercentile();
        }
    }

    public static class P99 extends Snapshots {
        @Override
        Double get(Snapshot metric) {
            return metric.get99thPercentile();
        }
    }

    public static class P999 extends Snapshots {
        @Override
        Double get(Snapshot metric) {
            return metric.get999thPercentile();
        }
    }

    public static class Median extends Snapshots {
        @Override
        Double get(Snapshot metric) {
            return metric.getMedian();
        }
    }

    public static class Count extends Metereds<Long> {
        @Override
        Long get(Metered metric) {
            return metric.getCount();
        }

        @Override
        Long get(Counter metric) {
            return metric.getCount();
        }
    }

    public static class RateMean extends Metereds<Double> {
        @Override
        Double get(Metered metric) {
            return metric.getMeanRate();
        }
    }

    public static class Rate1m extends Metereds<Double> {
        @Override
        Double get(Metered metric) {
            return metric.getOneMinuteRate();
        }
    }

    public static class Rate5m extends Metereds<Double> {
        @Override
        Double get(Metered metric) {
            return metric.getFiveMinuteRate();
        }
    }

    public static class Rate15m extends Metereds<Double> {
        @Override
        Double get(Metered metric) {
            return metric.getFifteenMinuteRate();
        }
    }

    public static class Min extends Summarizables {
        @Override
        Double get(Summarizable metric) {
            return metric.getMin();
        }
    }

    public static class Mean extends Summarizables {
        @Override
        Double get(Summarizable metric) {
            return metric.getMean();
        }
    }

    public static class StdDev extends Summarizables {
        @Override
        Double get(Summarizable metric) {
            return metric.getStdDev();
        }
    }

    public static class Max extends Summarizables {
        @Override
        Double get(Summarizable metric) {
            return metric.getMax();
        }
    }

    public static class Sum extends Summarizables {
        @Override
        Double get(Summarizable metric) {
            return metric.getSum();
        }
    }
}