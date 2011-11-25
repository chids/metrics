package com.yammer.metrics.reporting;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsProcessor;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.util.MetricPredicate;
import com.yammer.metrics.util.Utils;

/**
 * A generic reporter for reporting {@link Metric}s via HTTP. The exact form of the reporting (HTTP verb, formatting of
 * metrics) is determined by subclassing this reporter.
 */
public abstract class HTTPReporter<T> extends AbstractPollingReporter implements MetricsProcessor<HTTPReporter.Context<T>> {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final MetricPredicate predicate;
    private final URI uri;

    public HTTPReporter(MetricsRegistry registry, URI uri) {
        this(registry, uri, MetricPredicate.ALL);
    }

    public HTTPReporter(MetricsRegistry registry, URI uri, MetricPredicate predicate) {
        super(registry, "http-reporter");
        this.uri = uri;
        this.predicate = predicate;
    }

    public static abstract class Context<T> {
        protected final HttpURLConnection connection;

        public Context(HttpURLConnection connection) {
            this.connection = connection;
        }

        public final HttpURLConnection getConnection() {
            return this.connection;
        }

        public abstract T getContext();
        
        public void close() {};
        
        public void finishRequest() {};
    }

    @Override
    public void run() {
        HttpURLConnection connection = null;
        Context<T> context = null;
        try {
            connection = (HttpURLConnection)uri.toURL().openConnection();
            //connection.setDoInput(true);
            connection.setDoOutput(true);
            context = createContext(connection);
            for(Entry<MetricName, Metric> entry : Utils.filterMetrics(metricsRegistry.allMetrics(), predicate).entrySet()) {
                entry.getValue().processWith(this, entry.getKey(), context);
            }
            context.finishRequest();
            connection.getOutputStream().close();
            final int response = connection.getResponseCode();
            log.debug("Response was: " + response);
            if(2 == (response / 100)) {
                connection.getInputStream().read(new byte[connection.getInputStream().available()]);
            }
            if(context != null) {
                context.close();
            }
            connection.disconnect();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    protected abstract Context<T> createContext(HttpURLConnection connection);
}