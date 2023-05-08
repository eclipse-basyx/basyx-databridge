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
package org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.camel.Endpoint;

public class RouteCreatorHelper {
	private RouteCreatorHelper() {
	}

	public static Object getDataSourceEndpoint(RoutesConfiguration routesConfiguration, String dataSourceId) {
		return routesConfiguration.getDatasources().get(dataSourceId).getConnectionURI();
	}

	public static Object getDataSinkEndpoint(RoutesConfiguration routesConfiguration, String dataSinkId) {
		return routesConfiguration.getDatasinks().get(dataSinkId).getConnectionURI();
	}

	public static Object[] getDataSinkEndpoints(RoutesConfiguration routesConfiguration, List<String> dataSinkIdList) {
		List<Object> endpoints = new ArrayList<>();
		for (String dataSinkId : dataSinkIdList) {
			endpoints.add(routesConfiguration.getDatasinks().get(dataSinkId).getConnectionURI());
		}

		return endpoints.toArray(new Object[0]);
	}

	public static Object[] getDataTransformerEndpoints(RoutesConfiguration routesConfiguration, List<String> transformerIdList) {
		List<Object> endpoints = new ArrayList<>();
		for (String transformerId : transformerIdList) {
			endpoints.add(routesConfiguration.getTransformers().get(transformerId).getConnectionURI());
		}
		return endpoints.toArray(new Object[0]);
	}
	
	public static String[] castToStringArray(Object[] endpoints) {
		return Arrays.copyOf(endpoints, endpoints.length, String[].class);
	}

	public static Endpoint[] castToEndpointArray(Object[] endpoints) {
		return Arrays.copyOf(endpoints, endpoints.length, Endpoint[].class);
	}
}
