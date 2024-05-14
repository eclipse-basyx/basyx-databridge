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

package org.eclipse.digitaltwin.basyx.databridge.examples.aasopcua.test;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.ExecutionException;

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
import org.eclipse.digitaltwin.basyx.databridge.opcua.configuration.factory.OpcuaDefaultSinkConfigurationFactory;
import org.eclipse.milo.examples.server.ExampleServer;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestOPCUAUpdater {

	private static AASServerComponent aasServer;
	private static BaSyxContextConfiguration aasContextConfig;
	private static InMemoryRegistry registry;
	private static DataBridgeComponent updater;
	protected static ExampleServer opcUaServer;

	@BeforeClass
	public static void setUp() throws Exception {
		System.out.println("Setting up env...");
		startOpcUaServer();
		registry = new InMemoryRegistry();

		aasContextConfig = new BaSyxContextConfiguration(4001, "");
		BaSyxAASServerConfiguration aasConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY, "aasx/telemeteryTest.aasx");
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
	public void testOpcUaDataSink() throws InterruptedException, ExecutionException, UaException {
		aasServer.startComponent();
		System.out.println("AAS Server started");
		System.out.println("Start Updater");
		ClassLoader loader = this.getClass().getClassLoader();
		RoutesConfiguration configuration = new RoutesConfiguration();

		// Extend configuration for connections
		RoutesConfigurationFactory routesFactory = new RoutesConfigurationFactory(loader);
		configuration.addRoutes(routesFactory.create());

		// Extend configuration for AAS Source
		AASPollingConsumerDefaultConfigurationFactory aasSourceConfigFactory = new AASPollingConsumerDefaultConfigurationFactory(loader);
		configuration.addDatasources(aasSourceConfigFactory.create());

		// Extend configuration for OPC UA Sink
		OpcuaDefaultSinkConfigurationFactory opcuaConfigFactory = new OpcuaDefaultSinkConfigurationFactory(loader);
		configuration.addDatasinks(opcuaConfigFactory.create());

		// Extend configuration for Jsonata
		JsonataDefaultConfigurationFactory jsonataConfigFactory = new JsonataDefaultConfigurationFactory(loader);
		configuration.addTransformers(jsonataConfigFactory.create());

		updater = new DataBridgeComponent(configuration);
		updater.startComponent();
		System.out.println("Updater started");

		// Wait for the updater to finish
		Thread.sleep(3000);

		System.out.println("Check OPC UA Nodes");
		checkOpcUaNodes();
	}

	private void checkOpcUaNodes() throws UaException, InterruptedException, ExecutionException {
		// Create an instance of OpcUaClient and connect to the server
		String serverUrl = "127.0.0.1";
		int serverPort = 12686;
		String pathToService = "milo";
		String endpointUrl = "opc.tcp://" + serverUrl + ":" + serverPort + "/" + pathToService;
		OpcUaClient client = createClientConnection(endpointUrl);
		
		// read the values of the two updated nodes
		NodeId nodeId1 = new NodeId(2, "HelloWorld/ScalarTypes/Double");
		NodeId nodeId2 = new NodeId(2, "HelloWorld/ScalarTypes/Integer");
		String value1 = client.readValue(0.0, TimestampsToReturn.Both, nodeId1).get().getValue().getValue().toString();
		String value2 = client.readValue(0.0, TimestampsToReturn.Both, nodeId2).get().getValue().getValue().toString();
		System.out.println("Value of node " + nodeId1 + ": " + value1);
		System.out.println("Value of node " + nodeId2 + ": " + value2);

		// assert that the values are equal to the values that were sent to the AAS
		assertEquals("103.5585973", value1);
		assertEquals("379.5784558", value2);

		// close the client
		client.disconnect().get();
	}

	private static OpcUaClient createClientConnection(String endpointUrl) throws UaException {
		try {
			List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints(endpointUrl).get();

			IdentityProvider identityProvider = new AnonymousProvider();

			UaException connectionException = null;

			for (EndpointDescription endpoint : endpoints) {
				try {
					OpcUaClientConfig config = OpcUaClientConfig.builder().setEndpoint(endpoint).setIdentityProvider(identityProvider).setRequestTimeout(UInteger.valueOf(5000)).build();

					OpcUaClient client = OpcUaClient.create(config);
					client.connect().get();
					return client; // Successful connection
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw e;
				} catch (ExecutionException e) {
					connectionException = new UaException(StatusCodes.Bad_CommunicationError, "Error connecting to endpoint: " + endpoint.getEndpointUrl(), e.getCause());
				}
			}

			// If all endpoints failed, throw an exception
			throw connectionException != null ? connectionException : new UaException(StatusCodes.Bad_ConfigurationError, "No endpoints could be connected to.");

		} catch (InterruptedException | ExecutionException e) {
			throw new UaException(StatusCodes.Bad_InternalError, "Error obtaining server endpoints.", e);
		}
	}

	private static void startOpcUaServer() throws Exception {
		opcUaServer = new ExampleServer();
		opcUaServer.startup().get();
	}

	private static void stopOpcUaServer() throws InterruptedException, ExecutionException {
		opcUaServer.shutdown().get();
	}

}
