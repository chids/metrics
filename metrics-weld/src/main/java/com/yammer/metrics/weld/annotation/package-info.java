/**
 * This package contains rape-n-paste implementations of...
 * <ol>
 * <li>{@link com.yammer.metrics.annotation.ExceptionMetered}</li>
 * <li>{@link com.yammer.metrics.annotation.Gauge}</li>
 * <li>{@link com.yammer.metrics.annotation.Metered}</li>
 * <li>{@link com.yammer.metrics.weld.annotations.annotation.Timed}</li>
 * </ol>
 * ...which have been altered to work with Weld.
 * 
 * Ideally we'd like to reuse the annotations from the metrics-annotation module but for the following reasons we can't and won't: 
 * <ol>
 * <li>Weld requires annotations used for interception to be annotated with {@link javax.interceptor.InterceptorBinding}</li>
 * <li>Weld requires {@link javax.interceptor.Interceptor}s to be annotated with the annotation to intercept, hence the annotation must support {@link java.lang.annotation.ElementType#TYPE} (which the generic annotations from the metrics-annotation package doesn't, and rightfully so)</li>
 * <li>Weld requires properties of annotations used for interception to be annotated with {@link javax.enterprise.util.Nonbinding} to apply an interceptor on all annotated methods regardless of the annotations property values</li> 
 * <li>Java a annotations doesn't support inheritance (which if they did we could have simply extended the core annotations and decorated them with the additional annotations)</li>
 * </ol>
 */
package com.yammer.metrics.weld.annotation;