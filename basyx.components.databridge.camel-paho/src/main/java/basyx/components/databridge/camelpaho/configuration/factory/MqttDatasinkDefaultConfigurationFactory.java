/*******************************************************************************
* Copyright (C) 2022 the Eclipse BaSyx Authors
* 
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/

* 
* SPDX-License-Identifier: EPL-2.0
******************************************************************************/

package basyx.components.databridge.camelpaho.configuration.factory;

import basyx.components.databridge.camelpaho.configuration.MqttDatasinkConfiguration;
import basyx.components.databridge.core.configuration.factory.DataSinkConfigurationFactory;

/**
 * A default configuration factory for MQTT Datasink from a default file path
 * 
 * @author mateusmolina-iese
 *
 */
public class MqttDatasinkDefaultConfigurationFactory extends DataSinkConfigurationFactory {
	private static final String FILE_PATH = "mqtt_datasink.json";

	public MqttDatasinkDefaultConfigurationFactory(ClassLoader loader) {
		super(FILE_PATH, loader, MqttDatasinkConfiguration.class);
	}

	public MqttDatasinkDefaultConfigurationFactory(String filePath, ClassLoader loader) {
		super(filePath, loader, MqttDatasinkConfiguration.class);
	}
}
