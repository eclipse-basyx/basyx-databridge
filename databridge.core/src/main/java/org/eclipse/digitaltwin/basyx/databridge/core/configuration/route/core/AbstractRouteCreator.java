/*******************************************************************************
 * Copyright (C) 2022 the Eclipse BaSyx Authors
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

package org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core;

import java.util.Map;

import org.apache.camel.builder.RouteBuilder;

public abstract class AbstractRouteCreator implements IRouteCreator {
	private RouteBuilder routeBuilder;
	private RoutesConfiguration routesConfiguration;

	/**
	 * @param routeBuilder
	 * @param routesConfiguration
	 */
	public AbstractRouteCreator(RouteBuilder routeBuilder, RoutesConfiguration routesConfiguration) {
		this.routeBuilder = routeBuilder;
		this.routesConfiguration = routesConfiguration;
	}

	protected RoutesConfiguration getRoutesConfiguration() {
		return routesConfiguration;
	}

	protected RouteBuilder getRouteBuilder() {
		return routeBuilder;
	}

	@Override
	public void addRouteToRouteBuilder(RouteConfiguration routeConfig) {
		String dataSourceEndpoint = RouteCreatorHelper.getDataSourceEndpoint(routesConfiguration, routeConfig.getDatasource());
		String[] dataSinkEndpoints = RouteCreatorHelper.getDataSinkEndpoints(routesConfiguration, routeConfig.getDatasinks());
		String[] dataTransformerEndpoints = RouteCreatorHelper.getDataTransformerEndpoints(routesConfiguration, routeConfig.getTransformers());
		Map<String, String[]> datasinkMapping = RouteCreatorHelper.getDataSinkMapping(routesConfiguration, routeConfig.getDatasinkMappingConfiguration());
		String routeId = routeConfig.getRouteId();

		if (datasinkMapping == null || datasinkMapping.isEmpty()) {
			configureRoute(routeConfig, dataSourceEndpoint, dataSinkEndpoints, dataTransformerEndpoints, routeId);
		} else {
			configureRoute(routeConfig, dataSourceEndpoint, dataSinkEndpoints, dataTransformerEndpoints, datasinkMapping, routeId);
		}
	}

	protected abstract void configureRoute(RouteConfiguration routeConfig, String dataSourceEndpoint, String[] dataSinkEndpoints, String[] dataTransformerEndpoints, String routeId);

	protected abstract void configureRoute(RouteConfiguration routeConfig, String dataSourceEndpoint, String[] dataSinkEndpoints, String[] dataTransformerEndpoints, Map<String, String[]> DataSinkMapping, String routeId);
}
