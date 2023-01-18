/*******************************************************************************
* Copyright (C) 2021 the Eclipse BaSyx Authors
* 
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/

* 
* SPDX-License-Identifier: EPL-2.0
******************************************************************************/

package basyx.components.databridge.aas.configuration;

import basyx.components.databridge.core.configuration.entity.DataSinkConfiguration;

/**
 * An implementation of AAS data sink configuration
 * @author haque
 *
 */
public class AASDatasinkConfiguration extends DataSinkConfiguration {
	private static final String PROPERTY_TYPE = "PROPERTY";

	private String type;
	private String submodelEndpoint;
	private String idShortPath;

	public AASDatasinkConfiguration() {}
	
	public AASDatasinkConfiguration(String submodelEndpoint, String idShortPath, String uniqueId) {
		super(uniqueId);
		this.type = PROPERTY_TYPE;
		this.submodelEndpoint = submodelEndpoint;
		this.idShortPath = idShortPath;
	}
	
	public AASDatasinkConfiguration(String aasEndpoint, String propertyPath) {
		this(aasEndpoint, propertyPath, null);
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSubmodelEndpoint() {
		return submodelEndpoint;
	}

	public void setSubmodelEndpoint(String endpoint) {
		this.submodelEndpoint = endpoint;
	}

	public String getIdShortPath() {
		return idShortPath;
	}

	public void setIdShortPath(String path) {
		this.idShortPath = path;
	}

	@Override
	public String getConnectionURI() {
		String endpointDefinition = "aas:";
		endpointDefinition += this.submodelEndpoint;
		endpointDefinition += "?propertyPath=" + this.idShortPath;
		return endpointDefinition;
	}
}
