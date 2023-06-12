package org.eclipse.digitaltwin.basyx.databridge.aas.configuration;

import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataSourceConfiguration;

public class AASDataSourceConfiguration extends DataSourceConfiguration{

	private String path;
	private String submodelEndpoint;
	
	public AASDataSourceConfiguration(String uniqueId, String serverUrl, int serverPort, String path, String submodelEndpoint) {
		super(uniqueId, serverUrl, serverPort);
		this.submodelEndpoint = submodelEndpoint;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public String getSubmodelEndpoint() {
		return submodelEndpoint;
	}

	public void setSubmodelEndpoint(String endpoint) {
		this.submodelEndpoint = endpoint;
	}
	
	@Override
	public Object getConnectionURI() {
		// TODO Auto-generated method stub
		return "aas:" + getSubmodelEndpoint() + "?propertyPath=" + getPath();
	}

}
