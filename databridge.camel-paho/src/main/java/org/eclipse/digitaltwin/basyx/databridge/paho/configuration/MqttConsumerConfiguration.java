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
package org.eclipse.digitaltwin.basyx.databridge.paho.configuration;

import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataSourceConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.core.security.BasicCredential;

/**
 * An implementation of MQTT consumer configuration
 * @author haque
 *
 */
public class MqttConsumerConfiguration extends DataSourceConfiguration {
	
	private static final String MQTT_PROTOCOL_NAME = "paho:";
	private static final String BROKER_URL_PARAM_NAME = "brokerUrl";
	private static final String BROKER_URL_PREFIX = "tcp://";
	private static final String USERNAME_PARAMETER = "userName";
	private static final String PASSWORD_PARAMETER = "password";
	
	private String topic;
	private String clientId;
	private BasicCredential credentials;
	
	public MqttConsumerConfiguration() {}
	
	public MqttConsumerConfiguration(String uniqueId, String serverUrl, int serverPort, String clientId, String topic, BasicCredential credentials) {
		super(uniqueId, serverUrl, serverPort);
		this.topic = topic;
		this.clientId = clientId;
		this.credentials = credentials;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public BasicCredential getCredentials() {
		return credentials;
	}

	public String getConnectionURI() {
		
		StringBuilder connectionUriBuilder = new StringBuilder();
		connectionUriBuilder.append(MQTT_PROTOCOL_NAME);
		connectionUriBuilder.append(topic);
		connectionUriBuilder.append("?");
		connectionUriBuilder.append(getMqttBrokerUrl(getServerUrl(), getServerPort()));
		
		addCredentialsIfConfigured(connectionUriBuilder);
		
		return connectionUriBuilder.toString();
	}
	
	private void addCredentialsIfConfigured(StringBuilder connectionUriBuilder) {
		
		if (credentials == null || credentials.getUserName().isBlank())
			return;
		
		connectionUriBuilder.append("&" + USERNAME_PARAMETER + "=");
		connectionUriBuilder.append(credentials.getUserName());
		connectionUriBuilder.append("&" + PASSWORD_PARAMETER + "=");
		connectionUriBuilder.append(credentials.getPassword());
	}

	private String getMqttBrokerUrl(String serverUrl, int port) {
		
		StringBuilder mqttBrokerUrlBuilder = new StringBuilder();
		mqttBrokerUrlBuilder.append(BROKER_URL_PARAM_NAME);
		mqttBrokerUrlBuilder.append("=");
		mqttBrokerUrlBuilder.append(BROKER_URL_PREFIX);
		mqttBrokerUrlBuilder.append(serverUrl);
		mqttBrokerUrlBuilder.append(":");
		mqttBrokerUrlBuilder.append(port);
		
		addClientIdIfConfigured(mqttBrokerUrlBuilder);
		
		return mqttBrokerUrlBuilder.toString();
	}

	private void addClientIdIfConfigured(StringBuilder mqttBrokerUrlBuilder) {
		
		if (clientId == null || clientId.isBlank())
			return;
		
		mqttBrokerUrlBuilder.append("&clientId=");
		mqttBrokerUrlBuilder.append(clientId);
	}
	
}
