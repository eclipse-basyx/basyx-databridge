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
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

import org.awaitility.Awaitility;
import org.eclipse.basyx.aas.registration.memory.InMemoryRegistry;
import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
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
	private final static Logger logger = LoggerFactory.getLogger(TestAASUpdater.class);

	private final static InMemoryRegistry REGISTRY = new InMemoryRegistry();
	private final static String MQTT_BROKER_URL = "tcp://broker.mqttdashboard.com:1883";
	private final static String USER_NAME = "test1";
	private final static String PASSWORD = "1234567";
	private final static String CLIENT_ID = UUID.randomUUID().toString();

	private final static String TOPIC_PRESSURE  = "aas/pressure";
	private final static String TOPIC_ROTATION = "aas/rotation";
	private final static String TOPIC_PRESSURE_ROTATION = "aas/pressure_rotation";
	
	
	private static AASServerComponent aasServer;
	private static BaSyxContextConfiguration aasContextConfig;
	private static DataBridgeComponent updater;
	private static String receivedMessage;
	private static Server mqttBroker;
	
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
		mqttBroker.stopServer();
		aasServer.stopComponent();
	}
	
	@Test
	public void getUpdatedPropertyValueA() throws MqttException, MqttSecurityException, MqttPersistenceException, InterruptedException, JsonProcessingException {
		
		String topic = TOPIC_PRESSURE;
		String expectedValue = wrapStringValue("103.5585973");
		awaitAndAssertMqttPropagation(expectedValue, topic);
	}
	
	@Test
	public void getUpdatedPropertyValueB() throws MqttException, MqttSecurityException, MqttPersistenceException, InterruptedException, JsonProcessingException {

		String topic = TOPIC_ROTATION;
		String expectedValue = wrapStringValue("379.5784558");
		awaitAndAssertMqttPropagation(expectedValue, topic);
	}
	
	@Test
	public void getAllProperties() throws MqttException, MqttSecurityException, MqttPersistenceException, InterruptedException, IOException, URISyntaxException {

		String topic = TOPIC_PRESSURE_ROTATION;
		String expectedValue = getExpectedValueFromFile();
		awaitAndAssertMqttPropagation(expectedValue, topic);
	}
	
	private void awaitAndAssertMqttPropagation(String expectedValue, String currentTopic) throws MqttException, MqttSecurityException, MqttPersistenceException, InterruptedException {
		
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
			Awaitility.await().with().pollInterval(1, TimeUnit.SECONDS).atMost(10, TimeUnit.SECONDS).untilAsserted(() -> assertMessage(expectedValue, receivedMessage));
			mqttClient.disconnect();
			mqttClient.close();

		} catch (MqttException me) {
            me.printStackTrace();
		}
	}
	
	private void assertMessage(String expectedValue, String receivedMessage) {
		if (!isAllProperties(expectedValue)) {
			assertEquals(expectedValue, receivedMessage);
		}
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			assertEquals(mapper.readTree(expectedValue), mapper.readTree(receivedMessage));
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	private boolean isAllProperties(String expectedValue) {
		return expectedValue.startsWith("[");
	}

	private static void configureAasServer() throws InterruptedException {
		
		aasContextConfig = new BaSyxContextConfiguration(4001, "");
		
		BaSyxAASServerConfiguration aasConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY, "aasx/telemeteryTest.aasx");
		
		aasServer = new AASServerComponent(aasContextConfig, aasConfig);
		aasServer.setRegistry(REGISTRY);
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
		TimeUnit.SECONDS.sleep(5); // FIXME: Failed to start route routeN because of null
		updater.startComponent();
	}
	
	private static MqttClient mqttConnectionInitiate() throws MqttException {
		
		MqttClient mqttClient = new MqttClient(MQTT_BROKER_URL, CLIENT_ID, new MemoryPersistence());
		
		MqttConnectOptions connOpts = setUpMqttConnection(USER_NAME, PASSWORD);
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

	private static MqttConnectOptions setUpMqttConnection(String username, String password) {
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setUserName(username);
		connOpts.setPassword(password.toCharArray());
		return connOpts;
	}
	
	private String wrapStringValue(String value) {
		return "\"" + value + "\"";
	}
}
