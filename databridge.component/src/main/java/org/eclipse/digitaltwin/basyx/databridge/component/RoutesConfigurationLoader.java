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
package org.eclipse.digitaltwin.basyx.databridge.component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataSinkConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataSourceConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataTransformerConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.factory.DataSinkConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.factory.DataSourceConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.factory.DataTransformerConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.factory.RoutesConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RoutesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Used for configuring the routes
 *
 * @author danish
 */
public class RoutesConfigurationLoader {
	private static Logger logger = LoggerFactory.getLogger(RoutesConfigurationLoader.class);

	private final static String TEMPORARY_CONFIG_DIRECTORY = System.getProperty("java.io.tmpdir") + "/dataBridge";
	public static final String JSONATA_ENV_VAR_NAME = "jsonatatransformers";
	
	public RoutesConfigurationLoader() {
		resetFileDirectory();
		createFilesFromEnvironmentVariables();
	}

	public RoutesConfigurationLoader(String configFilePath) {
		resetFileDirectory();
		copyConfigsToFileDirectory(configFilePath);
		createFilesFromEnvironmentVariables();
	}

	public RoutesConfiguration create() {
		ClassLoader loader = DataBridgeExecutable.class.getClassLoader();
		RoutesConfiguration configuration = new RoutesConfiguration();

		configureRouteFactory(loader, configuration);

		addAvailableConfigurations(configuration);

		return configuration;
	}

	private static Set<Class<?>> findAllConfigurationFactoryClasses() {
		return DataBridgeUtils.findAllConfigurationFactoryClasses(DataBridgeUtils.PACKAGE_PREFIX);
	}

	private static void createFilesFromEnvironmentVariables() {
		createRoutesFile();
		createConfigurationFactoryFiles();
		createJSONataFiles();
	}

	private static void createJSONataFiles() {
		String[] jsonataFiles = getJSONataFilesFromEnv();

		for (String jsonataFile : jsonataFiles) {
			createFileFromEnvironmentVariable(jsonataFile);
		}
	}

	private static String[] getJSONataFilesFromEnv() {
		Gson gson = new Gson();
		String jsonataFilesFromEnv = System.getenv(JSONATA_ENV_VAR_NAME);
		if (jsonataFilesFromEnv == null)
			return new String[0];

		return gson.fromJson(jsonataFilesFromEnv, String[].class);
	}

	private static void createConfigurationFactoryFiles() {
		Set<Class<?>> classes = findAllConfigurationFactoryClasses();
		classes.forEach(RoutesConfigurationLoader::createFileFromEnvironmentVariable);
	}

	private static void createRoutesFile() {
		createFileFromEnvironmentVariable(RoutesConfigurationFactory.DEFAULT_FILE_PATH);
	}

	private static void resetFileDirectory() {
		try {
			File directory = new File(TEMPORARY_CONFIG_DIRECTORY);
			FileUtils.deleteDirectory(directory);
			directory.mkdir();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void addAvailableConfigurations(RoutesConfiguration configuration) {
		Set<Class<?>> classes = findAllConfigurationFactoryClasses();

		Set<String> configFiles = DataBridgeUtils.getFiles(TEMPORARY_CONFIG_DIRECTORY);

		logger.info("Found files: " + configFiles);

		classes.stream().forEach(clazz -> DataBridgeUtils
				.getAllConfigFilesMatchingInputFileName(configFiles,
						DataBridgeUtils.findAvailableConfigurationFile(clazz))
				.stream()
				.forEach(userInputConfigFilename -> addConfiguration(clazz, userInputConfigFilename, configuration)));
	}

	private static void createFileFromEnvironmentVariable(Class<?> clazz) {
		String fileName = DataBridgeUtils.findAvailableConfigurationFile(clazz);

		if (fileName == null)
			return;

		createFileFromEnvironmentVariable(fileName);
	}

	private static void createFileFromEnvironmentVariable(String variableName) {
		String fileContent = System.getenv(variableName);

		if (fileContent == null)
			fileContent = System.getenv(variableName.replaceAll("\\.", "_"));

		if (fileContent == null)
			return;

		logger.info("Creating file " + variableName + " from environment with content " + fileContent);
		writeFileToTempDirectory(variableName, fileContent);
	}

	private static void writeFileToTempDirectory(String fileName, String fileContent) {
		String pathToFile = getFilePathInFileDirectory(fileName);

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathToFile))) {
			writer.write(fileContent);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void copyConfigsToFileDirectory(String filePath) {
		File source = new File(filePath);
		File dest = new File(TEMPORARY_CONFIG_DIRECTORY);

		try {
			FileUtils.copyDirectory(source, dest);
		} catch (IOException e) {
			logger.warn("Could not reach config files files from " + filePath + ". "
					+ "If no files are configured via environment variables, this is an error.");
		}
	}

	private static void addConfiguration(Class<?> clazz, String userInputConfigFilename, RoutesConfiguration configuration) {
		if (DataSourceConfigurationFactory.class.isAssignableFrom(clazz)) {
			addDataSource(getFilePathInFileDirectory(userInputConfigFilename), clazz, configuration);
		} else if (DataTransformerConfigurationFactory.class.isAssignableFrom(clazz)) {
			addTransformer(getFilePathInFileDirectory(userInputConfigFilename), clazz, configuration);
		} else if (DataSinkConfigurationFactory.class.isAssignableFrom(clazz)) {
			addDataSink(getFilePathInFileDirectory(userInputConfigFilename), clazz, configuration);
		} else {
			logger.info("Config file doesn't match to consumer, transformer, or server!");
		}
	}

	private static String getFilePathInFileDirectory(String fileName) {
		return TEMPORARY_CONFIG_DIRECTORY + "/" + fileName;
	}

	private static void configureRouteFactory(ClassLoader loader, RoutesConfiguration configuration) {
		RoutesConfigurationFactory routesFactory = new RoutesConfigurationFactory(getFilePathInFileDirectory(RoutesConfigurationFactory.DEFAULT_FILE_PATH), loader);
		configuration.addRoutes(routesFactory.create());
	}

	private static void addDataSource(String path, Class<?> clazz, RoutesConfiguration configuration) {
		@SuppressWarnings("unchecked")
		List<DataSourceConfiguration> configurations = (List<DataSourceConfiguration>) DataBridgeUtils.getConfigurations(path, DataBridgeExecutable.class.getClassLoader(), clazz);

		configuration.addDatasources(configurations);

		logger.info("Data source added - {}", FilenameUtils.getName(path));
	}

	private static void addTransformer(String path, Class<?> clazz, RoutesConfiguration configuration) {
		@SuppressWarnings("unchecked")
		List<DataTransformerConfiguration> configurations = (List<DataTransformerConfiguration>) DataBridgeUtils.getConfigurations(path, DataBridgeExecutable.class.getClassLoader(), clazz);

		configuration.addTransformers(configurations);

		logger.info("Data Transformer added - {}", FilenameUtils.getName(path));
	}

	private static void addDataSink(String path, Class<?> clazz, RoutesConfiguration configuration) {
		@SuppressWarnings("unchecked")
		List<DataSinkConfiguration> configurations = (List<DataSinkConfiguration>) DataBridgeUtils.getConfigurations(path, DataBridgeExecutable.class.getClassLoader(), clazz);

		configuration.addDatasinks(configurations);

		logger.info("Data sink added - {}", FilenameUtils.getName(path));
	}
}
