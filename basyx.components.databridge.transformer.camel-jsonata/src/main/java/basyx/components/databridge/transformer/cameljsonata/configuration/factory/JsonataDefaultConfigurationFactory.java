/*******************************************************************************
* Copyright (C) 2021 the Eclipse BaSyx Authors
* 
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/

* 
* SPDX-License-Identifier: EPL-2.0
******************************************************************************/

package basyx.components.databridge.transformer.cameljsonata.configuration.factory;

import java.util.List;

import org.apache.commons.io.FilenameUtils;

import basyx.components.databridge.core.configuration.entity.DataTransformerConfiguration;
import basyx.components.databridge.core.configuration.factory.DataTransformerConfigurationFactory;
import basyx.components.databridge.transformer.cameljsonata.configuration.JsonataTransformerConfiguration;

/**
 * Jsonata default configuration factory from default path
 * @author haque
 *
 */
public class JsonataDefaultConfigurationFactory extends DataTransformerConfigurationFactory {
	public static final String DEFAULT_FILE_PATH = "jsonatatransformer.json";
	
	private String filePath;
	
	public JsonataDefaultConfigurationFactory(ClassLoader loader) {
		super(DEFAULT_FILE_PATH, loader, JsonataTransformerConfiguration.class);
	}
	
	public JsonataDefaultConfigurationFactory(String filePath, ClassLoader loader) {
		super(filePath, loader, JsonataTransformerConfiguration.class);
		this.filePath = filePath;
	}
	
	@Override
	public List<DataTransformerConfiguration> create() {
		List<DataTransformerConfiguration> configs = super.create();
		
		configs.stream().forEach(this::setQueryPath);
		
		return configs;
	}

	private void setQueryPath(DataTransformerConfiguration config) {
		if(isConfiguredFromDefaultPath()) {
			return;
		}

		String queryPath = ((JsonataTransformerConfiguration) config).getQueryPath();
		
		((JsonataTransformerConfiguration) config).setQueryPath(FilenameUtils.getPath(filePath) + queryPath);
	}

	private boolean isConfiguredFromDefaultPath() {
		return filePath == null || filePath.isEmpty();
	}
}
