package com.yammer.metrics.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class InstrumentedAppenderTest {
    final LoggerContext lc = new LoggerContext();
    final Logger logger = lc.getLogger("abc.def");

    final InstrumentedAppender appender = new InstrumentedAppender();

    @Before
    public void setUp() throws Exception {
        appender.setContext(lc);
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.TRACE);
    }

    @Test
    public void maintainsAccurateCounts() throws Exception {
        assertThat(appender.ALL_METER.count(), is(0L));
        assertThat(appender.TRACE_METER.count(), is(0L));
        assertThat(appender.DEBUG_METER.count(), is(0L));
        assertThat(appender.INFO_METER.count(), is(0L));
        assertThat(appender.WARN_METER.count(), is(0L));
        assertThat(appender.ERROR_METER.count(), is(0L));

        logger.trace("trace");
        assertThat(appender.ALL_METER.count(), is(1L));
        assertThat(appender.TRACE_METER.count(), is(1L));
        assertThat(appender.DEBUG_METER.count(), is(0L));
        assertThat(appender.INFO_METER.count(), is(0L));
        assertThat(appender.WARN_METER.count(), is(0L));
        assertThat(appender.ERROR_METER.count(), is(0L));

        logger.trace("Test");
        logger.debug("Test");
        assertThat(appender.ALL_METER.count(), is(3L));
        assertThat(appender.TRACE_METER.count(), is(2L));
        assertThat(appender.DEBUG_METER.count(), is(1L));
        assertThat(appender.INFO_METER.count(), is(0L));
        assertThat(appender.WARN_METER.count(), is(0L));
        assertThat(appender.ERROR_METER.count(), is(0L));

        logger.info("Test");
        assertThat(appender.ALL_METER.count(), is(4L));
        assertThat(appender.TRACE_METER.count(), is(2L));
        assertThat(appender.DEBUG_METER.count(), is(1L));
        assertThat(appender.INFO_METER.count(), is(1L));
        assertThat(appender.WARN_METER.count(), is(0L));
        assertThat(appender.ERROR_METER.count(), is(0L));

        logger.warn("Test");
        assertThat(appender.ALL_METER.count(), is(5L));
        assertThat(appender.TRACE_METER.count(), is(2L));
        assertThat(appender.DEBUG_METER.count(), is(1L));
        assertThat(appender.INFO_METER.count(), is(1L));
        assertThat(appender.WARN_METER.count(), is(1L));
        assertThat(appender.ERROR_METER.count(), is(0L));

        logger.error("Test");
        assertThat(appender.ALL_METER.count(), is(6L));
        assertThat(appender.TRACE_METER.count(), is(2L));
        assertThat(appender.DEBUG_METER.count(), is(1L));
        assertThat(appender.INFO_METER.count(), is(1L));
        assertThat(appender.WARN_METER.count(), is(1L));
        assertThat(appender.ERROR_METER.count(), is(1L));
    }
}
