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
package org.eclipse.digitaltwin.basyx.databridge.examples.dotaasv3jsonatamqtt.test;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.eclipse.digitaltwin.basyx.databridge.aas.configuration.factory.AASPollingConsumerDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.component.DataBridgeComponent;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.factory.RoutesConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RoutesConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.jsonata.configuration.factory.JsonataDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.paho.configuration.factory.MqttDataSinkDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.timer.configuration.factory.TimerDefaultConfigurationFactory;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.moquette.broker.Server;
import io.moquette.broker.config.ClasspathResourceLoader;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.IResourceLoader;
import io.moquette.broker.config.ResourceLoaderConfig;

public class TestAASUpdater {

	public static final String PRESSURE_SME_PATH = "/submodels/aHR0cHM6Ly9leGFtcGxlLmNvbS9pZHMvc20vODU4M18zMTQwXzcwMzJfOTc2Ng/submodel-elements/pressure/";
	public static final String ROTATION_SME_PATH = "/submodels/aHR0cHM6Ly9leGFtcGxlLmNvbS9pZHMvc20vODU4M18zMTQwXzcwMzJfOTc2Ng/submodel-elements/rotation/";
	public static final String COMPLETE_SUBMODEL_PATH = "/submodels/aHR0cHM6Ly9leGFtcGxlLmNvbS9pZHMvc20vODU4M18zMTQwXzcwMzJfOTc2Ng/";

	private static String mqtt_broker_url = "tcp://127.0.0.1:1884";
	private static String user_name = "test1";
	private static String password = "1234567";
	private static String client_id = UUID.randomUUID().toString();

	private static Logger logger = LoggerFactory.getLogger(TestAASUpdater.class);
	private static String receivedMessage;

	private static DataBridgeComponent updater;
	protected static Server mqttBroker;
	
	private static ClientAndServer mockServer;

	@BeforeClass
	public static void setUp() throws IOException {
		configureAndStartMockserver();
		
		configureAndStartMqttBroker();
		
		configureAndStartUpdaterComponent();
	}
	
	@AfterClass
	public static void tearDown() {
		updater.stopComponent();
		mockServer.close();
	}

	@Test
	public void getUpdatedPropertyValueA() throws MqttException, MqttSecurityException, MqttPersistenceException, InterruptedException, JsonProcessingException {

		String topic = "aas/pressure";
		String expectedValue = wrapStringValue("103.5585973");

		assertPropertyValue(expectedValue, topic);
	}

	@Test
	public void getUpdatedPropertyValueB() throws MqttException, MqttSecurityException, MqttPersistenceException, InterruptedException, JsonProcessingException {

		String topic = "aas/rotation";
		String expectedValue = wrapStringValue("379.5784558");

		assertPropertyValue(expectedValue, topic);
	}

	@Test
	public void getAllProperties() throws MqttException, MqttSecurityException, MqttPersistenceException, InterruptedException, IOException, URISyntaxException {

		String topic = "aas/pressure_rotation";
		String expectedValue = getExpectedValueFromFile();

		assertAllProperties(expectedValue, topic);
	}

	private void assertPropertyValue(String expectedValue, String topic) throws MqttSecurityException, MqttPersistenceException, MqttException, InterruptedException {

		fetchExpectedValue(topic);

		assertEquals(receivedMessage, expectedValue);
	}

	private void assertAllProperties(String expectedValue, String topic) throws MqttSecurityException, MqttPersistenceException, MqttException, InterruptedException, JsonMappingException, JsonProcessingException {

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

	private static MqttClient mqttConnectionInitiate() throws MqttException {

		MqttClient mqttClient = new MqttClient(mqtt_broker_url, client_id, new MemoryPersistence());

		MqttConnectOptions connOpts = setUpMqttConnection(user_name, password);
		mqttClient.connect(connOpts);
		connOpts.setCleanSession(true);
		return mqttClient;
	}

	private static MqttConnectOptions setUpMqttConnection(String username, String password) {
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setUserName(username);
		connOpts.setPassword(password.toCharArray());
		return connOpts;
	}

	private static String getExpectedValueFromFile() throws IOException, URISyntaxException {

		String filename = "submodelproperties.json";
		URL resource = TestAASUpdater.class.getClassLoader().getResource(filename);
		byte[] content = Files.readAllBytes(Paths.get(resource.toURI()));
		return new String(content);
	}

	private String wrapStringValue(String value) {
		return "\"" + value + "\"";
	}

	private static void configureAndStartMockserver() throws IOException {
		mockServer = ClientAndServer.startClientAndServer(4001);

		createExpectationForGetRequestForPressureValue();

		createExpectationForGetRequestForRotationValue();

		createExpectationForGetRequestForCompleteSubmodel();
	}

	private static void configureAndStartUpdaterComponent() {
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

	private static void configureAndStartMqttBroker() throws IOException {
		mqttBroker = new Server();
		IResourceLoader classpathLoader = new ClasspathResourceLoader("config.moquette");
		final IConfig classPathConfig = new ResourceLoaderConfig(classpathLoader);
		mqttBroker.startServer(classPathConfig);
	}

	@SuppressWarnings("resource")
	private static void createExpectationForGetRequestForPressureValue() throws IOException {
		InputStream stream = TestAASUpdater.class.getResourceAsStream("/pressure.json");
		String exampleRequest = IOUtils.toString(stream, StandardCharsets.UTF_8);

		new MockServerClient("localhost", 4001)
				.when(request().withMethod("GET").withPath(PRESSURE_SME_PATH))
				.respond(response().withStatusCode(HttpStatus.SC_OK)
						.withBody(exampleRequest)
						.withHeaders(new Header("Content-Type", "application/json; charset=utf-8")));
	}

	@SuppressWarnings("resource")
	private static void createExpectationForGetRequestForRotationValue() throws IOException {
		InputStream stream = TestAASUpdater.class.getResourceAsStream("/rotation.json");
		String exampleRequest = IOUtils.toString(stream, StandardCharsets.UTF_8);
		new MockServerClient("localhost", 4001)
				.when(request().withMethod("GET").withPath(ROTATION_SME_PATH))
				.respond(response().withStatusCode(HttpStatus.SC_OK)
						.withBody(exampleRequest)
						.withHeaders(new Header("Content-Type", "application/json; charset=utf-8")));
	}

	@SuppressWarnings("resource")
	private static void createExpectationForGetRequestForCompleteSubmodel() throws IOException {
		InputStream stream = TestAASUpdater.class.getResourceAsStream("/completeSubmodel.json");
		String exampleRequest = IOUtils.toString(stream, StandardCharsets.UTF_8);
		new MockServerClient("localhost", 4001)
				.when(request().withMethod("GET").withPath(COMPLETE_SUBMODEL_PATH))
				.respond(response().withStatusCode(HttpStatus.SC_OK)
						.withBody(exampleRequest)
						.withHeaders(new Header("Content-Type", "application/json; charset=utf-8")));
	}

}
