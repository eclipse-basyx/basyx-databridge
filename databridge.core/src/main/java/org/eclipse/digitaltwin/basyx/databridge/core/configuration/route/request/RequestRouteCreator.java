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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.MulticastDefinition;
import org.apache.camel.model.RouteDefinition;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.delegator.handler.ResponseOkCodeHandler;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.AbstractRouteCreator;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RouteConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RoutesConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.event.EventRouteCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures and creates the request route
 *
 * @author danish
 *
 */
public class RequestRouteCreator extends AbstractRouteCreator {
	private static Logger logger = LoggerFactory.getLogger(EventRouteCreator.class);

	private static final Long TIMEOUT = 5000L;

	public RequestRouteCreator(RouteBuilder routeBuilder, RoutesConfiguration routesConfiguration) {
		super(routeBuilder, routesConfiguration);
	}

	@Override
	protected void configureRoute(RouteConfiguration routeConfig, String dataSourceEndpoint, String[] dataSinkEndpoints, String[] dataTransformerEndpoints, String routeId) {
		RouteDefinition routeDefinition = startRouteDefinition((RequestRouteConfiguration) routeConfig, dataSourceEndpoint, routeId);

		if (!(dataTransformerEndpoints == null || dataTransformerEndpoints.length == 0)) {
			routeDefinition.to(dataTransformerEndpoints)
					.log("Transformer : " + routeId);
		}
		routeDefinition.to(dataSinkEndpoints)
				.bean(new ResponseOkCodeHandler());
	}

	@Override
	protected void configureRoute(RouteConfiguration routeConfig, String dataSourceEndpoint, String[] dataSinkEndpoints, String[] dataTransformerEndpoints, Map<String, String[]> dataSinkMapping, String routeId) {
		MulticastDefinition routeDefinition = startRouteDefinition((RequestRouteConfiguration) routeConfig, dataSourceEndpoint, routeId).multicast();
		dataSinkMapping.forEach((dataSink, dataTransformers) -> routeDefinition.pipeline()
				.to(dataTransformers)
				.to(dataSink)
				.to("log:" + routeId));

		getUnmappedEndpoints(dataSinkEndpoints, dataSinkMapping).forEach(dataSink -> routeDefinition.to(dataSink)
				.to("log: " + routeId));

		routeDefinition.end()
				.bean(new ResponseOkCodeHandler());
	}

	private List<String> getUnmappedEndpoints(String[] dataSinkEndpoints, Map<String, String[]> dataSinkMapping) {
		return Arrays.stream(dataSinkEndpoints)
				.filter(Predicate.not(dataSinkMapping::containsKey))
				.collect(Collectors.toList());
	}

	private RouteDefinition startRouteDefinition(RequestRouteConfiguration requestRouteConfig, String dataSourceEndpoint, String routeId) {
		String delegatorEndpoint = requestRouteConfig.getRequestEndpointURI();
		return getRouteBuilder().from(delegatorEndpoint)
				.routeId(routeId)
				.pollEnrich(dataSourceEndpoint, TIMEOUT)
				.log("Source : " + routeId);
	}
}
