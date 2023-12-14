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
package org.eclipse.digitaltwin.basyx.databridge.examples.aasopcua.test;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.aas.registration.memory.InMemoryRegistry;
import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.digitaltwin.basyx.databridge.core.component.DataBridgeComponent;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.factory.RoutesConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RoutesConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.jsonata.configuration.factory.JsonataDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.jsonjackson.configuration.factory.JsonJacksonDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.opcua.configuration.factory.OpcuaDefaultSinkConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.aas.configuration.factory.AASPollingConsumerDefaultConfigurationFactory;
import org.eclipse.milo.examples.server.ExampleServer;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestOPCUAUpdater {
	private static AASServerComponent aasServer;
	private static DataBridgeComponent updater;
	private static InMemoryRegistry registry;
	protected static ExampleServer opcUaServer;

	protected static IIdentifier deviceAAS = new CustomId("TestUpdatedDevice");
	private static BaSyxContextConfiguration aasContextConfig;

	@BeforeClass
	public static void setUp() throws Exception {
		System.out.println("Setting up env...");
		startOpcUaServer();
		registry = new InMemoryRegistry();

		aasContextConfig = new BaSyxContextConfiguration(4001, "");
		BaSyxAASServerConfiguration aasConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY,
				"aasx/updatertest.aasx");
		aasServer = new AASServerComponent(aasContextConfig, aasConfig);
		aasServer.setRegistry(registry);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		System.out.println("Tearing down env...");
		updater.stopComponent();
		aasServer.stopComponent();
		stopOpcUaServer();
	}

	@Test
	public void test() throws Exception {
		aasServer.startComponent();
		System.out.println("AAS STARTED");
		System.out.println("START UPDATER");
		ClassLoader loader = this.getClass().getClassLoader();
		RoutesConfiguration configuration = new RoutesConfiguration();

		// Extend configutation for connections
		RoutesConfigurationFactory routesFactory = new RoutesConfigurationFactory(loader);
		configuration.addRoutes(routesFactory.create());

		// Extend configuration for Opcua Sink
		OpcuaDefaultSinkConfigurationFactory opcuaSinkConfigFactory = new OpcuaDefaultSinkConfigurationFactory(loader);
		configuration.addDatasinks(opcuaSinkConfigFactory.create());

		// Extend configuration for AAS Source
		AASPollingConsumerDefaultConfigurationFactory aasSourceConfigFactory = new AASPollingConsumerDefaultConfigurationFactory(loader);
		configuration.addDatasources(aasSourceConfigFactory.create());

		JsonJacksonDefaultConfigurationFactory jsonJacksonConfigFactory = new JsonJacksonDefaultConfigurationFactory(loader);
		configuration.addTransformers(jsonJacksonConfigFactory.create());

		// Extend configuration for Jsonata
		JsonataDefaultConfigurationFactory jsonataConfigFactory = new JsonataDefaultConfigurationFactory(loader);
		configuration.addTransformers(jsonataConfigFactory.create());

		updater = new DataBridgeComponent(configuration);
		updater.startComponent();
		System.out.println("UPDATER STARTED");
		Thread.sleep(3000);
		System.out.println("CHECK PROPERTY");
		checkOPCUAServer();
	}
	
	private void checkOPCUAServer() throws InterruptedException, ExecutionException, UaException {
		// Discover endpoints. Replace "opc.tcp://localhost:4840" with your server's endpoint URL.
	    List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints("opc.tcp://127.0.0.1:12686").get();

	    // Use the first discovered endpoint. In a production scenario, you might want to select
	    // a specific endpoint based on security policies or other criteria.
	    EndpointDescription endpoint = endpoints.get(0);

	    // Create and configure the client with the selected endpoint.
	    OpcUaClientConfigBuilder configBuilder = new OpcUaClientConfigBuilder();
	    configBuilder.setEndpoint(endpoint);

	    OpcUaClient client = OpcUaClient.create(configBuilder.build());
	    client.connect().get();

	    // Assuming you want to read the values of two nodes
	    NodeId nodeIdA = new NodeId(2, "ConnectedPropertyA");
	    NodeId nodeIdB = new NodeId(2, "ConnectedPropertyB");

	    // Read the node values
	    DataValue valueA = client.readValue(0, TimestampsToReturn.Both, nodeIdA).get();
	    DataValue valueB = client.readValue(0, TimestampsToReturn.Both, nodeIdB).get();
	    
	    // Verify the values
	    // Replace "expectedValueA" and "expectedValueB" with the actual expected values
	    Object expectedValueA = "3.14"; // Example expected value
	    Object expectedValueB = "32";   // Example expected value
	    
	    assertEquals(expectedValueA, valueA.getValue().getValue());
	    assertEquals(expectedValueB, valueB.getValue().getValue());

	    // Disconnect the client
	    client.disconnect().get();
	}

	private static void startOpcUaServer() throws Exception {
		opcUaServer = new ExampleServer();
		opcUaServer.startup().get();
	}

	private static void stopOpcUaServer() throws InterruptedException, ExecutionException {
		opcUaServer.shutdown().get();
	}
}