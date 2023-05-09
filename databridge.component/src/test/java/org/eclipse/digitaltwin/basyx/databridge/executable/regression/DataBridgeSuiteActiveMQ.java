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

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.io.FileUtils;
import org.eclipse.basyx.aas.aggregator.api.IAASAggregator;
import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.junit.Test;

/**
 * Suite for testing that the DataBridge is setup correctly
 * with ActiveMQ
 * 
 * @author danish
 *
 */
public abstract class DataBridgeSuiteActiveMQ {
	private static Connection connection;
	private static Session session;
	private static Destination destination;

	protected static IIdentifier deviceAAS = new CustomId("TestUpdatedDeviceAAS");

	protected abstract Connection getActiveMQConnection() throws JMSException;

	protected abstract IAASAggregator getAASAggregatorProxy();

	@Test
	public void datapointIsPropagated() throws Exception {
		publishNewDatapoint("first-topic");

		publishNewDatapoint("second-topic");

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

		isPropertyValueUpdated(sm, "ConnectedPropertyA", "336.36");

		isPropertyValueUpdated(sm, "ConnectedPropertyB", "858383");
	}

	private void isPropertyValueUpdated(ISubmodel sm, String id, String actualValue) {
		ISubmodelElement updatedProp = sm.getSubmodelElement(id);

		Object propValue = updatedProp.getValue();
		
		assertEquals(actualValue, propValue);
	}

	private void publishNewDatapoint(String queueName) {
		try {
			configureAndStartConnection();
			
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
			destination = session.createQueue(queueName);
			
			sendMessage();
			
			closeConnections();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendMessage() throws JMSException, IOException {
		MessageProducer producer = session.createProducer(destination);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

		String json = FileUtils.readFileToString(new File(getClass().getClassLoader().getResource("test.json").getFile()), StandardCharsets.UTF_8);
		
		TextMessage message = session.createTextMessage(json);
		
		producer.send(message);
	}

	private void configureAndStartConnection() throws JMSException {
		connection = getActiveMQConnection();
		connection.start();
	}
	
	private void closeConnections() throws JMSException {
		session.close();
		connection.close();
	}
}
