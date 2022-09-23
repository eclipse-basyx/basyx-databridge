/*******************************************************************************
* Copyright (C) 2022 the Eclipse BaSyx Authors
* 
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/

* 
* SPDX-License-Identifier: EPL-2.0
******************************************************************************/

package basyx.components.databridge.aas.configuration;

import basyx.components.databridge.core.configuration.entity.DataSourceConfiguration;

/**
 * An implementation of AAS data source configuration
 * 
 * @author mateusmolina-iese
 *
 */
public class AASDatasourceConfiguration extends DataSourceConfiguration {

	private String path;

	public AASDatasourceConfiguration(String uniqueId, String serverUrl, int serverPort, String path) {
		super(uniqueId, serverUrl, serverPort);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String getConnectionURI() {
		return "aas:" + getServerUrl() + "?propertyPath=" + getPath();
	}
}
