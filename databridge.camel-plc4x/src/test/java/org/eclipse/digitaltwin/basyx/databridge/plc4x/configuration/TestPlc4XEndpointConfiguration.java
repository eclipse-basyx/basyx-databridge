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
import java.io.IOException;
import java.util.List;
import org.apache.camel.component.plc4x.Plc4XComponent;
import org.apache.camel.component.plc4x.Plc4XEndpoint;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataSourceConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.plc4x.configuration.factory.Plc4XDefaultConfigurationFactory;
import org.junit.After;
import org.junit.Test;

/**
 * Tests the PLC4X endpoint configuration
 * 
 * @author danish
 *
 */
public class TestPlc4XEndpointConfiguration {
	
	private static final String EXPECTED_ENDPOINT_URI = "plc4x:modbus-tcp://localhost:502?period=100&tag.value-1=holding-register%3A1";
	
	private Plc4XEndpoint endpoint;
	
	@After
	public void tearDown() throws IOException {
		if (endpoint != null)
			endpoint.close();
	}
	
	@Test
	public void configureEndpointWithConcreteOptionType() {
		setup("plc4xconsumerA.json");
		
		assertEquals(EXPECTED_ENDPOINT_URI, endpoint.getEndpointUri());
	}
	
	@Test
	public void configureEndpointWithOptionsAsString() {
		setup("plc4xconsumerB.json");
		
		assertEquals(EXPECTED_ENDPOINT_URI, endpoint.getEndpointUri());
	}
	
	private void setup(String filePath) {
		Plc4XDefaultConfigurationFactory plc4xDefaultConfigurationFactory = new Plc4XDefaultConfigurationFactory(filePath, TestPlc4XEndpointConfiguration.class.getClassLoader());
		
		List<DataSourceConfiguration> dataSourceConfigurations = plc4xDefaultConfigurationFactory.create();
		
		endpoint = new Plc4XEndpoint((String) dataSourceConfigurations.get(0).getConnectionURI(), new Plc4XComponent());
	}

}
