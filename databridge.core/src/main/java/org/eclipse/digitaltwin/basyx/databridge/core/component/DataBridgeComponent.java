/*******************************************************************************
 * Copyright (C) 2021 the Eclipse BaSyx Authors
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * SPDX-License-Identifier: MIT
 ******************************************************************************/
package org.eclipse.digitaltwin.basyx.databridge.core.component;

import java.util.HashMap;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.health.HealthCheckRegistry;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.health.DefaultHealthCheckRegistry;
import org.apache.camel.impl.health.RoutesHealthCheckRepository;
import org.eclipse.basyx.components.IComponent;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.IRouteCreatorFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RoutesConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.event.EventRouteConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.event.EventRouteCreatorFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.request.RequestRouteConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.request.RequestRouteCreatorFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.timer.TimerRouteConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.timer.TimerRouteCreatorFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.health.routebuilder.HealthCheckRouteBuilder;
import org.eclipse.digitaltwin.basyx.databridge.core.routebuilder.DataBridgeRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core DataBridge component which can run the updater if routes configuration is
 * provided
 *
 * @author haque, fischer, danish
 *
 */
public class DataBridgeComponent implements IComponent {
	private static Logger logger = LoggerFactory.getLogger(DataBridgeComponent.class);
	private DataBridgeRouteBuilder orchestrator;

	protected CamelContext camelContext;

	public DataBridgeComponent(RoutesConfiguration configuration) {
		camelContext = new DefaultCamelContext();
		orchestrator = new DataBridgeRouteBuilder(configuration, getRouteCreatorFactoryMapDefault());
	}

	private static Map<String, IRouteCreatorFactory> getRouteCreatorFactoryMapDefault() {
		Map<String, IRouteCreatorFactory> defaultRouteCreatorFactoryMap = new HashMap<>();
		defaultRouteCreatorFactoryMap.put(EventRouteConfiguration.ROUTE_TRIGGER, new EventRouteCreatorFactory());
		defaultRouteCreatorFactoryMap.put(TimerRouteConfiguration.ROUTE_TRIGGER, new TimerRouteCreatorFactory());
		defaultRouteCreatorFactoryMap.put(RequestRouteConfiguration.ROUTE_TRIGGER, new RequestRouteCreatorFactory());

		return defaultRouteCreatorFactoryMap;
	}

	/**
	 * Starts the Camel component
	 */
	@Override
	public void startComponent() {
		startRoutes();
	}

	public void startRoutes() {
		try {
			configureHealthCheck();
			camelContext.addRoutes(orchestrator);
			camelContext.start();
			logger.info("Updater started");
		} catch (Exception e) {
			e.printStackTrace();
			camelContext = null;
		}
	}

	private void configureHealthCheck() throws Exception {
		camelContext.setLoadHealthChecks(true);
		camelContext.setExtension(HealthCheckRegistry.class, configureHealthCheckRegistry());
		camelContext.addRoutes(new HealthCheckRouteBuilder());
	}

	private HealthCheckRegistry configureHealthCheckRegistry() {
		HealthCheckRegistry registry = new DefaultHealthCheckRegistry();
		registry.register(new RoutesHealthCheckRepository());
		return registry;
	}

	/**
	 * Stops the Camel component
	 */
	@Override
	public void stopComponent() {
		if (camelContext != null && !camelContext.isStopped()) {
			camelContext.stop();
			logger.info("Updater stopped");
		}
	}
}
