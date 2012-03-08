package com.yammer.metrics.weld.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.interceptor.Interceptor;
import javax.interceptor.InterceptorBinding;

import com.yammer.metrics.annotation.Gauge;

/**
 * Marker {@link Annotation} used to wire Weld {@link Interceptor}s for methods annotated with {@link Gauge}.
 */
@InterceptorBinding
@Retention(RUNTIME)
@Target({ METHOD, FIELD, TYPE })
public @interface WeldGauge {}
