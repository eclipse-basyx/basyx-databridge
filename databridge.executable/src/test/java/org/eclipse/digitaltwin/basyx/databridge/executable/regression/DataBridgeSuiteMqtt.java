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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.eclipse.basyx.aas.aggregator.api.IAASAggregator;
import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.junit.Test;

/**
 * Suite for testing that the DataBridge is setup correctly
 * with Mqtt
 * 
 * @author danish
 *
 */
public abstract class DataBridgeSuiteMqtt {
	protected static IIdentifier deviceAAS = new CustomId("TestUpdatedDeviceAAS");
	
	protected abstract IMqttClient getMqttClient() throws MqttException;
	
	protected abstract IAASAggregator getAASAggregatorProxy();
	
	@Test
	public void test() throws Exception {
		publishNewDatapoint();
		
		waitForPropagation();
		
		checkIfPropertyIsUpdated();
	}

	private void waitForPropagation() throws InterruptedException {
		Thread.sleep(10000);
	}

	private void checkIfPropertyIsUpdated() throws InterruptedException {
		IAASAggregator proxy = getAASAggregatorProxy();
		
		IAssetAdministrationShell aas = proxy.getAAS(deviceAAS);
		ISubmodel sm = aas.getSubmodels().get("ConnectedSubmodel");
		
		ISubmodelElement updatedProp = sm.getSubmodelElement("ConnectedPropertyB");

		Object propValue = updatedProp.getValue();
		
		assertEquals("858383", propValue);
	}

	private void publishNewDatapoint() throws MqttException, MqttSecurityException, MqttPersistenceException, IOException {
		String json = FileUtils.readFileToString(new File(getClass().getClassLoader().getResource("test.json").getFile()), StandardCharsets.UTF_8);
		
		IMqttClient mqttClient = getMqttClient();
		mqttClient.connect();
		mqttClient.publish("PropertyB", new MqttMessage(json.getBytes()));
		mqttClient.disconnect();
		mqttClient.close();
	}
}
