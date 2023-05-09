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

import java.util.List;

import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RouteConfiguration;
import org.springframework.http.HttpMethod;

/**
 * A connection of a single route (source, transformer(s)) with delegation
 * request trigger
 *
 * @author danish
 *
 */
public class RequestRouteConfiguration extends RouteConfiguration {
	public static final String ROUTE_TRIGGER = "request";
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String PATH = "path";
	public static final String REQUEST_COMPONENT = "jetty";
	public static final String REQUEST_PROTOCOL = "http";
	public static final String HTTP_METHOD_RESTRICT_PARAMETER = "httpMethodRestrict=" + HttpMethod.GET;

	private String host;
	private String port;
	private String servicePath;

	public RequestRouteConfiguration(String datasource, List<String> transformers, List<String> datasinks) {
		super(ROUTE_TRIGGER, datasource, transformers, datasinks);
	}

	public RequestRouteConfiguration(RouteConfiguration configuration) {
		super(configuration);
		host = (String) getTriggerData().get(HOST);
		port = (String) getTriggerData().get(PORT);
		servicePath = (String) getTriggerData().get(PATH);
	}

	public String getPath() {
		return servicePath;
	}

	public void setPath(String path) {
		this.servicePath = path;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getRequestEndpointURI() {
		return REQUEST_COMPONENT + ":" + REQUEST_PROTOCOL + "://" + getHost() + ":" + getPort() + getPath() + "?"
				+ HTTP_METHOD_RESTRICT_PARAMETER;
	}
}
