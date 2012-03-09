package com.yammer.metrics.reporting;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import clojure.lang.IPersistentMap;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Var;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricProcessor;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;

public class RiemannReporter extends AbstractPollingReporter implements MetricProcessor<Object> {

	static {
		try {
			RT.loadResourceScript("riemann/client.clj");
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private static final Var connect = RT.var("riemann.client", "tcp-client");
	private static final Var send = RT.var("riemann.client", "send-event");
	private static final Var close = RT.var("riemann.client", "close-client");

	public RiemannReporter(MetricsRegistry registry, String name) throws Exception {
		super(registry, name);
		System.err.println("Connecting");
		final Object connection = connect.invoke("127.0.0.1", 5555);
		System.err.println("...connected");
		final AtomicInteger i = new AtomicInteger();
		while (0 == new Integer(0).intValue()) {
			@SuppressWarnings("serial")
			final IPersistentMap map = PersistentHashMap.create(new HashMap<String, Object>() {
				{
					put("service", "foo");
					put("state", "bar");
					put("metric", i.incrementAndGet());
					put("tags", PersistentList.create(Arrays.asList("foo", "bar")));
				}
			});
			send.invoke(connection, map);
			System.err.println("...sent");
		}
		close.invoke(connection);
		System.err.println("...disconnected");
	}

	public static void main(String[] args) throws Exception {
		MetricsRegistry registry = new MetricsRegistry();
		RiemannReporter reporter = new RiemannReporter(registry, "name");
		reporter.shutdown();
	}

	@Override
	public void run() {}

	@Override
	public void processMeter(MetricName name, Metered meter, Object context) throws Exception {}

	@Override
	public void processCounter(MetricName name, Counter counter, Object context) throws Exception {}

	@Override
	public void processHistogram(MetricName name, Histogram histogram, Object context) throws Exception {}

	@Override
	public void processTimer(MetricName name, Timer timer, Object context) throws Exception {}

	@Override
	public void processGauge(MetricName name, Gauge<?> gauge, Object context) throws Exception {}
}