package com.yammer.metrics.guice;

import java.lang.reflect.Method;

public interface MetricsBaptizer
{
    String giveNameTo(String annotationName, Method method);
}