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
package org.eclipse.digitaltwin.basyx.databridge.examples.sqlaas;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.digitaltwin.basyx.databridge.aas.configuration.factory.AASProducerDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.component.DataBridgeComponent;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.factory.RoutesConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RoutesConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.jsonata.configuration.factory.JsonataDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.jsonjackson.configuration.factory.JsonJacksonDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.sql.configuration.factory.SqlDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.timer.configuration.factory.TimerDefaultConfigurationFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.verify.VerificationTimes;

/**
 * @author jungjan, mateusmolina
 */
public class TestAASUpdater {
	private static final String VALUE_PATH = "/submodels/dmFsdWU=/submodel-elements/value/$value";
	
	private static DataBridgeComponent updater;

	private static ClientAndServer mockServer;
	
	private static ClassLoader loader = TestAASUpdater.class.getClassLoader();

	@BeforeClass
	public static void setUp() throws IOException {
		configureAndStartMockserver();
		String path = System.getProperty("user.home");
		System.out.println(path);
		
		try (TestDb testDb = new TestDb("src/main/resources/values.db")) {
			
			testDb.startInsertingValues(1);
			
			configureAndStartUpdaterComponent();
			
			TimeUnit.SECONDS.sleep(6);
			
			testDb.stopInsertingValues();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void tearDown() {
		updater.stopComponent();
		mockServer.close();
	}

	@Test
	public void testSqlConfiguration() throws InterruptedException {
		verifyCalls(VALUE_PATH, 2);
	}

	private static void configureAndStartMockserver() {
		mockServer = ClientAndServer.startClientAndServer(4001);
	}

	private static void configureAndStartUpdaterComponent() {
		RoutesConfiguration configuration = new RoutesConfiguration();
		
		RoutesConfigurationFactory routesFactory = new RoutesConfigurationFactory(loader);
		configuration.addRoutes(routesFactory.create());

		SqlDefaultConfigurationFactory sqlConfigFactory = new SqlDefaultConfigurationFactory(loader);
		configuration.addDatasources(sqlConfigFactory.create());

		TimerDefaultConfigurationFactory timerConfigFactory = new TimerDefaultConfigurationFactory(loader);
		configuration.addDatasources(timerConfigFactory.create());

		AASProducerDefaultConfigurationFactory aasConfigFactory = new AASProducerDefaultConfigurationFactory(loader);
		configuration.addDatasinks(aasConfigFactory.create());

		JsonJacksonDefaultConfigurationFactory jsonJacksonConfigFactory = new JsonJacksonDefaultConfigurationFactory(loader);
		configuration.addTransformers(jsonJacksonConfigFactory.create());

		JsonataDefaultConfigurationFactory jsonataConfigFactory = new JsonataDefaultConfigurationFactory(loader);
		configuration.addTransformers(jsonataConfigFactory.create());

		updater = new DataBridgeComponent(configuration);
		updater.startComponent();
	}

	@SuppressWarnings("resource")
	private void verifyCalls(String path, int atLeastCalledTimes) {
		MockServerClient mockServerClient = new MockServerClient("localhost", 4001).verify(request().withMethod("PATCH")
				.withPath(path)
				.withHeader("Content-Type", "application/json"), VerificationTimes.atLeast(atLeastCalledTimes));

		verifyUniqueBodies(path, mockServerClient);
	}

	private void verifyUniqueBodies(String path, MockServerClient mockServerClient) {
		HttpRequest[] recordedRequests = retrieveRequests(path, mockServerClient);

		Set<String> uniqueBodies = Arrays.stream(recordedRequests).map(HttpRequest::getBodyAsString).collect(Collectors.toSet());
		assertEquals(recordedRequests.length, uniqueBodies.size());
	}

	private HttpRequest[] retrieveRequests(String path, MockServerClient mockServerClient) {
		return mockServerClient.retrieveRecordedRequests(HttpRequest.request()
				.withMethod("PATCH")
				.withPath(path)
				.withHeader("Content-Type", "application/json"));
	}
}
