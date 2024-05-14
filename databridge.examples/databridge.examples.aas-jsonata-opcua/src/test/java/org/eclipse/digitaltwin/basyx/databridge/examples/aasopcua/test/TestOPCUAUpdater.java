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

import java.util.concurrent.ExecutionException;

import org.eclipse.basyx.aas.registration.memory.InMemoryRegistry;
import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.core.component.DataBridgeComponent;
import org.eclipse.milo.examples.server.ExampleServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;

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
		BaSyxAASServerConfiguration aasConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY, "aasx/telemetryTest.aasx");
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

	private static void startOpcUaServer() throws Exception {
		opcUaServer = new ExampleServer();
		opcUaServer.startup().get();
	}

	private static void stopOpcUaServer() throws InterruptedException, ExecutionException {
		opcUaServer.shutdown().get();
	}

}
