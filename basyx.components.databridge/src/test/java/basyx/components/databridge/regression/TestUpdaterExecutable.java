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
package basyx.components.databridge.regression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.junit.Test;
import basyx.components.databridge.camelactivemq.configuration.ActiveMQConsumerConfiguration;
import basyx.components.databridge.core.configuration.route.core.RoutesConfiguration;
import basyx.components.databridge.executable.UpdaterExecutable;

/**
 * Tests the updater executable scenarios 
 *
 * @author danish
 */
public class TestUpdaterExecutable {
	private static final String PATH_PREFIX = "src/test/resources";
	
	private static final String CONTENT = "[\r\n"
			+ "	{\r\n"
			+ "		\"uniqueId\": \"property1\",\r\n"
			+ "		\"serverUrl\": \"127.0.0.1\",\r\n"
			+ "		\"serverPort\": 61616,\r\n"
			+ "		\"queue\": \"first-topic\"\r\n"
			+ "	}\r\n"
			+ "]";
	
	@Test
	public void loadedFileisCorrect() throws IOException {
		Set<String> configFiles = UpdaterExecutable.listFiles(PATH_PREFIX);
		
		assertEquals(1, configFiles.size());
		
		assertEquals(CONTENT, Files.readString(Path.of(PATH_PREFIX + "/" + configFiles.stream().findAny().get())));
	}
	
	@Test
	public void configFactoryisCorrect() {
		Set<String> configFiles = UpdaterExecutable.listFiles(PATH_PREFIX);
		
		RoutesConfiguration configuration = new RoutesConfiguration();
		
		applyConfiguration(configFiles, configuration);
		
		assertTrue(configuration.getDatasources().get("property1") instanceof ActiveMQConsumerConfiguration);
		
		assertTrue(configuration.getDatasinks().size() == 0 && configuration.getRoutes().size() == 0 && configuration.getTransformers().size() == 0);
	}

	private void applyConfiguration(Set<String> configFiles, RoutesConfiguration configuration) {
		Set<Class<?>> classes = UpdaterExecutable.findAllConfigurationFactoryClasses(UpdaterExecutable.PACKAGE_PREFIX);
		classes.stream().forEach(clazz -> UpdaterExecutable.findAvailableConfigurationFilesAndAddConfiguration(clazz, configuration, configFiles, PATH_PREFIX));
	}
}
