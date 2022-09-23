/*******************************************************************************
* Copyright (C) 2022 the Eclipse BaSyx Authors
* 
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/

* 
* SPDX-License-Identifier: EPL-2.0
******************************************************************************/

package basyx.components.databridge.camelpaho.configuration;

import basyx.components.databridge.core.configuration.entity.DataSinkConfiguration;

/**
 * An implementation of MQTT provider configuration
 * 
 * @author mateusmolina-iese
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

	private String getPassword() {
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
		if (!getUserName().isEmpty()) {
			options = options.concat("&userName=").concat(getUserName()).concat("&password=").concat(getPassword());
		}
		return options;
	}

	@Override
	public String getConnectionURI() {
		return "paho:" + getTopic() + buildOptions() + ":" + getServerPort();

	}

}
