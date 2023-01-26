/*******************************************************************************
* Copyright (C) 2021 the Eclipse BaSyx Authors
* 
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/

* 
* SPDX-License-Identifier: EPL-2.0
******************************************************************************/

package org.eclipse.digitaltwin.basyx.components.databridge.camelopcua.configuration.factory;

import org.eclipse.digitaltwin.basyx.components.databridge.camelopcua.configuration.OpcuaConsumerConfiguration;
import org.eclipse.digitaltwin.basyx.components.databridge.core.configuration.factory.DataSourceConfigurationFactory;

/**
 * A default configuration factory for OpcUa from a default file location
 * 
 * @author Daniele Rossi
 *
 */
public class OpcuaDefaultConfigurationFactory extends DataSourceConfigurationFactory {
	public static final String DEFAULT_FILE_PATH = "opcuaconsumer.json";

	public OpcuaDefaultConfigurationFactory(ClassLoader loader) {
		super(DEFAULT_FILE_PATH, loader, OpcuaConsumerConfiguration.class);
	}

	public OpcuaDefaultConfigurationFactory(String filePath, ClassLoader loader) {
		super(filePath, loader, OpcuaConsumerConfiguration.class);
	}
}
