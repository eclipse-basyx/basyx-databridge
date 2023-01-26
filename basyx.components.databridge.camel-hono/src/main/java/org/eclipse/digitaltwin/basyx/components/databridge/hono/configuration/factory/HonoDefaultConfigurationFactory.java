/*******************************************************************************
* Copyright (C) 2021 the Eclipse BaSyx Authors
* 
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/

* 
* SPDX-License-Identifier: EPL-2.0
******************************************************************************/

package org.eclipse.digitaltwin.basyx.components.databridge.hono.configuration.factory;

import org.eclipse.digitaltwin.basyx.components.databridge.core.configuration.factory.DataSourceConfigurationFactory;
import org.eclipse.digitaltwin.basyx.components.databridge.hono.configuration.HonoConsumerConfiguration;

/**
 * A default configuration factory for Hono from a default path
 * @author haque
 *
 */
public class HonoDefaultConfigurationFactory extends DataSourceConfigurationFactory {
	public static final String DEFAULT_FILE_PATH = "honoconsumer.json";
	
	public HonoDefaultConfigurationFactory(ClassLoader loader) {
		super(DEFAULT_FILE_PATH, loader, HonoConsumerConfiguration.class);
	}
	
	public HonoDefaultConfigurationFactory(String filePath, ClassLoader loader) {
		super(filePath, loader, HonoConsumerConfiguration.class);
	}
}
