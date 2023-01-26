/*******************************************************************************
* Copyright (C) 2021 the Eclipse BaSyx Authors
* 
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/

* 
* SPDX-License-Identifier: EPL-2.0
******************************************************************************/

package org.eclipse.digitaltwin.basyx.components.databridge.camelkafka.configuration.factory;

import org.eclipse.digitaltwin.basyx.components.databridge.camelkafka.configuration.KafkaConsumerConfiguration;
import org.eclipse.digitaltwin.basyx.components.databridge.core.configuration.factory.DataSourceConfigurationFactory;

/**
 * A default configuration factory for kafka from a default file location
 * @author haque
 *
 */
public class KafkaDefaultConfigurationFactory extends DataSourceConfigurationFactory {
	public static final String DEFAULT_FILE_PATH = "kafkaconsumer.json";
	
	public KafkaDefaultConfigurationFactory(ClassLoader loader) {
		super(DEFAULT_FILE_PATH, loader, KafkaConsumerConfiguration.class);
	}
	
	public KafkaDefaultConfigurationFactory(String filePath, ClassLoader loader) {
		super(filePath, loader, KafkaConsumerConfiguration.class);
	}
}
