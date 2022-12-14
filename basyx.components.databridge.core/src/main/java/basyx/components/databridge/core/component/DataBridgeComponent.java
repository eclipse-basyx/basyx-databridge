/*******************************************************************************
* Copyright (C) 2021 the Eclipse BaSyx Authors
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/

*
* SPDX-License-Identifier: EPL-2.0
******************************************************************************/

package basyx.components.databridge.core.component;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.eclipse.basyx.components.IComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import basyx.components.databridge.core.configuration.route.core.IRouteCreatorFactory;
import basyx.components.databridge.core.configuration.route.core.RoutesConfiguration;
import basyx.components.databridge.core.configuration.route.event.EventRouteConfiguration;
import basyx.components.databridge.core.configuration.route.event.EventRouteCreatorFactory;
import basyx.components.databridge.core.configuration.route.timer.TimerRouteConfiguration;
import basyx.components.databridge.core.configuration.route.timer.TimerRouteCreatorFactory;
import basyx.components.databridge.core.routebuilder.DataBridgeRouteBuilder;

/**
 * Core Updater component which can run the updater if routes configuration is
 * provided
 *
 * @author haque, fischer
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
			camelContext.addRoutes(orchestrator);
			camelContext.start();
			logger.info("Updater started");
		} catch (Exception e) {
			e.printStackTrace();
			camelContext = null;
		}
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
