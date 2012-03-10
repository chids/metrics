package com.yammer.metrics.reporting;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import clojure.lang.IPersistentMap;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Var;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.Metric;
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
	private static final Var sendEvent = RT.var("riemann.client", "send-event");
	private static final Var sendMessage = RT.var("riemann.client", "send-message");
	private static final Var close = RT.var("riemann.client", "close-client");
	private Object connection;

	public static final class State {
		public final String state;
		public final String service;
		public final String description;
		public final boolean once;
		public final float ttl;
		public final float metric;

		public State(final String state, final String service, final String description, final boolean once,
				final float ttl, final float metric) {
			this.state = state;
			this.service = service;
			this.description = description;
			this.once = once;
			this.ttl = ttl;
			this.metric = metric;
		}
	}

	@SuppressWarnings("serial")
	private static IPersistentMap createState(final State state) throws UnknownHostException {
		return PersistentHashMap.create(new HashMap<String, Object>() {
			{
				put("time", (int)System.currentTimeMillis());
				put("state", state.state);
				put("service", state.service);
				put("host", InetAddress.getLocalHost().getHostName());
				put("description", state.description);
				put("once", state.once);
				// put("ttl", state.ttl);
				put("metric_f", state.metric);
			}
		});
	}

	@SuppressWarnings("serial")
	private static IPersistentMap createEvent(final Event event) throws UnknownHostException {
		return PersistentHashMap.create(new HashMap<String, Object>() {
			{
				put("time", (int)System.currentTimeMillis());
				put("state", event.state);
				put("service", event.service);
				put("host", InetAddress.getLocalHost().getHostName());
				put("description", event.description);
				put("tags", PersistentList.create(event.tags));
				// put("ttl", event.ttl);
				put("metric_f", event.metric);
			}
		});
	}

	public static final class Event {
		public final String state;
		public final String service;
		public final String description;
		public final List<String> tags;
		public final float ttl;
		public final float metric;

		public Event(final String state, final String service, final String description, final float ttl,
				final float metric, final String... tags) {
			this.state = state;
			this.service = service;
			this.description = description;
			this.ttl = ttl;
			this.metric = metric;
			this.tags = Collections.unmodifiableList(new ArrayList<String>(Arrays.asList(tags)));
		}
	}

	public RiemannReporter(MetricsRegistry registry, String name) throws Exception {
		super(registry, name);
	}

	public static void main(String[] args) throws Exception {
		final MetricsRegistry registry = new MetricsRegistry();
		final RiemannReporter reporter = new RiemannReporter(registry, "name");
		reporter.start(4, TimeUnit.SECONDS);
		final Timer timer = registry.newTimer(RiemannReporter.class, "timer");
		final Counter counter = registry.newCounter(RiemannReporter.class, "counter");
		final Histogram histogram = registry.newHistogram(RiemannReporter.class, "histogram");
		final Meter meter = registry.newMeter(RiemannReporter.class, "meter", "stuff", TimeUnit.SECONDS);
		final Gauge<String> gauge = registry.newGauge(RiemannReporter.class, "gauge", new Gauge<String>() {
			@Override
			public String value() {
				return UUID.randomUUID().toString();
			}
		});
		while (0 == new Integer(0).intValue()) {
			switch (new Random().nextInt(4)) {
				case 0:
					timer.time(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Thread.sleep(new Random().nextInt(5) + 1 * 10);
							return null;
						}
					});
					break;
				case 1:
					counter.inc(new Random().nextInt(1000));
					break;
				case 2:
					histogram.update(new Random().nextInt(10000));
					break;
				case 3:
					meter.mark(new Random().nextInt(1000));
					break;
			}
			Thread.sleep(500);
		}
		reporter.shutdown();
	}

	@Override
	public void start(final long period, final TimeUnit unit) {
		System.err.println("Connecting");
		this.connection = connect.invoke("127.0.0.1", 5555);
		System.err.println("...connected");
		super.start(period, unit);
	}

	@Override
	public void shutdown() {
		super.shutdown();
		close.invoke(this.connection);
		System.err.println("...disconnected");
	}

	@Override
	public void run() {
		System.err.println("Reporting");
		try {
			for (final Entry<MetricName, Metric> e : super.getMetricsRegistry().allMetrics().entrySet()) {
				e.getValue().processWith(this, e.getKey(), (Object)null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void send(final Event event) throws UnknownHostException {
		sendEvent.invoke(this.connection, createEvent(event));
	}

	@Override
	public void processMeter(MetricName name, Metered meter, Object context) throws Exception {
		send(new Event("ok", name.getName() + ".count", name.getMBeanName(), -1, meter.count()));
		send(new Event("ok", name.getName() + ".15min", name.getMBeanName(), -1, (float)meter.fifteenMinuteRate()));
		send(new Event("ok", name.getName() + ".5min", name.getMBeanName(), -1, (float)meter.fiveMinuteRate()));
		send(new Event("ok", name.getName() + ".1min", name.getMBeanName(), -1, (float)meter.oneMinuteRate()));
	}

	@Override
	public void processCounter(MetricName name, Counter counter, Object context) throws Exception {
		send(new Event("ok", name.getName() + ".count", name.getMBeanName(), -1, counter.count()));
	}

	@Override
	public void processHistogram(MetricName name, Histogram histogram, Object context) throws Exception {
		send(new Event("ok", name.getName() + ".count", name.getMBeanName(), -1, histogram.count()));
		send(new Event("ok", name.getName() + ".max", name.getMBeanName(), -1, (float)histogram.max()));
		send(new Event("ok", name.getName() + ".mean", name.getMBeanName(), -1, (float)histogram.mean()));
		send(new Event("ok", name.getName() + ".min", name.getMBeanName(), -1, (float)histogram.min()));
		send(new Event("ok", name.getName() + ".sum", name.getMBeanName(), -1, (float)histogram.sum()));
		send(new Event("ok", name.getName() + ".stdDev", name.getMBeanName(), -1, (float)histogram.stdDev()));
	}

	@Override
	public void processTimer(MetricName name, Timer timer, Object context) throws Exception {
		send(new Event("ok", name.getName() + ".count", name.getMBeanName(), -1, timer.count()));
		send(new Event("ok", name.getName() + ".15min", name.getMBeanName(), -1, (float)timer.fifteenMinuteRate()));
		send(new Event("ok", name.getName() + ".5min", name.getMBeanName(), -1, (float)timer.fiveMinuteRate()));
		send(new Event("ok", name.getName() + ".1min", name.getMBeanName(), -1, (float)timer.oneMinuteRate()));
		send(new Event("ok", name.getName() + ".mean", name.getMBeanName(), -1, (float)timer.mean()));
		send(new Event("ok", name.getName() + ".meanRate", name.getMBeanName(), -1, (float)timer.meanRate()));
		send(new Event("ok", name.getName() + ".min", name.getMBeanName(), -1, (float)timer.min()));
		send(new Event("ok", name.getName() + ".max", name.getMBeanName(), -1, (float)timer.max()));
		send(new Event("ok", name.getName() + ".stdDev", name.getMBeanName(), -1, (float)timer.stdDev()));
		send(new Event("ok", name.getName() + ".sum", name.getMBeanName(), -1, (float)timer.sum()));
	}

	@Override
	public void processGauge(MetricName name, Gauge<?> gauge, Object context) throws Exception {
		final Object value = gauge.value();
		if (value instanceof Number) {
			send(new Event(gauge.value().toString(), name.getName() + ".sum", name.getMBeanName(), -1,
					((Number)value).floatValue()));
		} else if (value != null) {
			send(new Event(value.toString(), name.getName() + ".sum", name.getMBeanName(), -1, 0));
		} else {
			System.err.println("Not reporting null from: " + name);
		}
	}
}