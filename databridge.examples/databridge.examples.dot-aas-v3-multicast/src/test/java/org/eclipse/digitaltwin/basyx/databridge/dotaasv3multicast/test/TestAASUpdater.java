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
package org.eclipse.digitaltwin.basyx.databridge.dotaasv3multicast.test;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;

import org.apache.http.HttpStatus;
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
	private static final String INPUT_DATA = "{\"Process\": {\"ProcessID\": \"00001\",\"Result\": true,\"ProcessData\": {\"Volume_NV\": 410.5,\"Volume_AV\": 407.7,\"Charge\": \"0000000001\",\"Pressure\": \"43.31\",\"Speed\": \"0.158790365\",\"Temperature\": \"42\",\"Result\": \"1\",\"StartProcess\": \"2022-06-22T08:21:33.4300238Z\",\"Volume\": \"39.7\",\"Weight\": \"49.69\",\"EndPorcess\": \"2022-06-22T08:22:56.4085953Z\"}}}";

	private static final String PROPERTY_PROCESS_ID_VALUE_PATH = "/submodels/cHJvY2Vzcw==/submodel-elements/processId/$value";
	private static final String PROPERTY_PROCESS_ID_VALUE = "\"00001\"";
	private static final String PROPERTY_PROCESS_RESULT_VALUE_PATH = "/submodels/cHJvY2Vzcw==/submodel-elements/processResult/$value";
	private static final String PROPERTY_PROCESS_RESULT_VALUE = "\"true\"";
	private static final String PROPERTY_PROCESS_DATA_VALUE_PATH = "/submodels/cHJvY2Vzcw==/submodel-elements/processData/$value";
	private static final String PROPERTY_PROCESS_DATA_VALUE = "\"{\"ProcessData\":{\"Volume_NV\":410.5,\"Volume_AV\":407.7,\"Charge\":\"0000000001\",\"Pressure\":\"43.31\",\"Speed\":\"0.158790365\",\"Temperature\":\"42\",\"Result\":\"1\",\"StartProcess\":\"2022-06-22T08:21:33.4300238Z\",\"Volume\":\"39.7\",\"Weight\":\"49.69\",\"EndPorcess\":\"2022-06-22T08:22:56.4085953Z\"}}\"";
	private static final String PROPERTY_PROCESS_DATA_RAW_VALUE_PATH = "/submodels/cHJvY2Vzcw==/submodel-elements/processData_raw/$value";
	private static final String PROPERTY_PROCESS_DATA_RAW_VALUE = "\"" + INPUT_DATA + "\"";
	private static final String PROPERTY_PROCESS_DURATION_VALUE_PATH = "/submodels/cHJvY2Vzcw==/submodel-elements/processDuration/$value";
	private static final String PROPERTY_PROCESS_DURATION_VALUE = "\"1M22S\"";

	private static Logger logger = LoggerFactory.getLogger(TestAASUpdater.class);

	private static DataBridgeComponent updater;
	protected static Server mqttBroker;

	private static ClientAndServer mockServer;

	@BeforeClass
	public static void setUp() throws IOException {
		configureAndStartMockserver();

		configureAndStartMqttBroker();

		configureAndStartUpdaterComponent();
		try {
			publishNewDatapoint("process-update");
			waitForPropagation();
		} catch (MqttException | InterruptedException e) {
			// ignore
		}
	}

	@AfterClass
	public static void tearDown() {
		updater.stopComponent();
		mockServer.close();
	}

	@Test
	public void singleMappedTransformer1() throws MqttSecurityException, MqttPersistenceException, MqttException, InterruptedException {
		verifyCall(PROPERTY_PROCESS_ID_VALUE_PATH, PROPERTY_PROCESS_ID_VALUE);
	}

	@Test
	public void singleMappedTransformer2() throws MqttSecurityException, MqttPersistenceException, MqttException, InterruptedException {
		verifyCall(PROPERTY_PROCESS_RESULT_VALUE_PATH, PROPERTY_PROCESS_RESULT_VALUE);
	}

	@Test
	public void jsonResultMappedTransformer() throws MqttSecurityException, MqttPersistenceException, MqttException, InterruptedException {
		verifyCall(PROPERTY_PROCESS_DATA_VALUE_PATH, PROPERTY_PROCESS_DATA_VALUE);

	}

	@Test
	public void noMappedTransformer() throws MqttSecurityException, MqttPersistenceException, MqttException, InterruptedException {
		verifyCall(PROPERTY_PROCESS_DATA_RAW_VALUE_PATH, PROPERTY_PROCESS_DATA_RAW_VALUE);
	}

	@Test
	public void multipleMappedTransformers() throws MqttSecurityException, MqttPersistenceException, MqttException, InterruptedException {
		verifyCall(PROPERTY_PROCESS_DURATION_VALUE_PATH, PROPERTY_PROCESS_DURATION_VALUE);
	}

	private static void configureAndStartMockserver() {
		mockServer = ClientAndServer.startClientAndServer(4001);

		createExpectationForRequest(PROPERTY_PROCESS_ID_VALUE_PATH, PROPERTY_PROCESS_ID_VALUE);
		createExpectationForRequest(PROPERTY_PROCESS_RESULT_VALUE_PATH, PROPERTY_PROCESS_RESULT_VALUE);
		createExpectationForRequest(PROPERTY_PROCESS_DATA_VALUE_PATH, PROPERTY_PROCESS_DATA_VALUE);
		createExpectationForRequest(PROPERTY_PROCESS_DATA_RAW_VALUE_PATH, PROPERTY_PROCESS_DATA_RAW_VALUE);
		createExpectationForRequest(PROPERTY_PROCESS_DURATION_VALUE_PATH, PROPERTY_PROCESS_DURATION_VALUE);
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

	private static void waitForPropagation() throws InterruptedException {
		Thread.sleep(5000);
	}

	private static void publishNewDatapoint(String topic) throws MqttException, MqttSecurityException, MqttPersistenceException {
		logger.info("Publishing event:\n{}\nto topic: {}", INPUT_DATA, topic);

		MqttClient mqttClient = new MqttClient("tcp://localhost:1884", "testClient", new MemoryPersistence());
		mqttClient.connect();
		mqttClient.publish(topic, new MqttMessage(INPUT_DATA.getBytes()));
		System.out.println(INPUT_DATA);
		mqttClient.disconnect();
		mqttClient.close();
	}

	private static void configureAndStartMqttBroker() throws IOException {
		mqttBroker = new Server();
		IResourceLoader classpathLoader = new ClasspathResourceLoader();
		final IConfig classPathConfig = new ResourceLoaderConfig(classpathLoader);
		mqttBroker.startServer(classPathConfig);
	}

	@SuppressWarnings("resource")
	private static void createExpectationForRequest(String path, String value) {
		new MockServerClient("localhost", 4001).when(request().withMethod("PATCH")
				.withPath(path)
				.withBody(value)
				.withHeader("Content-Type", "application/json"))
				.respond(response().withStatusCode(HttpStatus.SC_CREATED)
						.withHeaders(new Header("Content-Type", "application/json; charset=utf-8")));
	}

	@SuppressWarnings("resource")
	private void verifyCall(String path, String value) {
		new MockServerClient("localhost", 4001).verify(request().withMethod("PATCH")
				.withPath(path)
				.withHeader("Content-Type", "application/json")
				.withBody(value), VerificationTimes.exactly(1));
	}
}
