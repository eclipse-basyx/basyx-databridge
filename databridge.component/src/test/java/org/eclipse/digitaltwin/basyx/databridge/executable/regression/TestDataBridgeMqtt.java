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
import java.util.UUID;

import org.eclipse.basyx.aas.aggregator.api.IAASAggregator;
import org.eclipse.basyx.aas.aggregator.proxy.AASAggregatorProxy;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.digitaltwin.basyx.databridge.component.DataBridgeExecutable;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import io.moquette.broker.Server;
import io.moquette.broker.config.ClasspathResourceLoader;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.IResourceLoader;
import io.moquette.broker.config.ResourceLoaderConfig;

/**
 * Tests the DataBridge with Mqtt
 *
 * @author danish
 */
public class TestDataBridgeMqtt extends DataBridgeSuiteMqtt {
	private static AASServerComponent aasServer;
	protected static Server mqttBroker;

	protected static IIdentifier deviceAAS = new CustomId("TestUpdatedDeviceAAS");
	
	private static final String AAS_AGGREGATOR_URL = "http://localhost:4001";
	private static final String BROKER_URL = "tcp://localhost:1884";
	
	@BeforeClass
	public static void setUp() throws IOException {
		startMqttBroker();

		configureAndStartAASServer();
		
		startUpdaterComponent();
	}

	private static void startMqttBroker() throws IOException {
		mqttBroker = new Server();
		
		IResourceLoader classpathLoader = new ClasspathResourceLoader();
		final IConfig classPathConfig = new ResourceLoaderConfig(classpathLoader);
		
		mqttBroker.startServer(classPathConfig);
	}
	
	private static void configureAndStartAASServer() {
		BaSyxContextConfiguration aasContextConfig = new BaSyxContextConfiguration(4001, "");
		
		BaSyxAASServerConfiguration aasConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY, "aasx/updatertest.aasx");
		
		aasServer = new AASServerComponent(aasContextConfig, aasConfig);
		aasServer.startComponent();
	}
	
	private static void startUpdaterComponent() {
		DataBridgeExecutable.main(new String[] {"src/test/resources/mqtt/databridge"});
	}
	
	@Override
	protected IMqttClient getMqttClient() throws MqttException {
		String publisherId = UUID.randomUUID().toString();
		
		return new MqttClient(BROKER_URL, publisherId);
	}

	@Override
	protected IAASAggregator getAASAggregatorProxy() {
		return new AASAggregatorProxy(AAS_AGGREGATOR_URL);
	}
	
	private static void stopDataBridgeComponent() {
		if(DataBridgeExecutable.getDataBridgeComponent() != null) {
			DataBridgeExecutable.getDataBridgeComponent().stopComponent();
		}
	}
	
	@AfterClass
	public static void tearDown() {
		aasServer.stopComponent();
		
		mqttBroker.stopServer();
		
		stopDataBridgeComponent();
	}
}
