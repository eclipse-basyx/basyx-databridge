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

package org.eclipse.digitaltwin.basyx.databridge.examples.aas_jsonata_kafka.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.curator.test.TestingServer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.utils.Time;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.aas.registration.memory.InMemoryRegistry;
import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.digitaltwin.basyx.databridge.aas.configuration.factory.AASPollingConsumerDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.component.DataBridgeComponent;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.factory.RoutesConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RoutesConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.jsonata.configuration.factory.JsonataDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.kafka.configuration.factory.KafkaDefaultSinkConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.timer.configuration.factory.TimerDefaultConfigurationFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import scala.Option;

/**
 * Tests the DataBridge scenario with Kafka
 *
 */
public class TestKafkaUpdater {

	private static Logger logger = LoggerFactory.getLogger(TestKafkaUpdater.class);
	private static AASServerComponent aasServer;
	private static DataBridgeComponent updater;
	private static InMemoryRegistry registry = new InMemoryRegistry();

	private static KafkaServer kafkaServer;

	private static BaSyxContextConfiguration aasContextConfig;

	private static TestingServer zookeeper;
	private static String kafkaTmpLogsDirPath;

	@BeforeClass
	public static void setUp() throws Exception {
		configureAndStartKafkaServer();

		configureAndStartAasServer();

		configureAndStartUpdaterComponent();
	}

	@AfterClass
	public static void tearDown() throws IOException {
		updater.stopComponent();
		
		kafkaServer.shutdown();
		kafkaServer.awaitShutdown();
		
	    zookeeper.close();
		
		aasServer.stopComponent();

		clearLogs();
	}

	
	@Test
	public void checkKafkaTopic() throws Exception {		
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
	
	private static void configureAndStartAasServer() {
		aasContextConfig = new BaSyxContextConfiguration(4001, "");
		BaSyxAASServerConfiguration aasConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY,
				"aasx/telemeteryTest.aasx");
		aasServer = new AASServerComponent(aasContextConfig, aasConfig);
		aasServer.setRegistry(registry);

		aasServer.startComponent();
	}

	private static void configureAndStartUpdaterComponent() throws InterruptedException {
		ClassLoader loader = TestKafkaUpdater.class.getClassLoader();
		RoutesConfiguration configuration = new RoutesConfiguration();

		RoutesConfigurationFactory routesFactory = new RoutesConfigurationFactory(loader);
		configuration.addRoutes(routesFactory.create());
		
		TimerDefaultConfigurationFactory timerConfigFactory = new TimerDefaultConfigurationFactory(loader);
		configuration.addDatasources(timerConfigFactory.create());
		
		AASPollingConsumerDefaultConfigurationFactory aasConfigFactory = new AASPollingConsumerDefaultConfigurationFactory(loader);
		configuration.addDatasources(aasConfigFactory.create());

		KafkaDefaultSinkConfigurationFactory kafkaConfigFactory = new KafkaDefaultSinkConfigurationFactory(loader);
		configuration.addDatasinks(kafkaConfigFactory.create());

		JsonataDefaultConfigurationFactory jsonataConfigFactory = new JsonataDefaultConfigurationFactory(loader);
		configuration.addTransformers(jsonataConfigFactory.create());

		updater = new DataBridgeComponent(configuration);
		updater.startComponent();
	}

	private static void configureAndStartKafkaServer() throws Exception {
		startZookeeper();

		startKafkaServer();
	}

	private static void startKafkaServer() throws IOException {
		KafkaConfig kafkaConfig = new KafkaConfig(loadKafkaConfigProperties());
		
		kafkaTmpLogsDirPath = kafkaConfig.getString("log.dirs");
		
		createKafkaLogDirectoryIfNotExists(Paths.get(kafkaTmpLogsDirPath));

		Option<String> threadNamePrefix = Option.apply("kafka-server");

		kafkaServer = new KafkaServer(kafkaConfig, Time.SYSTEM, threadNamePrefix, true);
		kafkaServer.startup();
		
		logger.info("Kafka server started");
	}

	private static void startZookeeper() throws Exception {
		zookeeper = new TestingServer(2181, true);

		logger.info("Zookeeper server started: " + zookeeper.getConnectString());
	}

	private static void createKafkaLogDirectoryIfNotExists(Path kafkaTempDirPath) throws IOException {		
		if (!Files.exists(kafkaTempDirPath))
			Files.createDirectory(kafkaTempDirPath);
	}

	private static Properties loadKafkaConfigProperties() {
		Properties props = new Properties();
		try (FileInputStream configFile = new FileInputStream("src/test/resources/kafkaconfig.properties")) {
			props.load(configFile);
			return props;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to load kafka config from properties file");
		}
	}

	private static void clearLogs() throws IOException {
		Path tempLogDirPath = Paths.get(kafkaTmpLogsDirPath);
		
		if (Files.exists(tempLogDirPath))
			FileUtils.deleteDirectory(new File(kafkaTmpLogsDirPath));
	}

}
