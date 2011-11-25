package com.yammer.metrics.examples;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

import com.yammer.metrics.core.CounterMetric;
import com.yammer.metrics.core.GaugeMetric;
import com.yammer.metrics.core.HistogramMetric;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.TimerMetric;
import com.yammer.metrics.reporting.HTTPReporter;
import com.yammer.metrics.util.MetricPredicate;

public class SampleHTTPReporter extends HTTPReporter<JsonGenerator>
{

    public SampleHTTPReporter(MetricsRegistry registry) throws URISyntaxException
    {
        super(registry, new URI("http://127.0.0.1:9999/"), MetricPredicate.ALL);
    }

    @Override
    public void processMeter(MetricName name, Metered meter, HTTPReporter.Context<JsonGenerator> context) throws Exception
    {
        l.countDown();
    }

    @Override
    public void processCounter(MetricName name, CounterMetric counter, HTTPReporter.Context<JsonGenerator> context) throws Exception
    {
        final JsonGenerator json = context.getContext();
        json.writeStringField("name", name.toString());
        json.writeNumberField("value", counter.count());
        l.countDown();
    }

    @Override
    public void processHistogram(MetricName name, HistogramMetric histogram, HTTPReporter.Context<JsonGenerator> context) throws Exception
    {
        l.countDown();
    }

    @Override
    public void processTimer(MetricName name, TimerMetric timer, HTTPReporter.Context<JsonGenerator> context) throws Exception
    {
        l.countDown();
    }

    @Override
    public void processGauge(MetricName name, GaugeMetric<?> gauge, HTTPReporter.Context<JsonGenerator> context) throws Exception
    {
        final JsonGenerator json = context.getContext();
        json.writeStartObject();
        {
            json.writeStringField(name.toString(), gauge.value().toString());
        }
        json.writeEndObject();
        l.countDown();
    }

    // POST http://track.restfulmetrics.com/apps/your_app_id/metrics.json
    // curl http://track.restfulmetrics.com/apps/your_app_id/metrics.json -X POST -u your_api_key -D '{ metric: { name: "some_metric", value: 1 } }'
    /*
        {
            metric: {
                name: "some_metric",
                value: 1,
                distinct_id: "5fe5cccc3f5efaf5afe53fe5"
            }
        }
     */
    @Override
    protected HTTPReporter.Context<JsonGenerator> createContext(HttpURLConnection connection)
    {
        return new Context<JsonGenerator>(connection)
        {
            private JsonGenerator json;

            @Override
            public JsonGenerator getContext()
            {
                try
                {
                    connection.setRequestMethod("POST");
                    connection.addRequestProperty("Authorization", "<insert-api-key-here>");
                    json = new JsonFactory().createJsonGenerator(connection.getOutputStream());
                    json.writeStartObject();
                    json.writeObjectFieldStart("metric");
                    return json;
                }
                catch(Exception e)
                {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }

            @Override
            public void finishRequest()
            {
                try
                {
                    json.writeEndObject();
                    json.writeEndObject();
                    json.flush();
                    json.close();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
        };
    }

    static final CountDownLatch l = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException, URISyntaxException
    {
        new Thread(SampleHTTPReporter.class.getSimpleName() + " dummy listener")
        {
            {
                setDaemon(true);
            }

            @Override
            public void run()
            {
                try
                {
                    ServerSocket server = new ServerSocket(9999);
                    final Socket socket = server.accept();
                    l.await();
                    final InputStream input = socket.getInputStream();
                    final byte[] request = new byte[input.available()];
                    input.read(request);
                    socket.getOutputStream()
                            .write("HTTP 200 OK\nDate: Fri, 31 Dec 1999 23:59:59 GMT\nContent-Type: text/plain\nContent-Length: 0\n".getBytes());
                    socket.getOutputStream().flush();
                    socket.getOutputStream().close();
                    socket.close();
                    server.close();
                    System.err.println("Received:\n" + new String(request));
                }
                catch(Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();
        final MetricsRegistry reg = new MetricsRegistry();
        final CounterMetric counter = reg.newCounter(SampleHTTPReporter.class, "foo");
        counter.inc(5555);
        final SampleHTTPReporter reporter = new SampleHTTPReporter(reg);
        reporter.start(1, TimeUnit.SECONDS);
        l.await();
        Thread.sleep(200);
        reporter.shutdown();
    }
}