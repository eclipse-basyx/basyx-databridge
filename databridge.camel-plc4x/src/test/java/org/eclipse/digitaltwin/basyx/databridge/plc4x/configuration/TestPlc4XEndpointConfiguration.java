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
package org.eclipse.digitaltwin.basyx.databridge.plc4x.configuration;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.camel.component.plc4x.Plc4XEndpoint;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataSourceConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.plc4x.configuration.factory.Plc4XDefaultConfigurationFactory;
import org.junit.Test;

/**
 * Tests the PLC4X endpoint configuration
 * 
 * @author danish
 *
 */
public class TestPlc4XEndpointConfiguration {
	
	private static final String EXPECTED_CONNECTION_URI = "plc4x:modbus-tcp://localhost:502?period=100";
	private static final Map<String, Object> EXPECTED_TAGS = Collections.singletonMap("value-1", "holding-register:1");
	
	@Test
	public void configureEndpoint() {
		Plc4XDefaultConfigurationFactory plc4xDefaultConfigurationFactory = new Plc4XDefaultConfigurationFactory(TestPlc4XEndpointConfiguration.class.getClassLoader());
		
		List<DataSourceConfiguration> dataSourceConfigurations = plc4xDefaultConfigurationFactory.create();
		
		Plc4XEndpoint endpoint = (Plc4XEndpoint) dataSourceConfigurations.get(0).getConnectionURI();
		
		assertEquals(EXPECTED_CONNECTION_URI, endpoint.getEndpointUri());
		assertEqualTag(EXPECTED_TAGS, endpoint.getTags());
	}

	private void assertEqualTag(Map<String, Object> expectedTags, Map<String, Object> actualTags) {
		assertTrue(actualTags.containsKey("value-1"));
		assertEquals(expectedTags.get("value-1"), actualTags.get("value-1"));
	}

}
