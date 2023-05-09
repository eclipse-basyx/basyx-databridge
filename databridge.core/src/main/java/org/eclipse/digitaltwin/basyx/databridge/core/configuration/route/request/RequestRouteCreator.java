/*******************************************************************************
 * Copyright (C) 2023 the Eclipse BaSyx Authors
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
package org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.request;

import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.delegator.handler.ResponseOkCodeHandler;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.AbstractRouteCreator;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RouteConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RouteCreatorHelper;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RoutesConfiguration;

/**
 * Configures and creates the request route
 *
 * @author danish
 *
 */
public class RequestRouteCreator extends AbstractRouteCreator {
	private static final Long TIMEOUT = 5000L;

	public RequestRouteCreator(RouteBuilder routeBuilder, RoutesConfiguration routesConfiguration) {
		super(routeBuilder, routesConfiguration);
	}

	@Override
	protected void configureRoute(RouteConfiguration routeConfig, Object dataSourceEndpoint, Object[] dataSinkEndpoints,
			Object[] dataTransformerEndpoints, String routeId) {
		String delegatorEndpoint = ((RequestRouteConfiguration) routeConfig).getRequestEndpointURI();

		RouteDefinition routeDefinition = createRoute(dataSourceEndpoint, routeId, delegatorEndpoint);

		configureTransformers(dataTransformerEndpoints, routeId, routeDefinition);

		routeDefinition.bean(new ResponseOkCodeHandler());
	}

	private RouteDefinition createRoute(Object dataSourceEndpoint, String routeId, String delegatorEndpoint) {
		RouteDefinition routeDefinition = getRouteBuilder().from(delegatorEndpoint).routeId(routeId);
		
		configureSource(routeDefinition, dataSourceEndpoint, routeId);
		
		return routeDefinition;
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

	private void configureSource(RouteDefinition routeDefinition, Object dataSourceEndpoint, String routeId) {
		if (dataSourceEndpoint instanceof Endpoint) {
			routeDefinition.pollEnrich(((Endpoint) dataSourceEndpoint).getEndpointUri(), TIMEOUT).log("Source : " + routeId);
		} else {
			routeDefinition.pollEnrich((String) dataSourceEndpoint, TIMEOUT).log("Source : " + routeId);
		}
	}
}
