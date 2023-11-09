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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import org.eclipse.basyx.aas.aggregator.api.IAASAggregator;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Suite for testing that the DataBridge is setup correctly with AAS Polling Consumer
 * 
 * @author rana
 *
 */
public abstract class DataBridgeSuiteAASPollingConsumer {
	
	protected abstract MqttClient getMqttClient() throws MqttException;
	protected abstract IAASAggregator getAASAggregatorProxy();
	private static Logger logger = LoggerFactory.getLogger(DataBridgeSuiteAASPollingConsumer.class);
	private static String user_name = "test1";
	private static String password = "1234567";
	private static String receivedMessage;
	
	@Test
	public void getUpdatedPropertyValueA() throws MqttException, MqttSecurityException, MqttPersistenceException, InterruptedException, JsonProcessingException {
		
		String topic = "aas/pressure";
		String expectedValueA = wrapStringValue("103.5585973");

		assertPropertyValue(expectedValueA, topic);
	}
	
	@Test
	public void getUpdatedPropertyValueB() throws MqttException, MqttSecurityException, MqttPersistenceException, InterruptedException, IOException, URISyntaxException {
		
		String topic = "aas/rotation";
		String expectedValueB = wrapStringValue("379.5784558");
		
		assertPropertyValue(expectedValueB, topic);
	}
	
	private void assertPropertyValue(String expectedValue, String topic) throws MqttSecurityException, MqttPersistenceException, MqttException, InterruptedException {
		
		fetchExpectedValue(topic);
		
		assertEquals(receivedMessage, expectedValue);
	}

	
	private void fetchExpectedValue(String currentTopic) throws MqttException, MqttSecurityException, MqttPersistenceException, InterruptedException {
		
		try {

			MqttClient mqttClient = mqttConnectionInitiate();
			
			mqttClient.setCallback(new MqttCallback() {

				@Override
				public void connectionLost(Throwable cause) {
					logger.info("Connection Lost : "+cause.getMessage());
				}

				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					
					receivedMessage = new String(message.getPayload(), StandardCharsets.UTF_8);
				}

				@Override
				public void deliveryComplete(IMqttDeliveryToken token) {
					
				}
			});
			
			mqttClient.subscribe(currentTopic);
			waitForPropagation();
			mqttClient.disconnect();
			mqttClient.close();

		} catch (MqttException me) {
            me.printStackTrace();
		}
	}
	
	private MqttClient mqttConnectionInitiate() throws MqttException {
		
		MqttClient mqttClient = getMqttClient();
		
		MqttConnectOptions connOpts = setUpMqttConnection(user_name, password);
		connOpts.setCleanSession(true);
		mqttClient.connect(connOpts);
		return mqttClient;
	}
	
	private static MqttConnectOptions setUpMqttConnection(String username, String password) {
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setUserName(username);
		connOpts.setPassword(password.toCharArray());
		return connOpts;
	}
	
	private static void waitForPropagation() throws InterruptedException {
		Thread.sleep(6000);
	}
	
	private String wrapStringValue(String value) {
		return "\"" + value + "\"";
	}
}
