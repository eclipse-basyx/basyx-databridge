package basyx.components.databridge.core.configuration.route.request;

import java.util.List;

import basyx.components.databridge.core.configuration.route.core.RouteConfiguration;

public class RequestRouteConfiguration extends RouteConfiguration {
	public static final String ROUTE_TRIGGER = "request";
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String PATH = "path";
	public static final String REQUEST_COMPONENT = "jetty";
	public static final String REQUEST_PROTOCOL = "http";

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
		System.out.println("Path name : " + servicePath);
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
		return REQUEST_COMPONENT + ":" + REQUEST_PROTOCOL + "://" + getHost() + ":" + getPort() + getPath();
	}
}
