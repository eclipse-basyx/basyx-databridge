/*******************************************************************************
 * Copyright (C) 2023 the Eclipse BaSyx Authors
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
package org.eclipse.digitaltwin.basyx.components.databridge.executable;

import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.digitaltwin.basyx.components.databridge.core.configuration.entity.DataSinkConfiguration;
import org.eclipse.digitaltwin.basyx.components.databridge.core.configuration.entity.DataSourceConfiguration;
import org.eclipse.digitaltwin.basyx.components.databridge.core.configuration.entity.DataTransformerConfiguration;
import org.eclipse.digitaltwin.basyx.components.databridge.core.configuration.factory.DataSinkConfigurationFactory;
import org.eclipse.digitaltwin.basyx.components.databridge.core.configuration.factory.DataSourceConfigurationFactory;
import org.eclipse.digitaltwin.basyx.components.databridge.core.configuration.factory.DataTransformerConfigurationFactory;
import org.eclipse.digitaltwin.basyx.components.databridge.core.configuration.factory.RoutesConfigurationFactory;
import org.eclipse.digitaltwin.basyx.components.databridge.core.configuration.route.core.RoutesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used for configuring the routes
 *
 * @author danish
 */
public class RoutesConfigurationLoader {
	private static Logger logger = LoggerFactory.getLogger(RoutesConfigurationLoader.class);

	private String configFilePath;

	public RoutesConfigurationLoader(String configFilePath) {
		this.configFilePath = configFilePath;
	}

	public RoutesConfiguration create() {
		ClassLoader loader = DataBridgeExecutable.class.getClassLoader();
		RoutesConfiguration configuration = new RoutesConfiguration();

		configureRouteFactory(loader, configuration);

		addAvailableConfigurations(configuration);
		
		return configuration;
	}

	private void addAvailableConfigurations(RoutesConfiguration configuration) {
		Set<String> configFiles = DataBridgeUtils.getFiles(getConfigFilePath());

		Set<Class<?>> classes = DataBridgeUtils.findAllConfigurationFactoryClasses(DataBridgeUtils.PACKAGE_PREFIX);

		classes.stream().forEach(clazz -> DataBridgeUtils
				.getAllConfigFilesMatchingInputFileName(configFiles,
						DataBridgeUtils.findAvailableConfigurationFile(clazz))
				.stream()
				.forEach(userInputConfigFilename -> addConfiguration(clazz, userInputConfigFilename, configuration)));
	}
	
	private void addConfiguration(Class<?> clazz, String userInputConfigFilename, RoutesConfiguration configuration) {
		if (DataSourceConfigurationFactory.class.isAssignableFrom(clazz)) {
			addDataSource(getConfigFilePath() + "/" + userInputConfigFilename, clazz, configuration);
		} else if (DataTransformerConfigurationFactory.class.isAssignableFrom(clazz)) {
			addTransformer(getConfigFilePath() + "/" + userInputConfigFilename, clazz, configuration);
		} else if (DataSinkConfigurationFactory.class.isAssignableFrom(clazz)) {
			addDataSink(getConfigFilePath() + "/" + userInputConfigFilename, clazz, configuration);
		} else {
			logger.info("Config file doesn't match to consumer, transformer, or server!");
		}
	}
	
	private String getConfigFilePath() {
		return configFilePath;
	}

	private void configureRouteFactory(ClassLoader loader, RoutesConfiguration configuration) {
		RoutesConfigurationFactory routesFactory = new RoutesConfigurationFactory(getConfigFilePath() + "/routes.json",
				loader);
		configuration.addRoutes(routesFactory.create());
	}
	
	private static void addDataSource(String path, Class<?> clazz, RoutesConfiguration configuration) {
		@SuppressWarnings("unchecked")
		List<DataSourceConfiguration> configurations = (List<DataSourceConfiguration>) DataBridgeUtils.getConfigurations(path,
				DataBridgeExecutable.class.getClassLoader(), clazz);

		configuration.addDatasources(configurations);

		logger.info("Data source added - {}", FilenameUtils.getName(path));
	}
	
	private static void addTransformer(String path, Class<?> clazz, RoutesConfiguration configuration) {
		@SuppressWarnings("unchecked")
		List<DataTransformerConfiguration> configurations = (List<DataTransformerConfiguration>) DataBridgeUtils.getConfigurations(path,
				DataBridgeExecutable.class.getClassLoader(), clazz);

		configuration.addTransformers(configurations);

		logger.info("Data Transformer added - {}", FilenameUtils.getName(path));
	}

	private static void addDataSink(String path, Class<?> clazz, RoutesConfiguration configuration) {
		@SuppressWarnings("unchecked")
		List<DataSinkConfiguration> configurations = (List<DataSinkConfiguration>) DataBridgeUtils.getConfigurations(path,
				DataBridgeExecutable.class.getClassLoader(), clazz);

		configuration.addDatasinks(configurations);

		logger.info("Data sink added - {}", FilenameUtils.getName(path));
	}
}
