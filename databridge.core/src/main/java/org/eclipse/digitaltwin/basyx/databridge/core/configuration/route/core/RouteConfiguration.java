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

public class RouteConfiguration {
	private String trigger;
	private String routeId;
	private String datasource;
	private List<String> transformers = new ArrayList<>();
	private List<String> datasinks = new ArrayList<>();

	private Map<String, Object> triggerData = new HashMap<>();

	public RouteConfiguration() {
	}

	/**
	 * @param trigger
	 * @param routeId
	 * @param datasource
	 * @param transformers
	 * @param datasinks
	 */
	public RouteConfiguration(String trigger, String datasource, List<String> transformers, List<String> datasinks) {
		this.trigger = trigger;
		this.datasource = datasource;
		this.transformers = transformers;
		this.datasinks = datasinks;
	}

	public RouteConfiguration(RouteConfiguration configuration) {
		this(configuration.getRouteTrigger(), configuration.getDatasource(), configuration.getTransformers(), configuration.getDatasinks());
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

}
