/*******************************************************************************
* Copyright (C) 2022 the Eclipse BaSyx Authors
* 
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/

* 
* SPDX-License-Identifier: EPL-2.0
******************************************************************************/

package basyx.components.databridge.aas.configuration.factory;

import basyx.components.databridge.aas.configuration.AASDatasourceConfiguration;
import basyx.components.databridge.core.configuration.factory.DataSourceConfigurationFactory;

/**
 * A default configuration factory for AAS producer data source
 * 
 * @author mateusmolina-iese
 *
 */
public class AASDatasourceDefaultConfigurationFactory extends DataSourceConfigurationFactory {
	private static final String FILE_PATH = "aasserver_datasource.json";

	public AASDatasourceDefaultConfigurationFactory(ClassLoader loader) {
		super(FILE_PATH, loader, AASDatasourceConfiguration.class);
	}

	public AASDatasourceDefaultConfigurationFactory(String filePath, ClassLoader loader) {
		super(filePath, loader, AASDatasourceConfiguration.class);
	}
}
