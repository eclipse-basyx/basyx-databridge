/*******************************************************************************
 * Copyright (C) 2024 the Eclipse BaSyx Authors
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

package org.eclipse.digitaltwin.basyx.databridge.executable.regression;

import org.eclipse.basyx.aas.aggregator.api.IAASAggregator;
import org.eclipse.basyx.aas.aggregator.proxy.AASAggregatorProxy;
import org.eclipse.basyx.aas.registration.memory.InMemoryRegistry;
import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.component.DataBridgeExecutable;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Tests the DataBridge with HTTP Producer
 *
 * @author rana
 */
public class TestDataBridgeHttpProducer extends DataBridgeSuiteHttpProducer{
	
	private static final String AAS_AGGREGATOR_URL = "http://localhost:4001";
	private static final String END_POINT_URL = "http://localhost:8091";
	private static final String HOST = "localhost";
	private static AASServerComponent aasServer;
	private static BaSyxContextConfiguration aasContextConfig;
	private static InMemoryRegistry registry = new InMemoryRegistry();

	@BeforeClass
	public static void setUp() throws Exception {

		configureAndStartAasServer();
	
		startUpdaterComponent();
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		
		aasServer.stopComponent();
		
		stopDataBridgeComponent();
	}
	
	private static void configureAndStartAasServer() throws InterruptedException {
		
		aasContextConfig = new BaSyxContextConfiguration(4001, "");
		
		BaSyxAASServerConfiguration aasConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY, "aasx/telemeteryTest.aasx");
		
		aasServer = new AASServerComponent(aasContextConfig, aasConfig);
		aasServer.setRegistry(registry);
		aasServer.startComponent();
	}
	
	private static void startUpdaterComponent() {
		DataBridgeExecutable.main(new String[] {"src/test/resources/httpproducer/databridge"});
	}
	
	private static void stopDataBridgeComponent() {
		if(DataBridgeExecutable.getDataBridgeComponent() != null) {
			DataBridgeExecutable.getDataBridgeComponent().stopComponent();
		}
	}

	@Override
	protected IAASAggregator getAASAggregatorProxy() {
		return new AASAggregatorProxy(AAS_AGGREGATOR_URL);
	}

	@Override
	protected String getEndpoint() {
		return END_POINT_URL;
	}

	@Override
	protected String getHost() {
		return HOST;
	}
}
