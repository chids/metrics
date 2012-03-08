package com.yammer.metrics.weld.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.interceptor.InterceptorBinding;

/**
 * See the package doc.
 */
@InterceptorBinding
@Retention(RUNTIME)
@Target({ METHOD, FIELD, TYPE })
public @interface WeldTimed {}
