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
package org.eclipse.digitaltwin.basyx.databridge.opcua.configuration;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataSourceConfiguration;

/**
 * An implementation of OpcUa consumer configuration
 * 
 * @author Daniele Rossi
 *
 */
public class OpcuaConsumerConfiguration extends DataSourceConfiguration {
	private String pathToService;
	private String nodeInformation;
	private String username;
	private String password;
	private Long requestedPublishingInterval;

	public OpcuaConsumerConfiguration() {
	}

	public OpcuaConsumerConfiguration(String uniqueId, String serverUrl, int serverPort, String pathToService,
			String nodeInformation, String username, String password, Long requestedPublishingInterval) {
		super(uniqueId, serverUrl, serverPort);
		this.pathToService = pathToService;
		this.nodeInformation = nodeInformation;
		this.username = username;
		this.password = password;
		this.requestedPublishingInterval = requestedPublishingInterval;
	}

	public String getPathToService() {
		return pathToService;
	}

	public void setPathToService(String pathToService) {
		this.pathToService = pathToService;
	}

	public String getNodeInformation() {
		return nodeInformation;
	}

	public void setNodeInformation(String nodeInformation) {
		this.nodeInformation = nodeInformation;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Long getRequestedPublishingInterval() {
		return requestedPublishingInterval;
	}

	public void setRequestedPublishingInterval(Long requestedPublishingInterval) {
		this.requestedPublishingInterval = requestedPublishingInterval;
	}

	public String getConnectionURI() {
		String credentials = username == null || password == null ? "" : username + ":" + password + "@";
		return "milo-client:opc.tcp://" + credentials + getServerUrl() + ":" + getServerPort() + "/" + pathToService
				+ "?allowedSecurityPolicies=None&node=RAW(" + nodeInformation + ")" + getRequestedPublishingIntervalIfConfigured();
	}

	private String getRequestedPublishingIntervalIfConfigured() {
		return this.requestedPublishingInterval == null ? StringUtils.EMPTY : "&requestedPublishingInterval=" + this.requestedPublishingInterval;
	}
}
