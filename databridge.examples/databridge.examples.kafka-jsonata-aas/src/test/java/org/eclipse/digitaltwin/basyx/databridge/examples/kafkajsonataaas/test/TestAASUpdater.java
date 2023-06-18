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
package org.eclipse.digitaltwin.basyx.databridge.examples.kafkajsonataaas.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.curator.test.TestingServer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Time;
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

/**
 * Tests the DataBridge scenario with Kafka
 *
 */
public class TestAASUpdater {

	private static Logger logger = LoggerFactory.getLogger(TestAASUpdater.class);
	private static AASServerComponent aasServer;
	private static DataBridgeComponent updater;
	private static InMemoryRegistry registry = new InMemoryRegistry();

	private static KafkaServer kafkaServer;

	protected static IIdentifier deviceAASId = new CustomId("TestUpdatedDeviceAAS");
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
	public void getPropertyAValue() throws Exception {
		publishNewDatapoint("first-topic");
		
		awaitAndCheckPropertyValue("336.36", "ConnectedPropertyA");
	}
	
	@Test
	public void getPropertyBValue() throws Exception {
		publishNewDatapoint("second-topic");
		
		awaitAndCheckPropertyValue("858383", "ConnectedPropertyB");
	}
	
	private void awaitAndCheckPropertyValue(String expectedValue, String propertyIdShort) {
	    Awaitility.await().with().pollInterval(2, TimeUnit.SECONDS).atMost(14, TimeUnit.SECONDS).untilAsserted(() -> assertEquals(expectedValue, retrievePropertyValue(propertyIdShort)));
	}

	private Object retrievePropertyValue(String propertyIdShort) {
		ConnectedAssetAdministrationShell aas = getAAS(deviceAASId);

		ISubmodelElement updatedProp = getSubmodelElement(aas, "ConnectedSubmodel", propertyIdShort);
		
		return updatedProp.getValue();
	}

	private static void configureAndStartAasServer() {
		aasContextConfig = new BaSyxContextConfiguration(4001, "");
		BaSyxAASServerConfiguration aasConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY,
				"aasx/updatertest.aasx");
		aasServer = new AASServerComponent(aasContextConfig, aasConfig);
		aasServer.setRegistry(registry);

		aasServer.startComponent();
	}

	private static void configureAndStartUpdaterComponent() {
		ClassLoader loader = TestAASUpdater.class.getClassLoader();
		RoutesConfiguration configuration = new RoutesConfiguration();

		RoutesConfigurationFactory routesFactory = new RoutesConfigurationFactory(loader);
		configuration.addRoutes(routesFactory.create());

		KafkaDefaultConfigurationFactory kafkaConfigFactory = new KafkaDefaultConfigurationFactory(loader);
		configuration.addDatasources(kafkaConfigFactory.create());

		AASProducerDefaultConfigurationFactory aasConfigFactory = new AASProducerDefaultConfigurationFactory(loader);
		configuration.addDatasinks(aasConfigFactory.create());

		JsonataDefaultConfigurationFactory jsonataConfigFactory = new JsonataDefaultConfigurationFactory(loader);
		configuration.addTransformers(jsonataConfigFactory.create());

		updater = new DataBridgeComponent(configuration);
		updater.startComponent();
	}

	private void publishNewDatapoint(String topic)
			throws MqttException, MqttSecurityException, MqttPersistenceException, InterruptedException {
		String json = "{\"Account\":{\"Account Name\":\"Firefly\",\"Order\":[{\"OrderID\":\"order103\",\"Product\":[{\"Product Name\":\"Bowler Hat\",\"ProductID\":858383,\"SKU\":\"0406654608\",\"Description\":{\"Colour\":\"Purple\",\"Width\":300,\"Height\":200,\"Depth\":210,\"Weight\":0.75},\"Price\":34.45,\"Quantity\":2},{\"Product Name\":\"Trilby hat\",\"ProductID\":858236,\"SKU\":\"0406634348\",\"Description\":{\"Colour\":\"Orange\",\"Width\":300,\"Height\":200,\"Depth\":210,\"Weight\":0.6},\"Price\":21.67,\"Quantity\":1}]},{\"OrderID\":\"order104\",\"Product\":[{\"Product Name\":\"Bowler Hat\",\"ProductID\":858383,\"SKU\":\"040657863\",\"Description\":{\"Colour\":\"Purple\",\"Width\":300,\"Height\":200,\"Depth\":210,\"Weight\":0.75},\"Price\":34.45,\"Quantity\":4},{\"ProductID\":345664,\"SKU\":\"0406654603\",\"Product Name\":\"Cloak\",\"Description\":{\"Colour\":\"Black\",\"Width\":30,\"Height\":20,\"Depth\":210,\"Weight\":2},\"Price\":107.99,\"Quantity\":1}]}]}}";

		String bootstrapServer = "127.0.0.1:9092";

		Properties properties = createProducerProperties(bootstrapServer);

		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

		ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>(topic, json);

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

	private ISubmodelElement getSubmodelElement(ConnectedAssetAdministrationShell aas, String submodelId,
			String submodelElementId) {
		ISubmodel sm = aas.getSubmodels().get(submodelId);

		return sm.getSubmodelElement(submodelElementId);
	}

	private ConnectedAssetAdministrationShell getAAS(IIdentifier identifier) {
		ConnectedAssetAdministrationShellManager manager = new ConnectedAssetAdministrationShellManager(registry);

		return manager.retrieveAAS(identifier);
	}

	private static void clearLogs() throws IOException {
		Path tempLogDirPath = Paths.get(kafkaTmpLogsDirPath);
		
		if (Files.exists(tempLogDirPath))
			FileUtils.deleteDirectory(new File(kafkaTmpLogsDirPath));
	}

}
