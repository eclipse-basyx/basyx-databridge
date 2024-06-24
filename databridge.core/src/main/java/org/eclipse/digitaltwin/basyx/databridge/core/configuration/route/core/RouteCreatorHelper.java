/*******************************************************************************
 * Copyright (C) 2024 the Eclipse BaSyx Authors
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author DataBridge authors, jungjan
 */
public class RouteCreatorHelper {
	private RouteCreatorHelper() {
	}

	public static String getDataSourceEndpoint(RoutesConfiguration routesConfiguration, String dataSourceId) {
		return routesConfiguration.getDatasources()
				.get(dataSourceId)
				.getConnectionURI();
	}

	public static String getDataSinkEndpoint(RoutesConfiguration routesConfiguration, String dataSinkId) {
		return routesConfiguration.getDatasinks()
				.get(dataSinkId)
				.getConnectionURI();
	}

	public static String[] getDataSinkEndpoints(RoutesConfiguration routesConfiguration, List<String> dataSinkIdList) {
		return dataSinkIdList.stream()
				.map(routesConfiguration.getDatasinks()::get)
				.map(dataSinkConfiguration -> dataSinkConfiguration.getConnectionURI())
				.toArray(String[]::new);

	}

	public static String[] getDataTransformerEndpoints(RoutesConfiguration routesConfiguration, List<String> transformerIdLists) {
		return transformerIdLists.stream()
				.map(routesConfiguration.getTransformers()::get)
				.map(dataTransformerConfiguration -> dataTransformerConfiguration.getConnectionURI())
				.toArray(String[]::new);
	}

	public static Map<String, String[]> getDataSinkMapping(RoutesConfiguration routesConfiguration, Map<String, String[]> datasinkMappingConfiguration) {
		if (datasinkMappingConfiguration == null || datasinkMappingConfiguration.isEmpty()) {
			return null;
		}
		Set<String> dataSinkIds = datasinkMappingConfiguration.keySet();
		Map<String, String> resolvedDataSinkEndpoints = dataSinkIds.stream()
				.collect(Collectors.toMap(dataSinkId -> dataSinkId, dataSinkId -> routesConfiguration.getDatasinks()
						.get(dataSinkId)
						.getConnectionURI()));

		return dataSinkIds.stream()
				.collect(Collectors.toMap(resolvedDataSinkEndpoints::get, dataSinkId -> getDataTransformerEndpoints(routesConfiguration, Arrays.asList(datasinkMappingConfiguration.get(dataSinkId)))));
	}
}
