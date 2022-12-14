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
package basyx.components.databridge.regression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import basyx.components.databridge.camelactivemq.configuration.ActiveMQConsumerConfiguration;
import basyx.components.databridge.core.configuration.route.core.RoutesConfiguration;
import basyx.components.databridge.executable.RoutesConfigurationLoader;

/**
 * Tests the DataBridge routes configuration scenario
 *
 * @author danish
 */
public class TestRoutesConfigurationLoader {
	private static final String PATH_PREFIX = "src/test/resources/activemq/databridge";

	@Test
	public void configFactoryisCorrect() {
		RoutesConfiguration configuration = new RoutesConfigurationLoader(PATH_PREFIX).create();

		assertRoutesConfigurationIsCorrect(configuration);
	}

	private void assertRoutesConfigurationIsCorrect(RoutesConfiguration configuration) {
		assertEquals(2, configuration.getDatasources().size());
		assertEquals(2, configuration.getTransformers().size());
		assertEquals(2, configuration.getDatasinks().size());

		assertTrue(configuration.getDatasources().get("property1") instanceof ActiveMQConsumerConfiguration);
	}
}
