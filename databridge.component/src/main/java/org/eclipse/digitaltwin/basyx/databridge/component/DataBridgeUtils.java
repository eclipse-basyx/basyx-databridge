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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.digitaltwin.basyx.databridge.core.configuration.factory.ConfigurationFactory;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility class for DataBridge
 * 
 * @author danish
 *
 */
public class DataBridgeUtils {
	private static Logger logger = LoggerFactory.getLogger(DataBridgeUtils.class);

	public static final String PACKAGE_PREFIX = "org.eclipse.digitaltwin.basyx.databridge";
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
		try (Stream<Path> stream = Files.list(Paths.get(directory))) {
			return stream.filter(file -> !Files.isDirectory(file)).map(Path::getFileName).map(Path::toString).collect(Collectors.toSet());
		} catch (IOException e) {
			return Collections.emptySet();
		}
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
			listConfig = getConfigurations(configFactory);

			logger.info("Instantiated {}", configFactory.getClass().toString());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException();
		}

		return listConfig;
	}

	private static <ConfigurationClass> Object getConfigurations(
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
		try {
			return (String) clazz.getField(FIELD_FILE_PATH).get(null);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			logger.info("In class {} the field could not be  {} found!", clazz.getName(), FIELD_FILE_PATH);
		}

		return null;
	}

	public static Set<String> getAllConfigFilesMatchingInputFileName(Set<String> configFiles,
			String fileNameDefinedInConfigFactory) {
		return configFiles.stream()
				.filter(userInputConfigFilename -> userInputConfigFilename.equals(fileNameDefinedInConfigFactory))
				.collect(Collectors.toSet());
	}
}
