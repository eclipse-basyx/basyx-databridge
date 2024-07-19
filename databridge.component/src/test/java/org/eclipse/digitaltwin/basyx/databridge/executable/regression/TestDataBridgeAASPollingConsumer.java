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

import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.component.DataBridgeExecutable;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import io.moquette.broker.Server;
import io.moquette.broker.config.ClasspathResourceLoader;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.IResourceLoader;
import io.moquette.broker.config.ResourceLoaderConfig;

/**
 * Tests the DataBridge with AAS Polling Consumer
 *
 * @author rana
 */
public class TestDataBridgeAASPollingConsumer extends DataBridgeSuiteAASPollingConsumer {

	private static AASServerComponent aasServer;
	private static String BROKER_URL = "tcp://broker.mqttdashboard.com:1883";
	private static Server mqttBroker;
	
	@BeforeClass
	public static void setUp() throws IOException {
		
		startMqttBroker();

		configureAndStartAASServer();
		
		startUpdaterComponent();
		
	}

	@Override
	protected MqttClient getMqttClient() throws MqttException {
		String publisherId = UUID.randomUUID().toString();
		
		return new MqttClient(BROKER_URL, publisherId, new MemoryPersistence());
	}
	
	private static void startMqttBroker() throws IOException {
		mqttBroker = new Server();
		
		IResourceLoader classpathLoader = new ClasspathResourceLoader();
		final IConfig classPathConfig = new ResourceLoaderConfig(classpathLoader);
		
		mqttBroker.startServer(classPathConfig);
	}
	
	private static void configureAndStartAASServer() {
		BaSyxContextConfiguration aasContextConfig = new BaSyxContextConfiguration(4001, "");
		
		BaSyxAASServerConfiguration aasConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY, "aasx/telemeteryTest.aasx");
		
		aasServer = new AASServerComponent(aasContextConfig, aasConfig);
		aasServer.startComponent();
		
	}
	
	private static void startUpdaterComponent() {
		DataBridgeExecutable.main(new String[] {"src/test/resources/aaspollingconsumer/databridge"});
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
