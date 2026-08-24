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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.curator.test.TestingServer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.utils.Time;
import org.eclipse.basyx.aas.aggregator.api.IAASAggregator;
import org.eclipse.basyx.aas.aggregator.proxy.AASAggregatorProxy;
import org.eclipse.basyx.aas.registration.memory.InMemoryRegistry;
import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.component.DataBridgeExecutable;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import scala.Option;

/**
 * Tests the DataBridge with Kafka
 *
 * @author iamaccosta
 */
public class TestDataBridgeKafka extends DataBridgeSuiteKafka {
	private static AASServerComponent aasServer;
	private static InMemoryRegistry registry = new InMemoryRegistry();
	private static final String AAS_AGGREGATOR_URL = "http://localhost:4001";
	
	private static KafkaServer kafkaServer;
	private static TestingServer zookeeper;
	private static Logger logger = LoggerFactory.getLogger(TestDataBridgeKafka.class);
	private static String kafkaTmpLogsDirPath;
	
	@BeforeClass
	public static void setup() throws Exception {
		configureAndStartAasServer();

		configureAndStartKafkaServer();
		
		configureAndStartUpdaterComponent();
	}
	
	private static void configureAndStartAasServer() throws IOException {
		BaSyxContextConfiguration aasContextConfig = new BaSyxContextConfiguration(4001, "");
		BaSyxAASServerConfiguration aasConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY,
				"aasx/telemeteryTest.aasx");
		aasServer = new AASServerComponent(aasContextConfig, aasConfig);
		aasServer.setRegistry(registry);

		aasServer.startComponent();
	}
	
	private static void configureAndStartKafkaServer() throws Exception {
		startZookeeper();

		startKafkaServer();
	}
	
	private static void startZookeeper() throws Exception {
		zookeeper = new TestingServer(2181, true);

		logger.info("Zookeeper server started: " + zookeeper.getConnectString());
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
	
	private static void configureAndStartUpdaterComponent() {
		DataBridgeExecutable.main(new String[] { "src/test/resources/kafka/databridge" });
	}
	
	@Override
	protected IAASAggregator getAASAggregatorProxy() {
		return new AASAggregatorProxy(AAS_AGGREGATOR_URL);
	}
	
	@Override
	protected KafkaConsumer<String, String> getKafkaConsumer() throws IOException {
        Properties consumerProperties = new Properties();
        consumerProperties.put("bootstrap.servers", "localhost:9092");
        consumerProperties.put("group.id", "testGroup");
        consumerProperties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProperties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        return new KafkaConsumer<>(consumerProperties);
	}
	
	@AfterClass
	public static void tearDown() throws IOException {
		aasServer.stopComponent();

		kafkaServer.shutdown();
		zookeeper.stop();
	}
}