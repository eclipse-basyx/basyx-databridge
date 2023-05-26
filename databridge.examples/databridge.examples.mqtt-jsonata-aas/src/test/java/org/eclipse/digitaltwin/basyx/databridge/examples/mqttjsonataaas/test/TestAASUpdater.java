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
package org.eclipse.digitaltwin.basyx.databridge.examples.mqttjsonataaas.test;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;

import org.apache.http.HttpStatus;
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
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.digitaltwin.basyx.databridge.aas.configuration.factory.AASProducerDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.component.DataBridgeComponent;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.factory.RoutesConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RoutesConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.jsonata.configuration.factory.JsonataDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.paho.configuration.factory.MqttDefaultConfigurationFactory;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.verify.VerificationTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.moquette.broker.Server;
import io.moquette.broker.config.ClasspathResourceLoader;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.IResourceLoader;
import io.moquette.broker.config.ResourceLoaderConfig;

public class TestAASUpdater {
	
	private static final String PROPERTY_VALUE = "\"0.75\"";
	private static final String PROPERTY_VALUE_PATH = "/submodels/submodelId/submodel/submodel-elements/ConnectedPropertyD/$value";

	private static Logger logger = LoggerFactory.getLogger(TestAASUpdater.class);
	
	private static AASServerComponent aasServer;
	private static DataBridgeComponent updater;
	private static InMemoryRegistry registry = new InMemoryRegistry();
	protected static Server mqttBroker;

	protected static IIdentifier deviceAASPlainId = new CustomId("TestUpdatedDeviceAAS");
	protected static IIdentifier deviceAASIriId = new CustomId("https://example.com/ids/aas/7053_6021_1032_9066");
	private static BaSyxContextConfiguration aasContextConfig;
	
	private static ClientAndServer mockServer;

	@BeforeClass
	public static void setUp() throws IOException {
		configureAndStartMockserver();
		
		configureAndStartMqttBroker();

		configureAndStartAasServer();
		
		configureAndStartUpdaterComponent();
	}
	
	@AfterClass
	public static void tearDown() {
		updater.stopComponent();
		aasServer.stopComponent();
		mockServer.close();
	}
	
	@Test
	public void getPropertyBValue() throws MqttSecurityException, MqttPersistenceException, MqttException, InterruptedException {
		publishNewDatapoint("PropertyB");
		
		waitForPropagation();
		
		checkIfPropertyIsUpdated();
	}
	
	@Test
	public void getPropertyCValue() throws MqttSecurityException, MqttPersistenceException, MqttException, InterruptedException {
		publishNewDatapoint("PropertyC");
		
		waitForPropagation();
		
		checkIfPropertyIsUpdatedInEncodedAASEndpoint();
	}
	
	@Test
	public void getPropertyDValue() throws MqttSecurityException, MqttPersistenceException, MqttException, InterruptedException {
		publishNewDatapoint("PropertyD");
		
		waitForPropagation();
		
		verifyPropertyValueUpdateRequest();
	}
	
	private static void configureAndStartAasServer() {
		aasContextConfig = new BaSyxContextConfiguration(4001, "");
		BaSyxAASServerConfiguration aasConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY, "aasx/updatertest.aasx");
		aasServer = new AASServerComponent(aasContextConfig, aasConfig);
		aasServer.setRegistry(registry);
		
		aasServer.startComponent();
	}

	private static void configureAndStartMockserver() {
		mockServer = ClientAndServer.startClientAndServer(4002);

		createExpectationForPatchRequest();
	}

	private static void configureAndStartUpdaterComponent() {
		ClassLoader loader = TestAASUpdater.class.getClassLoader();
		RoutesConfiguration configuration = new RoutesConfiguration();

		RoutesConfigurationFactory routesFactory = new RoutesConfigurationFactory(loader);
		configuration.addRoutes(routesFactory.create());

		MqttDefaultConfigurationFactory mqttConfigFactory = new MqttDefaultConfigurationFactory(loader);
		configuration.addDatasources(mqttConfigFactory.create());

		AASProducerDefaultConfigurationFactory aasConfigFactory = new AASProducerDefaultConfigurationFactory(loader);
		configuration.addDatasinks(aasConfigFactory.create());

		JsonataDefaultConfigurationFactory jsonataConfigFactory = new JsonataDefaultConfigurationFactory(loader);
		configuration.addTransformers(jsonataConfigFactory.create());

		updater = new DataBridgeComponent(configuration);
		updater.startComponent();
	}

	private void waitForPropagation() throws InterruptedException {
		Thread.sleep(5000);
	}

	private void checkIfPropertyIsUpdated() throws InterruptedException {
		ConnectedAssetAdministrationShell aas = getAAS(deviceAASPlainId);
		
		ISubmodelElement updatedProp = getSubmodelElement(aas, "ConnectedSubmodel", "ConnectedPropertyB");

		Object propValue = updatedProp.getValue();
		
		assertEquals("858383", propValue);
	}
	
	private void checkIfPropertyIsUpdatedInEncodedAASEndpoint() throws InterruptedException {
		ConnectedAssetAdministrationShell aas = getAAS(deviceAASIriId);
		
		ISubmodelElement updatedProp = getSubmodelElement(aas, "ConnectedTestSubmodel", "ConnectedPropertyC");
		
		Object propValue = updatedProp.getValue();
		
		assertEquals("858383", propValue);
	}

	private void publishNewDatapoint(String topic) throws MqttException, MqttSecurityException, MqttPersistenceException {
		logger.info("Publishing event to {}", topic);
		
		String json = "{\"Account\":{\"Account Name\":\"Firefly\",\"Order\":[{\"OrderID\":\"order103\",\"Product\":[{\"Product Name\":\"Bowler Hat\",\"ProductID\":858383,\"SKU\":\"0406654608\",\"Description\":{\"Colour\":\"Purple\",\"Width\":300,\"Height\":200,\"Depth\":210,\"Weight\":0.75},\"Price\":34.45,\"Quantity\":2},{\"Product Name\":\"Trilby hat\",\"ProductID\":858236,\"SKU\":\"0406634348\",\"Description\":{\"Colour\":\"Orange\",\"Width\":300,\"Height\":200,\"Depth\":210,\"Weight\":0.6},\"Price\":21.67,\"Quantity\":1}]},{\"OrderID\":\"order104\",\"Product\":[{\"Product Name\":\"Bowler Hat\",\"ProductID\":858383,\"SKU\":\"040657863\",\"Description\":{\"Colour\":\"Purple\",\"Width\":300,\"Height\":200,\"Depth\":210,\"Weight\":0.75},\"Price\":34.45,\"Quantity\":4},{\"ProductID\":345664,\"SKU\":\"0406654603\",\"Product Name\":\"Cloak\",\"Description\":{\"Colour\":\"Black\",\"Width\":30,\"Height\":20,\"Depth\":210,\"Weight\":2},\"Price\":107.99,\"Quantity\":1}]}]}}";
		MqttClient mqttClient = new MqttClient("tcp://localhost:1884", "testClient", new MemoryPersistence());
		mqttClient.connect();
		mqttClient.publish(topic, new MqttMessage(json.getBytes()));
		mqttClient.disconnect();
		mqttClient.close();
	}

	private static void configureAndStartMqttBroker() throws IOException {
		mqttBroker = new Server();
		IResourceLoader classpathLoader = new ClasspathResourceLoader();
		final IConfig classPathConfig = new ResourceLoaderConfig(classpathLoader);
		mqttBroker.startServer(classPathConfig);
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
	
	@SuppressWarnings("resource")
	private static void createExpectationForPatchRequest() {
		new MockServerClient("localhost", 4002)
				.when(request().withMethod("PATCH").withPath(PROPERTY_VALUE_PATH)
						.withBody(PROPERTY_VALUE).withHeader("Content-Type", "application/json"))
				.respond(response().withStatusCode(HttpStatus.SC_CREATED)
						.withHeaders(new Header("Content-Type", "application/json; charset=utf-8")));
	}

	@SuppressWarnings("resource")
	private void verifyPropertyValueUpdateRequest() {
		new MockServerClient("localhost", 4002).verify(request().withMethod("PATCH")
				.withPath(PROPERTY_VALUE_PATH).withHeader("Content-Type", "application/json")
				.withBody(PROPERTY_VALUE), VerificationTimes.exactly(1));
	}
}
