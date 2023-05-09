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
package org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.event;

import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.AbstractRouteCreator;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RouteConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RouteCreatorHelper;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RoutesConfiguration;

public class EventRouteCreator extends AbstractRouteCreator {

	public EventRouteCreator(RouteBuilder routeBuilder, RoutesConfiguration routesConfiguration) {
		super(routeBuilder, routesConfiguration);
	}

	@Override
	protected void configureRoute(RouteConfiguration routeConfiguration, Object dataSourceEndpoint, Object[] dataSinkEndpoints, Object[] dataTransformerEndpoints, String routeId) {
		RouteDefinition routeDefinition = configureSource(dataSourceEndpoint, routeId);

		configureTransformers(dataTransformerEndpoints, routeId, routeDefinition);

		configureSinks(dataSinkEndpoints, routeId, routeDefinition);
	}

	private void configureSinks(Object[] dataSinkEndpoints, String routeId, RouteDefinition routeDefinition) {
		if (dataSinkEndpoints instanceof Endpoint[]) {
			routeDefinition.to((Endpoint) dataSinkEndpoints[0]).to("log:" + routeId);
		} else {
			routeDefinition.to((String) dataSinkEndpoints[0]).to("log:" + routeId);
		}
	}

	private void configureTransformers(Object[] dataTransformerEndpoints, String routeId,
			RouteDefinition routeDefinition) {
		if (!(dataTransformerEndpoints == null || dataTransformerEndpoints.length == 0)) {
			if (dataTransformerEndpoints instanceof Endpoint[]) {
				routeDefinition.to(RouteCreatorHelper.castToEndpointArray(dataTransformerEndpoints)).to("log:" + routeId);
			} else {
				routeDefinition.to(RouteCreatorHelper.castToStringArray(dataTransformerEndpoints)).to("log:" + routeId);
			}
		}
	}

	private RouteDefinition configureSource(Object dataSourceEndpoint, String routeId) {
		if (dataSourceEndpoint instanceof Endpoint) {
			return getRouteBuilder().from((Endpoint) dataSourceEndpoint).routeId(routeId).to("log:" + routeId);
		}
		
		return getRouteBuilder().from((String) dataSourceEndpoint).routeId(routeId).to("log:" + routeId);
	}

}
