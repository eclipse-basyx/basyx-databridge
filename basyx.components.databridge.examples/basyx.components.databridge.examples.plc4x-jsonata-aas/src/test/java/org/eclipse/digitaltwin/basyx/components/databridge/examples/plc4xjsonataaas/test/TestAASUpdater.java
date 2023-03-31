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
package org.eclipse.digitaltwin.basyx.components.databridge.examples.plc4xjsonataaas.test;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.connected.ConnectedAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.aas.registration.memory.InMemoryRegistry;
import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.digitaltwin.basyx.components.databridge.aas.configuration.factory.AASProducerDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.components.databridge.camelplc4x.configuration.factory.Plc4XDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.components.databridge.core.component.DataBridgeComponent;
import org.eclipse.digitaltwin.basyx.components.databridge.core.configuration.factory.RoutesConfigurationFactory;
import org.eclipse.digitaltwin.basyx.components.databridge.core.configuration.route.core.RoutesConfiguration;
import org.eclipse.digitaltwin.basyx.components.databridge.transformer.cameljsonata.configuration.factory.JsonataDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.components.databridge.transformer.cameljsonjackson.configuration.factory.JsonJacksonDefaultConfigurationFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.net.ModbusTCPListener;
import net.wimpi.modbus.procimg.ProcessImage;
import net.wimpi.modbus.procimg.SimpleInputRegister;
import net.wimpi.modbus.procimg.SimpleProcessImage;
import net.wimpi.modbus.procimg.SimpleRegister;

/**
 * Tests the DataBridge scenario with PLC4X using modbus
 *
 * @author danish
 *
 */
public class TestAASUpdater {
	
	private static final String HOST = "127.0.0.1";
	
	private static AASServerComponent aasServer;
	private static DataBridgeComponent updaterComponent;
	private static InMemoryRegistry registry = new InMemoryRegistry();
	private static ModbusTCPListener modbusTCPListener;

	private static IIdentifier deviceAASPlainId = new CustomId("TestUpdatedDeviceAAS");
	private static BaSyxContextConfiguration aasContextConfig;

	@BeforeClass
	public static void setUp() throws Exception {
		configureModbus();
		
		startModbusListener();

		configureAasServer();
		
		startAasServer();
		
		RoutesConfiguration configuration = configureRoutes(TestAASUpdater.class.getClassLoader());

		startDataBridgeComponent(configuration);
	}
	
	@AfterClass
	public static void tearDown() {
		updaterComponent.stopComponent();
		
		aasServer.stopComponent();
		
		modbusTCPListener.stop();
	}

	@Test
	public void test() throws Exception {
		String expectedValue = wrapStringValue("251");
		
		assertPropertyValue(expectedValue);
	}

	private void assertPropertyValue(String expectedValue) {
		Awaitility.await().with().pollInterval(2, TimeUnit.SECONDS).atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertEquals(expectedValue, fetchPropertyValue()));
	}

	private String fetchPropertyValue() {
		ConnectedAssetAdministrationShell aas = getAAS(deviceAASPlainId);

		ISubmodelElement updatedProp = getSubmodelElement(aas, "ConnectedSubmodel", "ConnectedPropertyA");

		String propValue = (String) updatedProp.getValue();
		return propValue;
	}

	private static void startAasServer() {
		aasServer.startComponent();
	}

	private static void startDataBridgeComponent(RoutesConfiguration configuration) {
		updaterComponent = new DataBridgeComponent(configuration);
		updaterComponent.startComponent();
	}

	private static RoutesConfiguration configureRoutes(ClassLoader loader) {
		RoutesConfiguration configuration = new RoutesConfiguration();

		RoutesConfigurationFactory routesFactory = new RoutesConfigurationFactory(loader);
		configuration.addRoutes(routesFactory.create());
		
		Plc4XDefaultConfigurationFactory plc4xDefaultConfigurationFactory = new Plc4XDefaultConfigurationFactory(
				loader);
		configuration.addDatasources(plc4xDefaultConfigurationFactory.create());
		
		JsonJacksonDefaultConfigurationFactory jsonJacksonConfigFactory = new JsonJacksonDefaultConfigurationFactory(
				loader);
		configuration.addTransformers(jsonJacksonConfigFactory.create());

		JsonataDefaultConfigurationFactory jsonataConfigFactory = new JsonataDefaultConfigurationFactory(loader);
		configuration.addTransformers(jsonataConfigFactory.create());

		AASProducerDefaultConfigurationFactory aasConfigFactory = new AASProducerDefaultConfigurationFactory(loader);
		
		configuration.addDatasinks(aasConfigFactory.create());
		return configuration;
	}

	private ISubmodelElement getSubmodelElement(ConnectedAssetAdministrationShell aas, String submodelId,
			String submodelElementId) {
		ISubmodel sm = aas.getSubmodels().get(submodelId);
		ISubmodelElement updatedProp = sm.getSubmodelElement(submodelElementId);

		return updatedProp;
	}

	private ConnectedAssetAdministrationShell getAAS(IIdentifier identifier) {
		ConnectedAssetAdministrationShellManager manager = new ConnectedAssetAdministrationShellManager(registry);
		ConnectedAssetAdministrationShell aas = manager.retrieveAAS(identifier);
		return aas;
	}
	
	private static void configureAasServer() {
		aasContextConfig = new BaSyxContextConfiguration(4001, "");
		BaSyxAASServerConfiguration aasConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY,
				"aasx/updatertest.aasx");
		aasServer = new AASServerComponent(aasContextConfig, aasConfig);
		aasServer.setRegistry(registry);
	}

	private static void configureModbus() throws UnknownHostException {
		ProcessImage image = createProcessImage();
		
		configureModbusCoupler(image);
	}

	private static void startModbusListener() throws UnknownHostException {
		modbusTCPListener = new ModbusTCPListener(3);
		modbusTCPListener.setAddress(InetAddress.getByName(HOST));
		modbusTCPListener.setPort(50201);
		modbusTCPListener.start();
	}

	private static void configureModbusCoupler(ProcessImage image) {
		ModbusCoupler.getReference().setProcessImage(image);
        ModbusCoupler.getReference().setMaster(false);
        ModbusCoupler.getReference().setUnitID(15);
	}

	private static ProcessImage createProcessImage() {
		SimpleProcessImage image = new SimpleProcessImage();
		image.addRegister(new SimpleRegister(251));
		image.addInputRegister(new SimpleInputRegister(45));
		return image;
	}
	
	private String wrapStringValue(String value) {
		return "\"" + value + "\"";
	}
}
