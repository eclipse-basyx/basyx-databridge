/*******************************************************************************
 * Copyright (C) 2021 the Eclipse BaSyx Authors
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
package org.eclipse.digitaltwin.basyx.databridge.examples.kafkajsonatamultipleaas.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.curator.test.TestingServer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Time;
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
import org.eclipse.digitaltwin.basyx.databridge.aas.configuration.factory.AASProducerDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.component.DataBridgeComponent;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.factory.RoutesConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RoutesConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.jsonata.configuration.factory.JsonataDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.kafka.configuration.factory.KafkaDefaultConfigurationFactory;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import scala.Option;

public class TestAASUpdater {
	private static Logger logger = LoggerFactory.getLogger(TestAASUpdater.class);
	private static AASServerComponent aasServer;
	private static DataBridgeComponent updater;
	private static InMemoryRegistry registry;
	
	private static KafkaServer kafkaServer;
	private static String kafkaTmpLogsDirPath;

	private static TestingServer zookeeper;
	
	protected static IIdentifier deviceAAS = new CustomId("TestUpdatedDeviceAAS");
	private static BaSyxContextConfiguration aasContextConfig;

	@BeforeClass
	public static void setUp() throws Exception {
		configureAndStartKafkaServer();
		configureAndStartAASServer();
		configureAndStartUpdaterComponent();
		
	}

	private static void configureAndStartAASServer() {
		aasContextConfig = new BaSyxContextConfiguration(4001, "");
		BaSyxAASServerConfiguration aasConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY, "aasx/updatertest.aasx");
		aasServer = new AASServerComponent(aasContextConfig, aasConfig);
		aasServer.setRegistry(registry = new InMemoryRegistry());
		aasServer.startComponent();
	}
	
	@AfterClass
	public static void tearDown() throws IOException {
		updater.stopComponent();
		aasServer.stopComponent();
		zookeeper.close();
		aasServer.stopComponent();
		clearLogs();
	}

	@Test
	public void test() throws Exception {
		publishNewDatapoint();
		Thread.sleep(5000);
		checkIfPropertyIsUpdated();
	}

	private static void configureAndStartUpdaterComponent() {
		ClassLoader loader = TestAASUpdater.class.getClassLoader();
		RoutesConfiguration configuration = new RoutesConfiguration();

		addRoutes(loader, configuration);
		addKafkaSource(loader, configuration);
		addDatasinks(loader, configuration);
		addDataTransformers(loader, configuration);

		updater = new DataBridgeComponent(configuration);
		updater.startComponent();
	}
	
	private static void addRoutes(ClassLoader loader, RoutesConfiguration configuration) {
		RoutesConfigurationFactory routesFactory = new RoutesConfigurationFactory(loader);
		configuration.addRoutes(routesFactory.create());
	}
	
	private static void addKafkaSource(ClassLoader loader, RoutesConfiguration configuration) {
		KafkaDefaultConfigurationFactory kafkaConfigFactory = new KafkaDefaultConfigurationFactory(loader);
		configuration.addDatasources(kafkaConfigFactory.create());
	}
	
	private static void addDatasinks(ClassLoader loader, RoutesConfiguration configuration) {
		AASProducerDefaultConfigurationFactory aasConfigFactory = new AASProducerDefaultConfigurationFactory(loader);
		configuration.addDatasinks(aasConfigFactory.create());
	}

	private static void addDataTransformers(ClassLoader loader, RoutesConfiguration configuration) {
		JsonataDefaultConfigurationFactory jsonataConfigFactory = new JsonataDefaultConfigurationFactory(loader);
		configuration.addTransformers(jsonataConfigFactory.create());
	}

	private void checkIfPropertyIsUpdated() throws InterruptedException {
		ConnectedAssetAdministrationShellManager manager = new ConnectedAssetAdministrationShellManager(registry);
		ConnectedAssetAdministrationShell shell = manager.retrieveAAS(deviceAAS);
		ISubmodel submodel = shell.getSubmodels().get("ConnectedSubmodel");
		ISubmodelElement updatedProp = submodel.getSubmodelElement("ConnectedPropertyA");
		Object propValue = updatedProp.getValue();
		System.out.println("UpdatedPROP" + propValue);
		assertEquals("198.56", propValue);

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
	
	private static void createKafkaLogDirectoryIfNotExists(Path kafkaTempDirPath) throws IOException {		
		if (!Files.exists(kafkaTempDirPath))
			Files.createDirectory(kafkaTempDirPath);
	}

	private static void startZookeeper() throws Exception {
		zookeeper = new TestingServer(2181, true);

		logger.info("Zookeeper server started: " + zookeeper.getConnectString());
	}

	private void publishNewDatapoint() throws MqttException, MqttSecurityException, MqttPersistenceException {
		String json = "{\"Account\":{\"Account Name\":\"Firefly\",\"Order\":[{\"OrderID\":\"order103\",\"Product\":[{\"Product Name\":\"Bowler Hat\",\"ProductID\":858383,\"SKU\":\"0406654608\",\"Description\":{\"Colour\":\"Purple\",\"Width\":300,\"Height\":200,\"Depth\":210,\"Weight\":0.75},\"Price\":34.45,\"Quantity\":2},{\"Product Name\":\"Trilby hat\",\"ProductID\":858236,\"SKU\":\"0406634348\",\"Description\":{\"Colour\":\"Orange\",\"Width\":300,\"Height\":200,\"Depth\":210,\"Weight\":0.6},\"Price\":21.67,\"Quantity\":1}]},{\"OrderID\":\"order104\",\"Product\":[{\"Product Name\":\"Bowler Hat\",\"ProductID\":858383,\"SKU\":\"040657863\",\"Description\":{\"Colour\":\"Purple\",\"Width\":300,\"Height\":200,\"Depth\":210,\"Weight\":0.75},\"Price\":34.45,\"Quantity\":4},{\"ProductID\":345664,\"SKU\":\"0406654603\",\"Product Name\":\"Cloak\",\"Description\":{\"Colour\":\"Black\",\"Width\":30,\"Height\":20,\"Depth\":210,\"Weight\":2},\"Price\":107.99,\"Quantity\":1}]}]}}";

		String bootstrapServer = "127.0.0.1:9092";

		Properties properties = createProducerProperties(bootstrapServer);

		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

		ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>("first-topic", json);

		producer.send(producerRecord);
		producer.flush();
		producer.close();
	}

	private Properties createProducerProperties(String bootstrapServer) {
		Properties properties = new Properties();
		properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
		properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		return properties;
	}
	
	private static void clearLogs() throws IOException {
		Path tempLogDirPath = Paths.get(kafkaTmpLogsDirPath);
		
		if (Files.exists(tempLogDirPath))
			FileUtils.deleteDirectory(new File(kafkaTmpLogsDirPath));
	}
}
