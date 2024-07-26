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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author DataBridge authors, jungjan
 */
public class RouteConfiguration {
	private String trigger;
	private String routeId;
	private String datasource;
	private List<String> transformers = new ArrayList<>();
	private List<String> datasinks = new ArrayList<>();
	private Map<String, String[]> datasinkMappingConfiguration;

	private Map<String, Object> triggerData = new HashMap<>();

	public RouteConfiguration() {
	}

	/**
	 * Constructs a new RouteConfiguration object with a mapping configuration to
	 * map distinct transformators to multiple datasinks.
	 *
	 * @param trigger
	 *            the trigger for the route configuration
	 * @param routeId
	 *            the ID of the route
	 * @param datasource
	 *            the datasource associated with the route
	 * @param transformers
	 *            the list of transformers to be applied in the route
	 * @param datasinks
	 *            the list of datasinks to which data should be routed
	 * @param datasinkMappingConfiguration
	 *            the mapping configuration for datasinks, mapping each datasink to
	 *            its corresponding configuration
	 */
	public RouteConfiguration(String trigger, String datasource, List<String> transformers, List<String> datasinks, Map<String, String[]> datasinkMappingConfiguration) {
		this.trigger = trigger;
		this.datasource = datasource;
		this.transformers = transformers;
		this.datasinks = datasinks;
		this.datasinkMappingConfiguration = datasinkMappingConfiguration;
	}

	/**
	 * Constructs a new RouteConfiguration object without a mapping configuration to
	 * map distinct transformators to multiple datasinks. I.e., all transformators
	 * would be equally applied to all data sinks.
	 *
	 * @param trigger
	 *            the trigger for the route configuration
	 * @param routeId
	 *            the ID of the route (optional, can be null)
	 * @param datasource
	 *            the datasource associated with the route
	 * @param transformers
	 *            the list of transformers to be applied in the route
	 * @param datasinks
	 *            the list of datasinks to which data should be routed
	 */
	public RouteConfiguration(String trigger, String datasource, List<String> transformers, List<String> datasinks) {
		this.trigger = trigger;
		this.datasource = datasource;
		this.transformers = transformers;
		this.datasinks = datasinks;
	}

	public RouteConfiguration(RouteConfiguration configuration) {
		this(configuration.getRouteTrigger(), configuration.getDatasource(), configuration.getTransformers(), configuration.getDatasinks(), configuration.getDatasinkMappingConfiguration());
		setRouteId(configuration.getRouteId());
		this.triggerData = configuration.triggerData;
	}

	protected Map<String, Object> getTriggerData() {
		return triggerData;
	}

	public List<String> getDatasinks() {
		return datasinks;
	}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

	public String getRouteTrigger() {
		return trigger;
	}

	public List<String> getTransformers() {
		return transformers;
	}

	public void setTransformers(List<String> transformers) {
		this.transformers = transformers;
	}

	public String getDatasource() {
		return datasource;
	}

	public void setDatasource(String datasource) {
		this.datasource = datasource;
	}

	public Map<String, String[]> getDatasinkMappingConfiguration() {
		return datasinkMappingConfiguration;
	}

	public void setDatasinkMappingConfiguration(Map<String, String[]> datasinkMappingConfiguration) {
		this.datasinkMappingConfiguration = datasinkMappingConfiguration;
	}

}
