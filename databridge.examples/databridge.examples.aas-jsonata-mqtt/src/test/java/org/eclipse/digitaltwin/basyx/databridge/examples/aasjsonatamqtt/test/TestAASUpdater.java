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

package org.eclipse.digitaltwin.basyx.databridge.examples.aasjsonatamqtt.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import static org.junit.Assert.assertEquals;

import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.aas.registration.memory.InMemoryRegistry;
import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.digitaltwin.basyx.databridge.aas.configuration.factory.AASPollingConsumerDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.component.DataBridgeComponent;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.factory.RoutesConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RoutesConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.jsonata.configuration.factory.JsonataDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.paho.configuration.factory.MqttDataSinkDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.timer.configuration.factory.TimerDefaultConfigurationFactory;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.moquette.broker.Server;
import io.moquette.broker.config.ClasspathResourceLoader;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.IResourceLoader;
import io.moquette.broker.config.ResourceLoaderConfig;

/**
 * A test of aas-jsonata-mqtt 
 * @author rana
 *
 */
public class TestAASUpdater {

	private static Logger logger = LoggerFactory.getLogger(TestAASUpdater.class);
	private static AASServerComponent aasServer;
	private static BaSyxContextConfiguration aasContextConfig;
	private static InMemoryRegistry registry = new InMemoryRegistry();
	private static DataBridgeComponent updater;
	private static String mqtt_broker_url = "tcp://broker.mqttdashboard.com:1883";
	private static String user_name = "test1";
	private static String password = "1234567";
	private static String client_id = UUID.randomUUID().toString();
	protected static String receivedMessage;
	protected static IIdentifier deviceAASPlainId = new CustomId("TestUpdatedDeviceAAS");
	protected static Server mqttBroker;
	
	@BeforeClass
	public static void setUp() throws Exception {

		configureAndStartMqttBroker();
		
		configureAasServer();
		
		startAasServer();

		configureAndStartUpdaterComponent();
		
	}
	
	@AfterClass
	public static void tearDown() {
		updater.stopComponent();
		
		aasServer.stopComponent();
	}
	
	@Test
	public void getUpdatedPropertyValueA() throws MqttException, MqttSecurityException, MqttPersistenceException, InterruptedException, JsonProcessingException {
		
		String topic = "aas/pressure";
		String expectedValue = "103.5585973";
		
		assertPropertyValue(expectedValue, topic);
	}
	
	@Test
	public void getUpdatedPropertyValueB() throws MqttException, MqttSecurityException, MqttPersistenceException, InterruptedException, JsonProcessingException {

		String topic = "aas/rotation";
		String expectedValue = "379.5784558";
		
		assertPropertyValue(expectedValue, topic);
	}
	
	@Test
	public void getUpdatedPropertyValueC() throws MqttException, MqttSecurityException, MqttPersistenceException, InterruptedException, IOException, URISyntaxException {

		String topic = "aas/pressure_rotation";
		String expectedValue = getExpectedValueFromFile();
		
		assertPropertyValueC(expectedValue, topic);
	}
	
	private void assertPropertyValue(String expectedValue, String topic) throws MqttSecurityException, MqttPersistenceException, MqttException, InterruptedException {
		
		fetchExpectedValue(topic);
		
		receivedMessage = receivedMessage.substring(1, receivedMessage.length() - 1);
		
		assertEquals(receivedMessage, expectedValue);
	}
	
	private void assertPropertyValueC(String expectedValue, String topic) throws MqttSecurityException, MqttPersistenceException, MqttException, InterruptedException, JsonMappingException, JsonProcessingException {
		
		fetchExpectedValue(topic);
		
		ObjectMapper mapper = new ObjectMapper();
		
		assertEquals(mapper.readTree(receivedMessage), mapper.readTree(expectedValue));
	}
	
	private static void fetchExpectedValue(String currentTopic) throws MqttException, MqttSecurityException, MqttPersistenceException, InterruptedException {
		
		try {
			
			MqttClient mqttClient = mqttConnectionInitiate();
			
			mqttClient.setCallback(new MqttCallback() {

				@Override
				public void connectionLost(Throwable cause) {
					logger.info("Connection Lost : "+cause.getMessage());
				}

				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					
					receivedMessage = new String(message.getPayload(), StandardCharsets.UTF_8);				
				}

				@Override
				public void deliveryComplete(IMqttDeliveryToken token) {
					
				}
			});

			mqttClient.subscribe(currentTopic);
			waitForPropagation();
			mqttClient.disconnect();
			mqttClient.close();

		} catch (MqttException me) {
            me.printStackTrace();
		}
	}
	
	private static void configureAasServer() throws InterruptedException {
		
		aasContextConfig = new BaSyxContextConfiguration(4001, "");
		
		BaSyxAASServerConfiguration aasConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY, "aasx/telemeteryTest.aasx");
		
		aasServer = new AASServerComponent(aasContextConfig, aasConfig);
		aasServer.setRegistry(registry);
	}
	
	private static void startAasServer() {
		aasServer.startComponent();
	}

	private static void configureAndStartUpdaterComponent() throws Exception {

		ClassLoader loader = TestAASUpdater.class.getClassLoader();
		RoutesConfiguration configuration = new RoutesConfiguration();

		RoutesConfigurationFactory routesFactory = new RoutesConfigurationFactory(loader);
		configuration.addRoutes(routesFactory.create());

		TimerDefaultConfigurationFactory timerConfigFactory = new TimerDefaultConfigurationFactory(loader);
		configuration.addDatasources(timerConfigFactory.create());

		AASPollingConsumerDefaultConfigurationFactory aasSourceConfigFactory = new AASPollingConsumerDefaultConfigurationFactory(
				loader);
		configuration.addDatasources(aasSourceConfigFactory.create());

		MqttDataSinkDefaultConfigurationFactory mqttConfigFactory = new MqttDataSinkDefaultConfigurationFactory(loader);
		configuration.addDatasinks(mqttConfigFactory.create());

		JsonataDefaultConfigurationFactory jsonataConfigFactory = new JsonataDefaultConfigurationFactory(loader);
		configuration.addTransformers(jsonataConfigFactory.create());

		updater = new DataBridgeComponent(configuration);
		updater.startComponent();
	}

	private static void waitForPropagation() throws InterruptedException {
		Thread.sleep(5000);
	}
	
	private static MqttClient mqttConnectionInitiate() throws MqttException {
		
		MqttClient mqttClient = new MqttClient(mqtt_broker_url, client_id, new MemoryPersistence());
		
		MqttConnectOptions connOpts = setUpMqttConnection(user_name, password);
		mqttClient.connect(connOpts);
		connOpts.setCleanSession(true);
		return mqttClient;
	}
	
	private static void configureAndStartMqttBroker() throws IOException {
		mqttBroker = new Server();
		IResourceLoader classpathLoader = new ClasspathResourceLoader();
		final IConfig classPathConfig = new ResourceLoaderConfig(classpathLoader);
		mqttBroker.startServer(classPathConfig);
	}
	
	private static String getExpectedValueFromFile() throws IOException, URISyntaxException {
		
		String filename = "submodelproperties.json";
		URL resource = TestAASUpdater.class.getClassLoader().getResource(filename);
	    byte[] content = Files.readAllBytes(Paths.get(resource.toURI()));
		return new String(content);
	}

	public static MqttConnectOptions setUpMqttConnection(String username, String password) {
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setUserName(username);
		connOpts.setPassword(password.toCharArray());
		return connOpts;
	}
}
