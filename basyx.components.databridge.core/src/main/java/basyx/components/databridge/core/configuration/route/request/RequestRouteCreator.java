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
package basyx.components.databridge.core.configuration.route.request;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import basyx.components.databridge.core.configuration.delegator.handler.ResponseOkCodeHandler;
import basyx.components.databridge.core.configuration.route.core.AbstractRouteCreator;
import basyx.components.databridge.core.configuration.route.core.RouteConfiguration;
import basyx.components.databridge.core.configuration.route.core.RoutesConfiguration;

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
	protected void configureRoute(RouteConfiguration routeConfig, String dataSourceEndpoint, String[] dataSinkEndpoints,
			String[] dataTransformerEndpoints, String routeId) {
		String delegatorEndpoint = ((RequestRouteConfiguration) routeConfig).getRequestEndpointURI();

		RouteDefinition routeDefinition = createRoute(dataSourceEndpoint, routeId, delegatorEndpoint);

		if (!(dataTransformerEndpoints == null || dataTransformerEndpoints.length == 0)) {
			routeDefinition.to(dataTransformerEndpoints).log("Transformer : " + routeId);
		}

		routeDefinition.bean(new ResponseOkCodeHandler());
	}

	private RouteDefinition createRoute(String dataSourceEndpoint, String routeId, String delegatorEndpoint) {
		return getRouteBuilder().from(delegatorEndpoint).routeId(routeId).pollEnrich(dataSourceEndpoint, TIMEOUT)
				.log("Source : " + routeId);
	}
}
