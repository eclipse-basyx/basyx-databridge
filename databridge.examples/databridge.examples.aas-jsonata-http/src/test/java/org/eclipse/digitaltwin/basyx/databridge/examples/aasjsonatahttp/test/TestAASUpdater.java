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

package org.eclipse.digitaltwin.basyx.databridge.examples.aasjsonatahttp.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.servlet.http.HttpServlet;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.basyx.aas.registration.memory.InMemoryRegistry;
import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.aas.configuration.factory.AASPollingConsumerDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.component.DataBridgeComponent;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.factory.RoutesConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RoutesConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.examples.httpserver.HttpDataSource;
import org.eclipse.digitaltwin.basyx.databridge.httppolling.configuration.factory.HttpProducerDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.jsonata.configuration.factory.JsonataDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.timer.configuration.factory.TimerDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.examples.aasjsonatahttp.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A test of aas-jsonata-http
 * @author rana
 *
 */
public class TestAASUpdater {

	private static AASServerComponent aasServer;
	private static BaSyxContextConfiguration aasContextConfig;
	private static InMemoryRegistry registry = new InMemoryRegistry();
	private static DataBridgeComponent updater;
	private static HttpDataSource httpData;
	private static HttpServlet httpServlet = new Server();
	private static int PORT = 8091;

	@BeforeClass
	public static void setUp() throws Exception {

		configureAndStartAasServer();
		
		serverStart();
		
		configureAndStartUpdaterComponent();
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		
		updater.stopComponent();
		
		aasServer.stopComponent();
		
		serverStop();
	}
	
	@Test
	public void CheckPropertyValueA() throws  InterruptedException, IOException {
		
		String expected_value = wrapStringValue("103.5585973");
		
		Thread.sleep(8000);
		
		String actualValue = getContentFromEndPoint();
		
		assertEquals(expected_value, actualValue);
	}
	
	@Test
	public void CheckPropertyValueB() throws  InterruptedException, IOException, URISyntaxException {
		
		String expected_value = getExpectedValueFromFile();
		
		Thread.sleep(6000);
		
		String actualValue = getContentFromEndPoint();
		
		ObjectMapper mapper = new ObjectMapper();
		
		assertEquals(mapper.readTree(actualValue), mapper.readTree(expected_value));
	}
	
	private String getContentFromEndPoint() throws ClientProtocolException, IOException, InterruptedException {
		
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet request = new HttpGet("http://localhost:8091");
		CloseableHttpResponse resp = client.execute(request);
		String content = new String(resp.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
		
		client.close();
		return content;
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

		HttpProducerDefaultConfigurationFactory httpConfigFactory = new HttpProducerDefaultConfigurationFactory(loader);
		configuration.addDatasinks(httpConfigFactory.create());
		
		JsonataDefaultConfigurationFactory jsonataConfigFactory = new JsonataDefaultConfigurationFactory(loader);
		configuration.addTransformers(jsonataConfigFactory.create());

		updater = new DataBridgeComponent(configuration);
		updater.startComponent();
	}
	
	private String getExpectedValueFromFile() throws IOException, URISyntaxException {
		
		String filename = "submodelproperties.json";
		URL resource = TestAASUpdater.class.getClassLoader().getResource(filename);
	    byte[] content = Files.readAllBytes(Paths.get(resource.toURI()));
		return new String(content);
	}
	
	
	private static void serverStart() throws InterruptedException {
		
		httpData = new HttpDataSource();
		httpData.runHttpServer("localhost", PORT, httpServlet);	
	}
	
	private static void serverStop() {
		
		httpData.stopHttpServer();
	}
	
	private String wrapStringValue(String value) {
		return "\"" + value + "\"";
	}
}
