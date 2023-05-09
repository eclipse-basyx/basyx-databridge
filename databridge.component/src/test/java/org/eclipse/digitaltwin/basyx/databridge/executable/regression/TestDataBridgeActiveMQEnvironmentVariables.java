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
package org.eclipse.digitaltwin.basyx.databridge.executable.regression;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.eclipse.basyx.aas.aggregator.api.IAASAggregator;
import org.eclipse.basyx.aas.aggregator.proxy.AASAggregatorProxy;
import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.digitaltwin.basyx.databridge.component.DataBridgeExecutable;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Tests the DataBridge with ActiveMQ configured via environment variables
 *
 * @author danish, schnicke
 */
public class TestDataBridgeActiveMQEnvironmentVariables extends DataBridgeSuiteActiveMQ {
	private static final String AAS_AGGREGATOR_URL = "http://localhost:4001";

	private static final String BROKER_URL = "tcp://localhost:61616";

	private static AASServerComponent aasServer;
	
	protected static BrokerService activeMQBroker;
	
	@BeforeClass
	public static void setUp() throws IOException {
		setUpEnvironmentVariables();

		activeMQBroker = DataBridgeActiveMQTestHelper.startActiveMQBroker(BROKER_URL);

		aasServer = DataBridgeActiveMQTestHelper.configureAndStartAASServer();
		
		startUpdaterComponent();
	}
	
	@AfterClass
	public static void tearDown() {
		aasServer.stopComponent();
		
		try {
			activeMQBroker.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		stopDataBridgeComponent();
		EnvironmentVariableHelper.setEnvironmentVariablesForTesting(Collections.emptyMap());
	}
	
	@Override
	protected Connection getActiveMQConnection() throws JMSException {
		return new ActiveMQConnectionFactory(BROKER_URL).createConnection();
	}

	@Override
	protected IAASAggregator getAASAggregatorProxy() {
		return new AASAggregatorProxy(AAS_AGGREGATOR_URL);
	}
	
	private static void setUpEnvironmentVariables() {
		Map<String, String> environmentVariables = RoutesConfigurationTestEnvironmentVariables.get();
		EnvironmentVariableHelper.setEnvironmentVariablesForTesting(environmentVariables);
	}

	private static void startUpdaterComponent() {
		DataBridgeExecutable.main(new String[] {});
	}
	
	private static void stopDataBridgeComponent() {
		if(DataBridgeExecutable.getDataBridgeComponent() != null) {
			DataBridgeExecutable.getDataBridgeComponent().stopComponent();
		}
	}
}
