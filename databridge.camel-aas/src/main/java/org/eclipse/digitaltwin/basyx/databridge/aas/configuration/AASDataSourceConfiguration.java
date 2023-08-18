package org.eclipse.digitaltwin.basyx.databridge.aas.configuration;

import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataSourceConfiguration;

public class AASDataSourceConfiguration extends DataSourceConfiguration{

	private String idShortPath;
	private String submodelEndpoint;
	
	public AASDataSourceConfiguration(String uniqueId, String serverUrl, int serverPort, String idShortPath, String submodelEndpoint) {
		super(uniqueId, serverUrl, serverPort);
		this.submodelEndpoint = submodelEndpoint;
	}

	public String getPath() {
		return idShortPath;
	}

	public void setPath(String idShortPath) {
		this.idShortPath = idShortPath;
	}
	
	public String getSubmodelEndpoint() {
		return submodelEndpoint;
	}

	public void setSubmodelEndpoint(String endpoint) {
		this.submodelEndpoint = endpoint;
	}
	
	@Override
	public String getConnectionURI() {
		return "aas:" + getSubmodelEndpoint() + "?propertyPath=" + getPath();
	}

}
