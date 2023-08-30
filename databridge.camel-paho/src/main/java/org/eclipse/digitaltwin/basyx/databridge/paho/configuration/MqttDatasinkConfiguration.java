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
package org.eclipse.digitaltwin.basyx.databridge.paho.configuration;

import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataSinkConfiguration;

/**
 * An implementation of MQTT producer configuration
 * @author rana
 *
 */
public class MqttDatasinkConfiguration extends DataSinkConfiguration {
		
	private String serverUrl;
	private int serverPort;
	private String topic;
	private String userName;
	private String password;
	private String clientId;

	public MqttDatasinkConfiguration() {
	}

	public MqttDatasinkConfiguration(String uniqueId, String serverUrl, int serverPort, String topic, String clientId,
			String userName, String password) {
		super(uniqueId);
		setServerUrl(serverUrl);
		setServerPort(serverPort);
		setTopic(topic);
		setClientId(clientId);
		setUserName(userName);
		setPassword(password);
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	private String buildOptions() {
		
		StringBuilder options = new StringBuilder();
		options.append("?brokerUrl=tcp://");
		options.append(getServerUrl());
		options.append(":");
		options.append(Integer.toString(getServerPort()));
		options.append("&clientId=");
		options.append(getClientId());
		
		if (!getUserName().isBlank()) {
			options.append("&userName=");
			options.append(getUserName());
			options.append("&password=");
			options.append(getPassword());
		}
		return options.toString();
	}

	@Override
	public String getConnectionURI() {
		
		StringBuilder connUri = new StringBuilder();
		connUri.append("paho:");
		connUri.append(getTopic());
		connUri.append(buildOptions());
		connUri.append(getServerPort());
		
		return connUri.toString();
	}
}
