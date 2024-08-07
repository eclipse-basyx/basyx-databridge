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
package org.eclipse.digitaltwin.basyx.databridge.core.routebuilder;

import java.util.Map;

import org.apache.camel.CamelContextAware;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.tooling.model.Strings;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.IRouteCreator;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.IRouteCreatorFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RouteConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RoutesConfiguration;

/**
 * This factory is used to create the apache camel routes for the data bridge
 * component
 *
 * @author fischer, mateusmolina
 *
 */
public class DataBridgeRouteBuilder extends RouteBuilder {
	private static final String ROUTE_ID_PREFIX = "route";
	private RoutesConfiguration routesConfiguration;
	private Map<String, IRouteCreatorFactory> routeCreatorFactoryMap;

	public DataBridgeRouteBuilder(RoutesConfiguration configuration, Map<String, IRouteCreatorFactory> routeCreatorFactoryMap) {
		this.routesConfiguration = configureRouteIds(configuration);
		this.routeCreatorFactoryMap = routeCreatorFactoryMap;
	}

	@Override
	public void configure() throws Exception {
		for (RouteConfiguration routeConfig : routesConfiguration.getRoutes()) {
			IRouteCreator routeCreator = routeCreatorFactoryMap.get(routeConfig.getRouteTrigger()).create(this, routesConfiguration);

			setCamelContextInRouteEntities();

			routeCreator.addRouteToRouteBuilder(routeConfig);
		}
	}

	private RoutesConfiguration configureRouteIds(RoutesConfiguration routesConfiguration) {
		long incrementalId = 1;
		for (RouteConfiguration route : routesConfiguration.getRoutes()) {
			if (Strings.isNullOrEmpty(route.getRouteId())) {
				route.setRouteId(ROUTE_ID_PREFIX + incrementalId);
				incrementalId++;
			}
		}

		return routesConfiguration;
	}

	private void setCamelContextInRouteEntities() {
		routesConfiguration.getDatasinks().entrySet().forEach(c -> CamelContextAware.trySetCamelContext(c.getValue(), getCamelContext()));
		routesConfiguration.getDatasources().entrySet().forEach(c -> CamelContextAware.trySetCamelContext(c.getValue(), getCamelContext()));
		routesConfiguration.getTransformers().entrySet().forEach(c -> CamelContextAware.trySetCamelContext(c.getValue(), getCamelContext()));
	}
}
