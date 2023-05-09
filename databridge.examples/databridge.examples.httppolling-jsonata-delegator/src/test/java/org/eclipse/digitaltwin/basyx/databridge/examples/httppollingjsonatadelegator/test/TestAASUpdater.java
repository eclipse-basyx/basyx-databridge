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
package org.eclipse.digitaltwin.basyx.databridge.examples.httppollingjsonatadelegator.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.digitaltwin.basyx.databridge.core.component.DataBridgeComponent;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.factory.RoutesConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RoutesConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.httppolling.configuration.factory.HttpPollingDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.jsonata.configuration.factory.JsonataDefaultConfigurationFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.HttpStatusCode;

/**
 * Tests the delegator scenario with httppolling
 *
 * @author danish
 *
 */
public class TestAASUpdater {
	private static final String EXPECTED_VALUE = "858383";
	private static DataBridgeComponent delegatorComponent;

	@BeforeClass
	public static void setUp() throws IOException {
		startHTTPServer();

		startDataBridgeComponent();
	}

	@Test
	public void transformedResponseIsReturned() throws ClientProtocolException, IOException {
		String actualValue = getContentFromDelegatedEndpoint();

		assertEquals(EXPECTED_VALUE, actualValue);
	}

	private static void startHTTPServer() {
		ClientAndServer clientServer = ClientAndServer.startClientAndServer(2018);

		clientServer.when(HttpRequest.request().withMethod("GET"))
				.respond(HttpResponse.response().withStatusCode(HttpStatusCode.OK_200.code())
						.withBody(getResponseBody()));
	}

	private static void startDataBridgeComponent() {
		delegatorComponent = new DataBridgeComponent(addConfigurations());
		delegatorComponent.startComponent();
	}

	private static RoutesConfiguration addConfigurations() {
		RoutesConfiguration configuration = new RoutesConfiguration();

		ClassLoader loader = TestAASUpdater.class.getClassLoader();

		RoutesConfigurationFactory routesFactory = new RoutesConfigurationFactory(loader);
		configuration.addRoutes(routesFactory.create());

		HttpPollingDefaultConfigurationFactory httpPollingConfigFactory = new HttpPollingDefaultConfigurationFactory(
				loader);
		configuration.addDatasources(httpPollingConfigFactory.create());

		JsonataDefaultConfigurationFactory jsonataConfigFactory = new JsonataDefaultConfigurationFactory(loader);
		configuration.addTransformers(jsonataConfigFactory.create());

		return configuration;
	}
	
	private String getContentFromDelegatedEndpoint() throws IOException, ClientProtocolException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet request = new HttpGet("http://localhost:8090/valueA");
		CloseableHttpResponse resp = client.execute(request);
		String content = new String(resp.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

		client.close();
		return content;
	}
	
	private static String getResponseBody() {
		return "{\"objects\":\n" + "[\n" + "{\"name\":\"object1\", \"value\":" + EXPECTED_VALUE
				+ "},\n" + "{\"name\":\"object2\", \"value\":42}\n" + "]\n" + "}";
	}

	@AfterClass
	public static void tearDown() {
		delegatorComponent.stopComponent();
	}
}
