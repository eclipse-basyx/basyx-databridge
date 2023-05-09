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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataSinkConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataSourceConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataTransformerConfiguration;

/**
 * An implementation of configurations of all the routes
 *
 * @author haque
 *
 */
public class RoutesConfiguration {
	private Map<String, DataSourceConfiguration> datasources = new HashMap<>();
	private Map<String, DataTransformerConfiguration> transformers = new HashMap<>();
	private Map<String, DataSinkConfiguration> datasinks = new HashMap<>();
	private List<RouteConfiguration> routes = new ArrayList<>();

	public RoutesConfiguration() {
	}

	public RoutesConfiguration(List<DataSourceConfiguration> datasources, List<DataTransformerConfiguration> transformers, List<DataSinkConfiguration> datasinks, List<RouteConfiguration> routes) {
		addDatasources(datasources);
		addTransformers(transformers);
		addDatasinks(datasinks);
		addRoutes(routes);
	}

	public RoutesConfiguration(Map<String, DataSourceConfiguration> datasources, Map<String, DataTransformerConfiguration> transformers, Map<String, DataSinkConfiguration> datasinks, List<RouteConfiguration> routes) {
		setDatasources(datasources);
		setTransformers(transformers);
		setDatasinks(datasinks);
		addRoutes(routes);
	}

	public Map<String, DataSourceConfiguration> getDatasources() {
		return datasources;
	}

	public void setDatasources(Map<String, DataSourceConfiguration> datasources) {
		this.datasources = datasources;
	}

	public void addDatasources(List<DataSourceConfiguration> datasources) {
		for (DataSourceConfiguration datasource : datasources) {
			addDatasource(datasource);
		}
	}

	public void addDatasource(DataSourceConfiguration datasource) {
		this.datasources.put(datasource.getUniqueId(), datasource);
	}

	public Map<String, DataTransformerConfiguration> getTransformers() {
		return transformers;
	}

	public void setTransformers(Map<String, DataTransformerConfiguration> transformers) {
		this.transformers = transformers;
	}

	public void addTransformers(List<DataTransformerConfiguration> datatransformers) {
		for (DataTransformerConfiguration datatransformer : datatransformers) {
			addTransformer(datatransformer);
		}
	}

	public void addTransformer(DataTransformerConfiguration datatransformer) {
		this.transformers.put(datatransformer.getUniqueId(), datatransformer);
	}

	public Map<String, DataSinkConfiguration> getDatasinks() {
		return datasinks;
	}

	public void setDatasinks(Map<String, DataSinkConfiguration> datasinks) {
		this.datasinks = datasinks;
	}

	public void addDatasinks(List<DataSinkConfiguration> datasinks) {
		for (DataSinkConfiguration datasink : datasinks) {
			addDatasink(datasink);
		}
	}

	public void addDatasink(DataSinkConfiguration datasink) {
		this.datasinks.put(datasink.getUniqueId(), datasink);
	}

	public List<RouteConfiguration> getRoutes() {
		return routes;
	}

	public void setRoutes(List<RouteConfiguration> routes) {
		this.routes = routes;
	}

	public void addRoutes(List<RouteConfiguration> routes) {
		this.routes.addAll(routes);
	}

	public void addRoute(RouteConfiguration route) {
		this.routes.add(route);
	}
}
