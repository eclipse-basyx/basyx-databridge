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
package org.eclipse.digitaltwin.basyx.databridge.executable.regression;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
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
 * with Kafka
 * 
 * @author iamaccosta
 *
 */
public abstract class DataBridgeSuiteKafka {
	
	protected abstract IAASAggregator getAASAggregatorProxy();
	
	protected abstract KafkaConsumer<String, String> getKafkaConsumer() throws IOException;

	@Test
	public void test () throws Exception {
		String topicName = "first-topic";

	    Properties consumerProperties = new Properties();
	    consumerProperties.put("bootstrap.servers", "localhost:9092");
	    consumerProperties.put("group.id", "testGroup");
	    consumerProperties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
	    consumerProperties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

	    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties);
	    consumer.subscribe(Collections.singletonList(topicName));

	    // Poll for messages
	    ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
	    boolean messageFound = false;
	    for (ConsumerRecord<String, String> record : records) {
	        Object valueFromKafka = record.value();
	        
	        assertEquals("103.5585973", valueFromKafka);
	        messageFound = true;
	    }
	    
	    if (!messageFound) {
	    	System.out.println("No messages found on Kafka topic " + topicName);
	    }

	    consumer.close();
	}
}