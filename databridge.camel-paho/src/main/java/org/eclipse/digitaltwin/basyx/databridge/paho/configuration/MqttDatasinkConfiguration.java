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
import java.net.URISyntaxException;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataSinkConfiguration;

/**
 * An implementation of MQTT producer configuration
 * @author rana
 *
 */

public class MqttDatasinkConfiguration extends DataSinkConfiguration{
	
	private static final String MQTT_PROTOCOL_NAME = "paho:";
	
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
		String options = "";
		options = options.concat("?brokerUrl=tcp://").concat(getServerUrl()).concat(":")
				.concat(Integer.toString(getServerPort())).concat("&clientId=").concat(getClientId());
		if (!getUserName().isBlank()) {
			options = options.concat("&userName=").concat(getUserName()).concat("&password=").concat(getPassword());
		}
		return options;
	}

	@Override
	public String getConnectionURI() {
		URIBuilder uriBuilder = new URIBuilder();
		uriBuilder.setScheme(MQTT_PROTOCOL_NAME);
		uriBuilder.setPath(getTopic() + buildOptions() + ":");
		
		return buildConnectionUri(uriBuilder);
	}
	
	private String buildConnectionUri(URIBuilder uriBuilder) {
		try {
			return uriBuilder.build().toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new RuntimeException("Exception occurred while creating MqttEnd point");
		}
	}
}
