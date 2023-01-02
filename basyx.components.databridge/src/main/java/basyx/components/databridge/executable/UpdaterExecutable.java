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
import basyx.components.databridge.core.configuration.factory.DataSinkConfigurationFactory;
import basyx.components.databridge.core.configuration.factory.DataSourceConfigurationFactory;
import basyx.components.databridge.core.configuration.factory.DataTransformerConfigurationFactory;
import basyx.components.databridge.core.configuration.factory.RoutesConfigurationFactory;
import basyx.components.databridge.core.configuration.route.core.RoutesConfiguration;

/**
 * Starts the stand-alone updater component
 *
 * @author danish
 */
public class UpdaterExecutable {
	private static Logger logger = LoggerFactory.getLogger(UpdaterExecutable.class);
	
	public static final String PACKAGE_PREFIX = "basyx.components.databridge";
	protected static final String PACKAGE_SUFFIX = "configuration.factory";
	protected static final String FACTORY_SUFFIX = "DefaultConfigurationFactory";
	public static final String PATH = "/usr/share/config";
	protected static final String CONSUMER = "consumer";
	protected static final String TRANSFORMER = "transformer";
	protected static final String SERVER = "server";
	protected static final String FIELD_FILE_PATH = "FILE_PATH";

	public static void main(String[] args) throws IllegalArgumentException, SecurityException {
		ClassLoader loader = UpdaterExecutable.class.getClassLoader();
		RoutesConfiguration configuration = new RoutesConfiguration();
		
		RoutesConfigurationFactory routesFactory = new RoutesConfigurationFactory(
				PATH + "/routes.json", loader);
		configuration.addRoutes(routesFactory.create());
		
		Set<String> configFiles = listFiles(PATH);

		Set<Class<?>> classes = findAllConfigurationFactoryClasses(PACKAGE_PREFIX);
		classes.stream().forEach(clazz -> findAvailableConfigurationFilesAndAddConfiguration(clazz, configuration, configFiles, PATH));

		UpdaterComponent updater = new UpdaterComponent(configuration);
		updater.startComponent();
	}

	public static void findAvailableConfigurationFilesAndAddConfiguration(Class<?> clazz, RoutesConfiguration configuration, Set<String> configFiles, String pathPrefix) {
		String fileNameDefinedInConfigFactory = null;
		try {
			fileNameDefinedInConfigFactory = (String) clazz.getField(FIELD_FILE_PATH).get(UpdaterExecutable.class);
			logger.info("Class field name : {}", fileNameDefinedInConfigFactory);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		
		final String fileName = fileNameDefinedInConfigFactory;
		
		configFiles.stream().filter(userInputConfigFilename -> userInputConfigFilename.equals(fileName)).forEach(userInputConfigFilename -> detectConfigurationFileTypeAndAddToConfiguration(userInputConfigFilename, clazz, configuration, pathPrefix));
	}

	private static void detectConfigurationFileTypeAndAddToConfiguration(String userInputConfigFilename, Class<?> clazz, RoutesConfiguration configuration, String pathPrefix) {
		if (userInputConfigFilename.contains(CONSUMER)) {
			addDataSource(pathPrefix + "/" + userInputConfigFilename, clazz, configuration);
		} else if (userInputConfigFilename.contains(TRANSFORMER)) {
			addTransformer(pathPrefix + "/" + userInputConfigFilename, clazz, configuration);
		} else if (userInputConfigFilename.contains(SERVER)) {
			addDataSink(pathPrefix + "/" + userInputConfigFilename, clazz, configuration);
		} else {
			logger.info("Config file doesn't match to consumer, transformer, or server!");
		}
	}

	private static void addDataSink(String path, Class<?> clazz, RoutesConfiguration configuration) {
		DataSinkConfigurationFactory defaultConfigFactory = getConfigFactory(path, UpdaterExecutable.class.getClassLoader(),
				DataSinkConfigurationFactory.class, clazz);

		configuration.addDatasinks(defaultConfigFactory.create());
		
		logger.info("Data sink added - {}", FilenameUtils.getName(path));
	}

	private static void addTransformer(String path, Class<?> clazz, RoutesConfiguration configuration) {
		DataTransformerConfigurationFactory defaultConfigFactory = getConfigFactory(path, UpdaterExecutable.class.getClassLoader(),
				DataTransformerConfigurationFactory.class, clazz);

		configuration.addTransformers(defaultConfigFactory.create());
		
		logger.info("Data Transformer added - {}", FilenameUtils.getName(path));
	}

	private static void addDataSource(String path, Class<?> clazz, RoutesConfiguration configuration) {
		DataSourceConfigurationFactory defaultConfigFactory = getConfigFactory(path, UpdaterExecutable.class.getClassLoader(),
				DataSourceConfigurationFactory.class, clazz);

		configuration.addDatasources(defaultConfigFactory.create());
		
		logger.info("Data source added - {}", FilenameUtils.getName(path));
	}

	public static Set<Class<?>> findAllConfigurationFactoryClasses(String packageName) {
		Reflections reflections = new Reflections(new ConfigurationBuilder().forPackage(packageName)
				.addScanners(Scanners.SubTypes.filterResultsBy(s -> true)));
		
		return reflections.getSubTypesOf(Object.class).stream()
				.filter(clazz -> clazz.getName().contains(FACTORY_SUFFIX)).collect(Collectors.toSet());
	}

	@SuppressWarnings("unchecked")
	private static <configFactoryClass> configFactoryClass getConfigFactory(String path, ClassLoader loader,
			Class<?> configFactoryClass, Class<?> nodeClass) {
		Constructor<?> constructor = getConstructor(path, nodeClass);
		
		configFactoryClass mqttConfigFactory = null;
		try {
			mqttConfigFactory = (configFactoryClass) constructor.newInstance(path, loader);
			logger.info("Instantiated class {}", mqttConfigFactory.getClass().toString());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
			logger.info("Problem while instantiating the config factory class for config file {}", path);
		}
		
		return mqttConfigFactory;
	}

	private static Constructor<?> getConstructor(String path, Class<?> nodeClass) {
		Constructor<?> constructor = null;
		try {
			constructor = nodeClass.getConstructor(String.class, ClassLoader.class);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			logger.info("Problem while calling constructor for config file {}", path);
		}
		
		return constructor;
	}

	public static Set<String> listFiles(String dir) {
		return Stream.of(new File(dir).listFiles()).filter(
				file -> !file.isDirectory() && "json".equals(FilenameUtils.getExtension(file.getAbsolutePath())))
				.map(File::getName).collect(Collectors.toSet());
	}
}
