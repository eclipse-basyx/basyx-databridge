package org.eclipse.digitaltwin.basyx.databridge.paho.configuration;

import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataSinkConfiguration;

public class MqttDatasinkConfiguration extends DataSinkConfiguration{
	
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

	public String getPassword() { // change to private?
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
	public Object getConnectionURI() {
		// TODO Auto-generated method stub
		return "paho:" + getTopic() + buildOptions() + ":" + getServerPort();
	}

}
