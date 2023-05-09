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
package org.eclipse.digitaltwin.basyx.databridge.executable.regression;

import java.io.IOException;
import java.net.UnknownHostException;
import org.eclipse.basyx.aas.aggregator.api.IAASAggregator;
import org.eclipse.basyx.aas.aggregator.proxy.AASAggregatorProxy;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.digitaltwin.basyx.databridge.component.DataBridgeExecutable;
import org.eclipse.digitaltwin.basyx.databridge.examples.plc4xjsonataaas.Modbus;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import net.wimpi.modbus.procimg.ProcessImage;
import net.wimpi.modbus.procimg.SimpleInputRegister;
import net.wimpi.modbus.procimg.SimpleProcessImage;
import net.wimpi.modbus.procimg.SimpleRegister;

/**
 * Tests the DataBridge with PLC4X
 *
 * @author danish
 */
public class TestDataBridgePlc4X extends DataBridgeSuitePlc4X {
	
	private static final String HOST = "127.0.0.1";
	private static final int PORT = 8095;
	private static final int THREAD_POOL_SIZE = 3;
	private static final String AAS_AGGREGATOR_URL = "http://localhost:4001";
	
	protected static IIdentifier deviceAAS = new CustomId("TestUpdatedDeviceAAS");
	private static AASServerComponent aasServer;
	private static Modbus modbus;
	
	@BeforeClass
	public static void setUp() throws IOException {
		configureAndStartModbus();

		configureAndStartAASServer();
		
		startUpdaterComponent();
	}
	
	@AfterClass
	public static void tearDown() {
		aasServer.stopComponent();
		
		stopDataBridgeComponent();
		
		modbus.stop();
	}
	
	@Override
	protected IAASAggregator getAASAggregatorProxy() {
		return new AASAggregatorProxy(AAS_AGGREGATOR_URL);
	}
	
	private static void configureAndStartModbus() throws UnknownHostException {
		modbus = new Modbus(THREAD_POOL_SIZE, HOST, PORT);
		modbus.configureDefaultModbusCoupler(createProcessImage());
		modbus.start();
	}

	private static ProcessImage createProcessImage() {
		SimpleProcessImage image = new SimpleProcessImage();
		image.addRegister(new SimpleRegister(251));
		image.addInputRegister(new SimpleInputRegister(45));
		return image;
	}
	
	private static void configureAndStartAASServer() {
		BaSyxContextConfiguration aasContextConfig = new BaSyxContextConfiguration(4001, "");
		
		BaSyxAASServerConfiguration aasConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY, "aasx/updatertest.aasx");
		
		aasServer = new AASServerComponent(aasContextConfig, aasConfig);
		aasServer.startComponent();
	}
	
	private static void startUpdaterComponent() {
		DataBridgeExecutable.main(new String[] {"src/test/resources/plc4x/databridge"});
	}
	
	private static void stopDataBridgeComponent() {
		if(DataBridgeExecutable.getDataBridgeComponent() != null) {
			DataBridgeExecutable.getDataBridgeComponent().stopComponent();
		}
	}
}
