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

package org.eclipse.digitaltwin.basyx.databridge.examples.aasjsonatarest.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
import org.eclipse.digitaltwin.basyx.databridge.rest.configuration.factory.RestDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.timer.configuration.factory.TimerDefaultConfigurationFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.HttpStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A test of aas-jsonata-rest
 * @author rana
 *
 */
public class TestAASUpdater {

	private static Logger logger = LoggerFactory.getLogger(TestAASUpdater.class);
	private static AASServerComponent aasServer;
	private static BaSyxContextConfiguration aasContextConfig;
	private static InMemoryRegistry registry = new InMemoryRegistry();
	private static DataBridgeComponent updater;
	private static int PORT = 8080;
	private static ClientAndServer clientServer;
	
	@BeforeClass
	public static void setUp() throws Exception {

		configureAndStartAasServer();
		
		mockServerConfig();
	
		configureAndStartUpdaterComponent();
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		updater.stopComponent();
		
		aasServer.stopComponent();
	}
	
	@Test
	public void CheckPropertyValueA() throws  InterruptedException, ClientProtocolException, IOException {
		
		String expected_value = "103.5585973";
		
		startHTTPServer(getResponseBody(expected_value));
		
		String http_url = "http://localhost:8080/data/machine/pressure";
		String actualValue = getContentFromDelegatedEndpoint(http_url);
		
		assertEquals(actualValue, expected_value);
	}
	
	@Test
	public void CheckPropertyValueB() throws  InterruptedException, ClientProtocolException, IOException, URISyntaxException {
		
		String expected_value = getExpectedValueFromFile();
		
		startHTTPServer(getResponseBody(expected_value));
		
		String http_url = "http://localhost:8080/data/machine/pressure_rotation";
		String actualValue = getContentFromDelegatedEndpoint(http_url);
		
		assertEquals(actualValue, expected_value);
	}
	
	private static void mockServerConfig() {
		 clientServer = ClientAndServer.startClientAndServer(PORT);
		 
		 logger.info("Mock Sever Started ..... ");
	}
	
	
	private static void startHTTPServer(String body) {
		
		clientServer.reset().when(HttpRequest.request().withMethod("PUT"))
				.respond(HttpResponse.response().withStatusCode(HttpStatusCode.OK_200.code())
						.withBody(body));
	}
	
	private String getContentFromDelegatedEndpoint(String url) throws IOException, ClientProtocolException {
		
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPut request = new HttpPut(url);
		CloseableHttpResponse resp = client.execute(request);
		HttpEntity entity = resp.getEntity();
		String content = EntityUtils.toString(entity, "UTF-8");

		client.close();
		return content;
	}
	
	private static String getResponseBody(String expected_Value) {
		return ""+ expected_Value +"";
	}
	
	private static void configureAndStartAasServer() throws InterruptedException {
		
		aasContextConfig = new BaSyxContextConfiguration(4001, "");
		
		BaSyxAASServerConfiguration aasConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY, "aasx/telemeteryTest.aasx");
		
		aasServer = new AASServerComponent(aasContextConfig, aasConfig);
		aasServer.setRegistry(registry);
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

		RestDefaultConfigurationFactory httpConfigFactory = new RestDefaultConfigurationFactory(loader);
		configuration.addDatasinks(httpConfigFactory.create());
		
		JsonataDefaultConfigurationFactory jsonataConfigFactory = new JsonataDefaultConfigurationFactory(loader);
		configuration.addTransformers(jsonataConfigFactory.create());

		updater = new DataBridgeComponent(configuration);
		updater.startComponent();
	}
	
	private static String getExpectedValueFromFile() throws IOException, URISyntaxException {
		
		String filename = "submodelproperties.json";
		URL resource = TestAASUpdater.class.getClassLoader().getResource(filename);
	    byte[] content = Files.readAllBytes(Paths.get(resource.toURI()));
		return new String(content);
	}

}
