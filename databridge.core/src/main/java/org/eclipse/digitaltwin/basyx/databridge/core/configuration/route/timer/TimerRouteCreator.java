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
package org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.timer;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.AbstractRouteCreator;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RouteConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RouteCreatorHelper;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RoutesConfiguration;

public class TimerRouteCreator extends AbstractRouteCreator {
	private static final Long TIMEOUT = 5000L;

	public TimerRouteCreator(RouteBuilder routeBuilder, RoutesConfiguration routesConfiguration) {
		super(routeBuilder, routesConfiguration);
	}

	@Override
	protected void configureRoute(RouteConfiguration routeConfig, String dataSourceEndpoint, String[] dataSinkEndpoints, String[] dataTransformerEndpoints, String routeId) {
		TimerRouteConfiguration timerConfig = (TimerRouteConfiguration) routeConfig;
		String timerEndpoint = RouteCreatorHelper.getDataSourceEndpoint(getRoutesConfiguration(), timerConfig.getTimerName());
		RouteDefinition routeDefinition = getRouteBuilder().from(timerEndpoint).pollEnrich(dataSourceEndpoint, TIMEOUT).routeId(routeId).to("log:" + routeId);

		if (!(dataTransformerEndpoints == null || dataTransformerEndpoints.length == 0)) {
			routeDefinition.to(dataTransformerEndpoints).to("log:" + routeId);
		}

		routeDefinition.to(dataSinkEndpoints[0]).to("log:" + routeId);
	}

}