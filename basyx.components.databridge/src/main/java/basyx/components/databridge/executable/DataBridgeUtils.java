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
package basyx.components.databridge.executable;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import basyx.components.databridge.core.configuration.factory.ConfigurationFactory;

/**
 * Utility class for DataBridge
 * 
 * @author danish
 *
 */
public class DataBridgeUtils {
	private static Logger logger = LoggerFactory.getLogger(DataBridgeUtils.class);

	public static final String PACKAGE_PREFIX = "basyx.components.databridge";
	protected static final String PACKAGE_SUFFIX = "configuration.factory";
	protected static final String FACTORY_SUFFIX = "DefaultConfigurationFactory";
	protected static final String CONSUMER = "consumer";
	protected static final String TRANSFORMER = "transformer";
	protected static final String SERVER = "server";
	protected static final String FIELD_FILE_PATH = "DEFAULT_FILE_PATH";
	protected static final String CREATE_METHOD_NAME = "create";

	private DataBridgeUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static Set<String> getFiles(String directory) {
		return Stream.of(new File(directory).listFiles()).filter(file -> !file.isDirectory()).map(File::getName)
				.collect(Collectors.toSet());
	}

	public static Set<Class<?>> findAllConfigurationFactoryClasses(String packageName) {
		Reflections reflections = new Reflections(new ConfigurationBuilder().forPackage(packageName)
				.addScanners(Scanners.SubTypes.filterResultsBy(s -> true)));

		return reflections.getSubTypesOf(ConfigurationFactory.class).stream().collect(Collectors.toSet());
	}

	@SuppressWarnings("unchecked")
	public static <ConfigurationClass> Object getConfigurations(String path, ClassLoader loader,
			Class<?> configurationClass) {
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

	private static <ConfigurationClass> Object invokeCreateMethodAndGetConfigurations(
			ConfigurationClass configurationFactory) throws IllegalAccessException, InvocationTargetException {
		Object configurations = null;
		try {
			Method method = configurationFactory.getClass().getMethod(DataBridgeUtils.CREATE_METHOD_NAME);
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

	public static String findAvailableConfigurationFile(Class<?> clazz) {
		String fileNameDefinedInConfigFactory = null;
		try {
			fileNameDefinedInConfigFactory = (String) clazz.getField(FIELD_FILE_PATH).get(null);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			logger.info("In class {} the field {} doesn't found!", clazz.getName(), FIELD_FILE_PATH);
		}

		return fileNameDefinedInConfigFactory;
	}

	public static Set<String> getAllConfigFilesMatchingInputFileName(Set<String> configFiles,
			String fileNameDefinedInConfigFactory) {
		return configFiles.stream()
				.filter(userInputConfigFilename -> userInputConfigFilename.equals(fileNameDefinedInConfigFactory))
				.collect(Collectors.toSet());
	}
}
