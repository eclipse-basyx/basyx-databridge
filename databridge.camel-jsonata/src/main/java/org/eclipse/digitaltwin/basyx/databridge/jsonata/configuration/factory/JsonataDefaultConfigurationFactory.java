/*******************************************************************************
 * Copyright (C) 2021 the Eclipse BaSyx Authors
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * SPDX-License-Identifier: MIT
 ******************************************************************************/
package org.eclipse.digitaltwin.basyx.databridge.jsonata.configuration.factory;

import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataTransformerConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.factory.DataTransformerConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.jsonata.configuration.JsonataTransformerConfiguration;

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
		
		((JsonataTransformerConfiguration) config).setQueryPath(FilenameUtils.getFullPath(filePath) + queryPath);
	}

	private boolean isConfiguredFromDefaultPath() {
		return filePath == null || filePath.isEmpty();
	}
}
