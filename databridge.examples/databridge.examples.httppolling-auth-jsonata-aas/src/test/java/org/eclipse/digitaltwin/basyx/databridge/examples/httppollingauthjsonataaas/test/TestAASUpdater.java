/*******************************************************************************
 * Copyright (C) 2024 the Eclipse BaSyx Authors
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
package org.eclipse.digitaltwin.basyx.databridge.examples.httppollingauthjsonataaas.test;
import org.apache.http.HttpStatus;
import org.eclipse.digitaltwin.basyx.databridge.aas.configuration.factory.AASProducerDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.component.DataBridgeComponent;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.factory.RoutesConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RoutesConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.httppolling.configuration.factory.HttpPollingDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.jsonata.configuration.factory.JsonataDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.timer.configuration.factory.TimerDefaultConfigurationFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.HttpStatusCode;
import org.mockserver.verify.VerificationTimes;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * Test class to test access to a secured Endpoint
 *
 * @author zielstor, fried
 */
public class TestAASUpdater {
	private static final String PATH_PROPERTY_1 = "/value1";
	private static final String PATH_PROPERTY_2 = "/value2";
	private static final String ENDPOINT_1 = "/submodels/aHR0cHM6Ly9leGFtcGxlLmNvbS9pZHMvU3VibW9kZWwvMzE3NV80MTMxXzc2MzFfMzQ5Mw/submodel-elements/intProperty/$value";
	private static final String ENDPOINT_2 = "/submodels/aHR0cHM6Ly9leGFtcGxlLmNvbS9pZHMvU3VibW9kZWwvMzE3NV80MTMxXzc2MzFfMzQ5Mw/submodel-elements/stringProperty/$value";
	private static final String CONSUMER1_RESULT = "[{\"value\":\"10\"}]";
	private static final String CONSUMER2_RESULT = "[{\"value\":\"Test\"}]";
	private static final String PRODUCER1_BODY = "\"10\"";
	private static final String PRODUCER2_BODY = "\"Test\"";
	public static final String AUTH_TOKEN = "Basic ZGVtb1VzZXJuYW1lOmRlbW9QYXNzd29yZA==";

	private static DataBridgeComponent updater;

	private static ClientAndServer mockProducer;
	private static ClientAndServer mockConsumer;

	@BeforeClass
	public static void setUp() throws IOException {
		mockProducer = createAndStartMockProducer();
		mockConsumer = createAndStartMockConsumer();
	}

	@Test
	public void test() throws Exception {
		System.out.println("START UPDATER");
		ClassLoader loader = TestAASUpdater.class.getClassLoader();
		RoutesConfiguration configuration = new RoutesConfiguration();

		// Extend configutation for connections
		// DefaulRoutesConfigFac takes default routes.json as to config
		RoutesConfigurationFactory routesFactory = new RoutesConfigurationFactory(loader);
		configuration.addRoutes(routesFactory.create());

		// Extend configuration for Http Source
		HttpPollingDefaultConfigurationFactory httpPollingConfigFactory = new HttpPollingDefaultConfigurationFactory(loader);
		configuration.addDatasources(httpPollingConfigFactory.create());

		// Extend configuration for AAS
		// DefaulRoutesConfigFactory takes default aasserver.json as to config
		AASProducerDefaultConfigurationFactory aasConfigFactory = new AASProducerDefaultConfigurationFactory(loader);
		configuration.addDatasinks(aasConfigFactory.create());

		// Extend configuration for Jsonata
		JsonataDefaultConfigurationFactory jsonataConfigFactory = new JsonataDefaultConfigurationFactory(loader);
		configuration.addTransformers(jsonataConfigFactory.create());

		// Extend configuration for Timer
		TimerDefaultConfigurationFactory timerConfigFactory = new TimerDefaultConfigurationFactory(loader);
		configuration.addDatasources(timerConfigFactory.create());

		updater = new DataBridgeComponent(configuration);
		updater.startComponent();

		System.out.println("UPDATER STARTED");

		waitForPropagation();

		assertEndpointCalledOneTime(ENDPOINT_1, PRODUCER1_BODY);
		assertEndpointCalledOneTime(ENDPOINT_2, PRODUCER2_BODY);

		updater.stopComponent();
	}

	private static void assertEndpointCalledOneTime(String endpoint, String producerBody) {
		mockProducer.verify(request().withMethod("PATCH").withPath(endpoint).withBody(producerBody), VerificationTimes.once());
	}

	private static ClientAndServer createAndStartMockConsumer() {
		ClientAndServer clientServer = ClientAndServer.startClientAndServer(8070);

		System.out.println("MockProducer running: " + clientServer.isRunning());
		createAuthorizedConsumerExpectation(clientServer, PATH_PROPERTY_1, CONSUMER1_RESULT, AUTH_TOKEN);
		createAuthorizedConsumerExpectation(clientServer, PATH_PROPERTY_2, CONSUMER2_RESULT, AUTH_TOKEN);
		createUnauthorizedConsumerExpectation(clientServer);
		return clientServer;
	}

	private static ClientAndServer createAndStartMockProducer() {
		ClientAndServer clientServer = ClientAndServer.startClientAndServer(4001);
		createBaSyxPatchSubmodelElementValueExpectation(clientServer,ENDPOINT_1,PRODUCER1_BODY);
		createBaSyxPatchSubmodelElementValueExpectation(clientServer,ENDPOINT_2,PRODUCER2_BODY);
		return clientServer;
    }

	private static void createBaSyxPatchSubmodelElementValueExpectation(ClientAndServer clientServer, String endpoint, String producerBody){
		clientServer.when(request().withMethod("PATCH").withPath(endpoint)
						.withBody(producerBody).withHeader("Content-Type", "application/json"))
				.respond(response().withStatusCode(HttpStatus.SC_OK)
						.withHeaders(new Header("Content-Type", "application/json; charset=utf-8")));
	}

	private static void createAuthorizedConsumerExpectation(ClientAndServer clientServer, String path, String result,String token) {
		clientServer.when(
						HttpRequest.request(path)
								.withMethod("GET")
								.withHeader("Authorization", token)
				)
				.respond(
						HttpResponse.response()
								.withStatusCode(HttpStatusCode.OK_200.code())
								.withBody(result)
				);
	}

	private static void createUnauthorizedConsumerExpectation(ClientAndServer clientServer) {
		clientServer.when(
						HttpRequest.request(PATH_PROPERTY_2)
								.withMethod("GET")
				)
				.respond(
						HttpResponse.response()
								.withStatusCode(HttpStatusCode.UNAUTHORIZED_401.code())
				);
	}

	private void waitForPropagation() throws InterruptedException {
		Thread.sleep(3000);
	}

	@AfterClass
	public static void tearDown() {
		mockProducer.stop();
		mockConsumer.stop();
	}

}
