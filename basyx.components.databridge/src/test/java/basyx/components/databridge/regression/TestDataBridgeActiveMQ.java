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

import java.io.IOException;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.usage.SystemUsage;
import org.eclipse.basyx.aas.aggregator.api.IAASAggregator;
import org.eclipse.basyx.aas.aggregator.proxy.AASAggregatorProxy;
import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import basyx.components.databridge.executable.DataBridgeComponent;

/**
 * Tests the DataBridge with ActiveMQ
 *
 * @author danish
 */
public class TestDataBridgeActiveMQ extends DataBridgeSuiteActiveMQ {
	private static final String AAS_AGGREGATOR_URL = "http://localhost:4001";

	private static final String BROKER_URL = "tcp://localhost:61616";

	private static AASServerComponent aasServer;
	
	protected static BrokerService activeMQBroker;
	
	@BeforeClass
	public static void setUp() throws IOException {
		startActiveMQBroker();

		configureAndStartAASServer();
		
		startUpdaterComponent();
	}
	
	private static void configureAndStartAASServer() {
		BaSyxContextConfiguration aasContextConfig = new BaSyxContextConfiguration(4001, "");
		
		BaSyxAASServerConfiguration aasConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY, "aasx/updatertest.aasx");
		
		aasServer = new AASServerComponent(aasContextConfig, aasConfig);
		aasServer.startComponent();
	}
	
	private static void startUpdaterComponent() {
		DataBridgeComponent dataBridgeComponent = new DataBridgeComponent("src/test/resources/activemq/databridge");
		dataBridgeComponent.start();
	}
	
	@Override
	protected Connection getActiveMQConnection() throws JMSException {
		return new ActiveMQConnectionFactory(BROKER_URL).createConnection();
	}

	@Override
	protected IAASAggregator getAASAggregatorProxy() {
		return new AASAggregatorProxy(AAS_AGGREGATOR_URL);
	}
	
	private static void startActiveMQBroker() {
		try {
			activeMQBroker = new BrokerService();
			activeMQBroker.addConnector(BROKER_URL);
			activeMQBroker.setPersistent(false);
			
			setSystemUsageLimit();
			
			activeMQBroker.start();
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

	private static void setSystemUsageLimit() {
		SystemUsage systemUsage = activeMQBroker.getSystemUsage();
		systemUsage.getStoreUsage().setLimit(1024 * 1024 * 8);
		systemUsage.getTempUsage().setLimit(1024 * 1024 * 8);
	}
	
	@AfterClass
	public static void tearDown() {
		aasServer.stopComponent();
		
		try {
			activeMQBroker.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
