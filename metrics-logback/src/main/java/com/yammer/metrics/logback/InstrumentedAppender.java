package com.yammer.metrics.logback;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MeterMetric;
import com.yammer.metrics.core.MetricsRegistry;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.concurrent.TimeUnit;

/**
 * A Logback {@link AppenderBase} which has six meters, one for each logging level and one for the
 * total number of statements being logged.
 */
public class InstrumentedAppender extends AppenderBase<ILoggingEvent> {
    
    final MeterMetric ALL_METER;
    final MeterMetric TRACE_METER;
    final MeterMetric DEBUG_METER;
    final MeterMetric INFO_METER;
    final MeterMetric WARN_METER;
    final MeterMetric ERROR_METER;

    /**
     * Create an appender that uses the default {@link MetricsRegistry}
     * as returned by {@link Metrics#defaultRegistry()}.
     */
    public InstrumentedAppender() {
        this(Metrics.defaultRegistry());
    }
    
    
    /**
     * Construct a new appender that uses a specific {@link MetricsRegistry}.
     * 
     * @param registry
     */
    public InstrumentedAppender(MetricsRegistry registry) {
        ALL_METER = registry.newMeter(InstrumentedAppender.class, "all", "statements", TimeUnit.SECONDS);
        TRACE_METER = registry.newMeter(InstrumentedAppender.class, "trace", "statements", TimeUnit.SECONDS);
        DEBUG_METER = registry.newMeter(InstrumentedAppender.class, "debug", "statements", TimeUnit.SECONDS);
        INFO_METER = registry.newMeter(InstrumentedAppender.class, "info", "statements", TimeUnit.SECONDS);
        WARN_METER = registry.newMeter(InstrumentedAppender.class, "warn", "statements", TimeUnit.SECONDS);
        ERROR_METER = registry.newMeter(InstrumentedAppender.class, "error", "statements", TimeUnit.SECONDS);
    }
    
    @Override
    protected void append(ILoggingEvent event) {
        ALL_METER.mark();
        if (event.getLevel().toInt() == Level.TRACE_INT) {
            TRACE_METER.mark();
        } else if (event.getLevel().toInt() == Level.DEBUG_INT) {
            DEBUG_METER.mark();
        } else if (event.getLevel().toInt() == Level.INFO_INT) {
            INFO_METER.mark();
        } else if (event.getLevel().toInt() == Level.WARN_INT) {
            WARN_METER.mark();
        } else if (event.getLevel().toInt() == Level.ERROR_INT) {
            ERROR_METER.mark();
        }
    }
}