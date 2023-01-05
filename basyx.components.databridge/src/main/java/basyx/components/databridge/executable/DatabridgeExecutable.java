/*******************************************************************************
 * Copyright (C) 2022 the Eclipse BaSyx Authors
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
package basyx.components.databridge.executable;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import basyx.components.databridge.core.component.UpdaterComponent;
import basyx.components.databridge.core.configuration.entity.DataSinkConfiguration;
import basyx.components.databridge.core.configuration.entity.DataSourceConfiguration;
import basyx.components.databridge.core.configuration.entity.DataTransformerConfiguration;
import basyx.components.databridge.core.configuration.factory.ConfigurationFactory;
import basyx.components.databridge.core.configuration.factory.DataSinkConfigurationFactory;
import basyx.components.databridge.core.configuration.factory.DataSourceConfigurationFactory;
import basyx.components.databridge.core.configuration.factory.DataTransformerConfigurationFactory;
import basyx.components.databridge.core.configuration.factory.RoutesConfigurationFactory;
import basyx.components.databridge.core.configuration.route.core.RoutesConfiguration;

/**
 * Starts the stand-alone databridge component
 *
 * @author danish
 */
public class DatabridgeExecutable {
	private static Logger logger = LoggerFactory.getLogger(DatabridgeExecutable.class);

	public static final String PACKAGE_PREFIX = "basyx.components.databridge";
	protected static final String PACKAGE_SUFFIX = "configuration.factory";
	protected static final String FACTORY_SUFFIX = "DefaultConfigurationFactory";
	public static final String DEFAULT_CONFIG_PATH = "/usr/share/config";
	protected static final String CONSUMER = "consumer";
	protected static final String TRANSFORMER = "transformer";
	protected static final String SERVER = "server";
	protected static final String FIELD_FILE_PATH = "DEFAULT_FILE_PATH";
	protected static final String CREATE_METHOD_NAME = "create";
	
	private static String configFilePath;

	public static void main(String[] args) throws IllegalArgumentException, SecurityException {
		ClassLoader loader = DatabridgeExecutable.class.getClassLoader();
		RoutesConfiguration configuration = new RoutesConfiguration();

		configureRouteFactory(loader, configuration);

		addAvailableConfigurations(configuration);

		UpdaterComponent updater = new UpdaterComponent(configuration);
		updater.startComponent();
	}

	private static void addAvailableConfigurations(RoutesConfiguration configuration) {
		Set<String> configFiles = listFiles(getConfigFilePath());

		Set<Class<?>> classes = findAllConfigurationFactoryClasses(PACKAGE_PREFIX);
		
		classes.stream().forEach(
				clazz -> findAvailableConfigurationFilesAndAddConfiguration(clazz, configuration, configFiles, getConfigFilePath()));
	}

	private static void configureRouteFactory(ClassLoader loader, RoutesConfiguration configuration) {
		RoutesConfigurationFactory routesFactory = new RoutesConfigurationFactory(getConfigFilePath() + "/routes.json", loader);
		configuration.addRoutes(routesFactory.create());
	}

	public static void findAvailableConfigurationFilesAndAddConfiguration(Class<?> clazz,
			RoutesConfiguration configuration, Set<String> configFiles, String pathPrefix) {
		try {
			final String fileNameDefinedInConfigFactory = (String) clazz.getField(FIELD_FILE_PATH).get(null);

			configFiles.stream()
					.filter(userInputConfigFilename -> userInputConfigFilename.equals(fileNameDefinedInConfigFactory))
					.forEach(userInputConfigFilename -> detectConfigurationFileTypeAndAddToConfiguration(
							userInputConfigFilename, clazz, configuration, pathPrefix));
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			logger.info("In class {} the field {} doesn't found!", clazz.getName(), FIELD_FILE_PATH);
		}
	}

	private static void detectConfigurationFileTypeAndAddToConfiguration(String userInputConfigFilename, Class<?> clazz,
			RoutesConfiguration configuration, String pathPrefix) {
		if (DataSourceConfigurationFactory.class.isAssignableFrom(clazz)) {
			addDataSource(pathPrefix + "/" + userInputConfigFilename, clazz, configuration);
		} else if (DataTransformerConfigurationFactory.class.isAssignableFrom(clazz)) {
			addTransformer(pathPrefix + "/" + userInputConfigFilename, clazz, configuration);
		} else if (DataSinkConfigurationFactory.class.isAssignableFrom(clazz)) {
			addDataSink(pathPrefix + "/" + userInputConfigFilename, clazz, configuration);
		} else {
			logger.info("Config file doesn't match to consumer, transformer, or server!");
		}
	}

	private static void addDataSink(String path, Class<?> clazz, RoutesConfiguration configuration) {
		@SuppressWarnings("unchecked")
		List<DataSinkConfiguration> configurations = (List<DataSinkConfiguration>) getConfigurations(path,
				DatabridgeExecutable.class.getClassLoader(), clazz);

		configuration.addDatasinks(configurations);

		logger.info("Data sink added - {}", FilenameUtils.getName(path));
	}

	private static void addTransformer(String path, Class<?> clazz, RoutesConfiguration configuration) {
		@SuppressWarnings("unchecked")
		List<DataTransformerConfiguration> configurations = (List<DataTransformerConfiguration>) getConfigurations(path,
				DatabridgeExecutable.class.getClassLoader(), clazz);

		configuration.addTransformers(configurations);

		logger.info("Data Transformer added - {}", FilenameUtils.getName(path));
	}

	private static void addDataSource(String path, Class<?> clazz, RoutesConfiguration configuration) {
		@SuppressWarnings("unchecked")
		List<DataSourceConfiguration> configurations = (List<DataSourceConfiguration>) getConfigurations(path,
				DatabridgeExecutable.class.getClassLoader(), clazz);

		configuration.addDatasources(configurations);

		logger.info("Data source added - {}", FilenameUtils.getName(path));
	}

	public static Set<Class<?>> findAllConfigurationFactoryClasses(String packageName) {
		Reflections reflections = new Reflections(new ConfigurationBuilder().forPackage(packageName)
				.addScanners(Scanners.SubTypes.filterResultsBy(s -> true)));

		return reflections.getSubTypesOf(ConfigurationFactory.class).stream().collect(Collectors.toSet());
	}

	@SuppressWarnings("unchecked")
	private static <ConfigurationClass> Object getConfigurations(String path, ClassLoader loader, Class<?> configurationClass) {
		Constructor<?> constructor = getConstructor(configurationClass);

		ConfigurationClass configFactory = null;
		
		Object listConfig = null;
		try {
			configFactory = (ConfigurationClass) constructor.newInstance(path, loader);
			listConfig = invokeCreateMethodAndGetConfigurations(configFactory);
			
			logger.info("Instantiated class {}", configFactory.getClass().toString());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException();
		}

		return listConfig;
	}

	private static <ConfigurationClass> Object invokeCreateMethodAndGetConfigurations(ConfigurationClass configurationFactory)
			throws IllegalAccessException, InvocationTargetException {
		Object configurations = null;
		try {
			Method method = configurationFactory.getClass().getMethod(CREATE_METHOD_NAME);
			configurations = method.invoke(configurationFactory);
			
			logger.info("Retrieved method {}", method.getName());
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException();
		}
		
		return configurations;
	}

	private static Constructor<?> getConstructor(Class<?> nodeClass) {
		Constructor<?> constructor = null;
		try {
			constructor = nodeClass.getConstructor(String.class, ClassLoader.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException();
		}

		return constructor;
	}

	public static Set<String> listFiles(String directory) {
		return Stream.of(new File(directory).listFiles()).filter(file -> !file.isDirectory()).map(File::getName)
				.collect(Collectors.toSet());
	}
	
	public static void setConfigFilePath(String configFilePath) {
		DatabridgeExecutable.configFilePath = configFilePath;
	}
	
	public static String getConfigFilePath() {
		if(configFilePath != null && !configFilePath.isBlank()) {
			return configFilePath;
		}
		
		return DEFAULT_CONFIG_PATH;
	}
}
