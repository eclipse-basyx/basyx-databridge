/*******************************************************************************
 * Copyright (C) 2021 the Eclipse BaSyx Authors
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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.connected.ConnectedAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.aas.registration.memory.InMemoryRegistry;
import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IdentifierType;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.connected.ConnectedSubmodel;
import org.eclipse.basyx.submodel.metamodel.map.identifier.Identifier;
import org.eclipse.digitaltwin.basyx.databridge.aas.configuration.factory.AASConsumerDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.component.DataBridgeComponent;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.factory.RoutesConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RoutesConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.jsonata.configuration.factory.JsonataDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.paho.configuration.factory.MqttDatasinkDefaultConfigurationFactory;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
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
	protected static IIdentifier deviceAASPlainId = new CustomId("TestUpdatedDeviceAAS");

	private static BaSyxContextConfiguration aasContextConfig;
	private static InMemoryRegistry registry = new InMemoryRegistry();
	
	protected static Server mqttBroker;
	private static DataBridgeComponent updater;
	private static String mqtt_broker_url = "tcp://broker.mqttdashboard.com:1883";
	private static String user_name = "test1";
	private static String password = "1234567";
	private static String client_id = UUID.randomUUID().toString();;
	
	@BeforeClass
	public static void setUp() throws Exception {

		configureAndStartMqttBroker();
		
		configureAndStartAasServer();

		configureAndStartUpdaterComponent();
		
	}
	
	@AfterClass
	public static void tearDown() {
		updater.stopComponent();
		aasServer.stopComponent();
	}
	
	private static void configureAndStartAasServer() throws InterruptedException {
		aasContextConfig = new BaSyxContextConfiguration(4001, "");
		BaSyxAASServerConfiguration aasConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY, "aasx/telemeteryTest.aasx");
		aasServer = new AASServerComponent(aasContextConfig, aasConfig);
		aasServer.setRegistry(registry);
		
		aasServer.startComponent();
	}

	public static void configureAndStartUpdaterComponent() throws Exception {

		ClassLoader loader = TestAASUpdater.class.getClassLoader();
		RoutesConfiguration configuration = new RoutesConfiguration();

		RoutesConfigurationFactory routesFactory = new RoutesConfigurationFactory(loader);
		configuration.addRoutes(routesFactory.create());

		TimerDefaultConfigurationFactory timerConfigFactory = new TimerDefaultConfigurationFactory(loader);
		configuration.addDatasources(timerConfigFactory.create());

		AASConsumerDefaultConfigurationFactory aasSourceConfigFactory = new AASConsumerDefaultConfigurationFactory(
				loader);
		configuration.addDatasources(aasSourceConfigFactory.create());

		MqttDatasinkDefaultConfigurationFactory mqttConfigFactory = new MqttDatasinkDefaultConfigurationFactory(loader);
		configuration.addDatasinks(mqttConfigFactory.create());

		JsonataDefaultConfigurationFactory jsonataConfigFactory = new JsonataDefaultConfigurationFactory(loader);
		configuration.addTransformers(jsonataConfigFactory.create());

		updater = new DataBridgeComponent(configuration);
		updater.startComponent();
	}

	private static void waitForPropagation() throws InterruptedException {
		Thread.sleep(3000);
	}
	
	@Test
	public void checkIfPropertyIsSent() throws MqttException, MqttSecurityException, MqttPersistenceException, InterruptedException, JsonProcessingException {

		String[] idShortArray = new String[]{"pressure","rotation"};
		String[] topicArray = new String[]{"aas/pressure","aas/rotation"};
		
		// Checking test case - 1
		checkUseCaseA(topicArray, idShortArray);
	
		// Checking test case for use case - 1
		//checkUseCaseB(topicArray, idShortArray);
		
	}
	
	private void checkUseCaseA(String[] topicArray, String[] idShortArray) throws MqttSecurityException, MqttPersistenceException, MqttException, InterruptedException, JsonProcessingException {
		
		// Checking idShortA
		String propValueA = (String) checkAASsubmodelElementsProperty(idShortArray[0]);
		receiveUseCaseMessageA(topicArray[0], propValueA);
			
		// Checking idShortB 
		String propValueB = (String) checkAASsubmodelElementsProperty(idShortArray[1]);
		receiveUseCaseMessageA(topicArray[1], propValueB);
	}

	
	private void checkUseCaseB(String[] topicArray, String[] idShortArray) throws MqttSecurityException, MqttPersistenceException, MqttException, InterruptedException, JsonProcessingException {
		
		// Checking idShort0
		String subModelA = checkAASsubmodelElements(idShortArray);
		receiveUseCaseMessageB(topicArray[0], subModelA);
		
		// Checking idShort1
		String subModelB = checkAASsubmodelElements(idShortArray);
		receiveUseCaseMessageB(topicArray[1], subModelB);
	}
	

	
	private String checkAASsubmodelElements(String[] idShort) throws JsonProcessingException, InterruptedException {
		
		Map<String,Object> map = new HashMap<>();
		
		ConnectedAssetAdministrationShell aas = getAAS(deviceAASPlainId);
		ConnectedSubmodel sm = (ConnectedSubmodel) aas.getSubmodel((IIdentifier) new Identifier(IdentifierType.IRI,"https://example.com/ids/sm/8583_3140_7032_9766"));
		
		Map<String, ISubmodelElement> subModelCollection = sm.getSubmodelElements();
		
		if(subModelCollection.size()>0) {
			
			for (Map.Entry<String, ISubmodelElement> entry : subModelCollection.entrySet()) {
				map.put(entry.getKey(), entry.getValue());
		    }
		}
		
		if(map.size() > 0) {
			
			JSONObject json = new JSONObject(map);
			JSONArray attributeValue = new JSONArray();
			
			for (String string : idShort) {
				JSONObject innerObject = json.getJSONObject(string);
				JSONObject element = innerObject.getJSONObject("elem");
				attributeValue.put(element);
			}
			
			return attributeValue.toString();
		}else {
			return null;
		}
	}
	
	private Object checkAASsubmodelElementsProperty(String idShort) throws InterruptedException {
		
		ConnectedAssetAdministrationShell aas = getAAS(deviceAASPlainId);
		ISubmodelElement updatedProp = getSubmodelElement(aas, "telemetryDataStructureTest", idShort);
		
		Object propValue = updatedProp.getValue();
		return propValue;
	}
	
	private static void receiveUseCaseMessageA(String currentTopic, String propValue) throws MqttException, MqttSecurityException, MqttPersistenceException, InterruptedException {

		try {
			
			// Initiate Conn
			MqttClient mqttClient = mqttConnectionInitiate();

			mqttClient.setCallback(new MqttCallback() {

				@Override
				public void connectionLost(Throwable cause) {
					logger.info("Connection Lost : "+cause.getMessage());
				}

				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					
					String receivedMessage = new String(message.getPayload(), StandardCharsets.UTF_8);
					receivedMessage = receivedMessage.substring(1, receivedMessage.length() - 1);
					assertEquals(propValue, receivedMessage);
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
	
	private static void receiveUseCaseMessageB(String currentTopic, String propValue) throws MqttException, MqttSecurityException, MqttPersistenceException, InterruptedException {

		try {
			
			// Initiate Conn
			MqttClient mqttClient = mqttConnectionInitiate();

			mqttClient.setCallback(new MqttCallback() {

				@Override
				public void connectionLost(Throwable cause) {
					logger.info("Connection Lost : "+cause.getMessage());
				}

				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {					
						
					String receivedMessage = new String(message.getPayload(), StandardCharsets.UTF_8);
					
					ObjectMapper mapper = new ObjectMapper();
					assertEquals(mapper.readTree(receivedMessage), mapper.readTree(propValue));
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
	
	private static MqttClient mqttConnectionInitiate() throws MqttException {
		
		MqttClient mqttClient = new MqttClient(mqtt_broker_url, client_id, new MemoryPersistence());
		MqttConnectOptions connOpts = setUpMqttConnection(user_name, password);
		mqttClient.connect(connOpts);
		
		return mqttClient;
	}
	
	private static void configureAndStartMqttBroker() throws IOException {
		mqttBroker = new Server();
		IResourceLoader classpathLoader = new ClasspathResourceLoader();
		final IConfig classPathConfig = new ResourceLoaderConfig(classpathLoader);
		mqttBroker.startServer(classPathConfig);
	}

	public static MqttConnectOptions setUpMqttConnection(String username, String password) {
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(true);
		connOpts.setUserName(username);
		connOpts.setPassword(password.toCharArray());
		return connOpts;
	}

	private ISubmodelElement getSubmodelElement(ConnectedAssetAdministrationShell aas, String submodelId, String submodelElementId) {
		ISubmodel sm = aas.getSubmodels().get(submodelId);
		ISubmodelElement updatedProp = sm.getSubmodelElement(submodelElementId);
		
		return updatedProp;
	}

	private ConnectedAssetAdministrationShell getAAS(IIdentifier identifier) {
		ConnectedAssetAdministrationShellManager manager = new ConnectedAssetAdministrationShellManager(registry);
		ConnectedAssetAdministrationShell aas = manager.retrieveAAS(identifier);
		return aas;
	}
}
