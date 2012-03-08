package com.yammer.metrics.weld.tests;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.runner.RunWith;

import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.weld.MetricsWeldExtension;
import com.yammer.metrics.weld.interceptor.TimedInterceptor;

@RunWith(Arquillian.class)
public abstract class AbstractWeldInterceptorTest { // implements MetricProcessor<Object> {

	@Inject
	protected MetricsRegistry registry;

	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap
				.create(JavaArchive.class)
				.addPackage(AbstractWeldInterceptorTest.class.getPackage())
				.addPackage(TimedInterceptor.class.getPackage())
				.addAsServiceProvider(Extension.class, MetricsWeldExtension.class)
				.addAsManifestResource("META-INF/beans.xml");
	}
}