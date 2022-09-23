/*******************************************************************************
* Copyright (C) 2022 the Eclipse BaSyx Authors
* 
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/

* 
* SPDX-License-Identifier: EPL-2.0
******************************************************************************/

package basyx.components.databridge.examples.aasjsonatamqtt.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.eclipse.basyx.aas.registration.memory.InMemoryRegistry;
import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import basyx.components.databridge.aas.configuration.factory.AASDatasourceDefaultConfigurationFactory;
import basyx.components.databridge.camelpaho.configuration.factory.MqttDatasinkDefaultConfigurationFactory;
import basyx.components.databridge.core.component.UpdaterComponent;
import basyx.components.databridge.core.configuration.factory.RoutesConfigurationFactory;
import basyx.components.databridge.core.configuration.route.core.RoutesConfiguration;
import basyx.components.databridge.transformer.cameljsonata.configuration.factory.JsonataDefaultConfigurationFactory;
import io.moquette.broker.Server;
import io.moquette.broker.config.ClasspathResourceLoader;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.IResourceLoader;
import io.moquette.broker.config.ResourceLoaderConfig;

/**
 * Tests an event-triggered connection between an AAS and a MQTT Broker
 * 
 * @author mateusmolina-iese
 *
 */
public class TestMQTTUpdater {
	private static ClassLoader loader = TestMQTTUpdater.class.getClassLoader();
	private static AASServerComponent aasServer;
	private static UpdaterComponent updater;
	private static InMemoryRegistry registry;
	protected static Server mqttBroker;
	protected static MqttAsyncClient mqttClient;
	private static final String topic = "Properties";

	private static final String expectedPropValue = "8";
	private static final String expectedSMJson = "{\"idShort\":\"ConnectedSubmodel\",\"identification\":{\"idType\":\"IRI\",\"id\":\"https://example.com/ids/sm/5280_2110_0112_9176\"},\"dataSpecification\":[],\"embeddedDataSpecifications\":[],\"modelType\":{\"name\":\"Submodel\"},\"kind\":\"Instance\",\"submodelElements\":[{\"modelType\":{\"name\":\"Property\"},\"kind\":\"Instance\",\"value\":8,\"idShort\":\"ConnectedPropertyA\",\"category\":\"VARIABLE\",\"qualifiers\":[{\"modelType\":{\"name\":\"Qualifier\"},\"type\":\"BaSyxMQTTBrokerEndpoint\",\"value\":\"tcp://localhost:1884\",\"valueType\":\"string\"},{\"modelType\":{\"name\":\"Qualifier\"},\"type\":\"BaSyxDataformat\",\"value\":\"Direct\",\"valueType\":\"string\"},{\"modelType\":{\"name\":\"Qualifier\"},\"type\":\"BaSyxJSONataSelector\",\"value\":\"$sum(Account.Order.Product.(Price * Quantity))\",\"valueType\":\"string\"},{\"modelType\":{\"name\":\"Qualifier\"},\"type\":\"BaSyxMQTTTopic\",\"value\":\"Properties\",\"valueType\":\"string\"},{\"modelType\":{\"name\":\"Qualifier\"},\"type\":\"BaSyxDatasourceType\",\"value\":\"MQTT\",\"valueType\":\"string\"}],\"semanticId\":{\"keys\":[]},\"valueType\":\"integer\",\"parent\":{\"keys\":[{\"type\":\"Submodel\",\"local\":true,\"value\":\"https://example.com/ids/sm/5280_2110_0112_9176\",\"idType\":\"IRI\"}]}},{\"modelType\":{\"name\":\"Property\"},\"kind\":\"Instance\",\"value\":\"\",\"idShort\":\"ConnectedPropertyB\",\"category\":\"VARIABLE\",\"qualifiers\":[{\"modelType\":{\"name\":\"Qualifier\"},\"type\":\"BaSyxMQTTBrokerEndpoint\",\"value\":\"tcp://localhost:1884\",\"valueType\":\"string\"},{\"modelType\":{\"name\":\"Qualifier\"},\"type\":\"BaSyxDataformat\",\"value\":\"DIRECT\",\"valueType\":\"string\"},{\"modelType\":{\"name\":\"Qualifier\"},\"type\":\"BaSyxMQTTTopic\",\"value\":\"PropertyB\",\"valueType\":\"string\"},{\"modelType\":{\"name\":\"Qualifier\"},\"type\":\"BaSyxDatasourceType\",\"value\":\"MQTT\",\"valueType\":\"string\"}],\"semanticId\":{\"keys\":[]},\"valueType\":\"anySimpleType\",\"parent\":{\"keys\":[{\"type\":\"Submodel\",\"local\":true,\"value\":\"https://example.com/ids/sm/5280_2110_0112_9176\",\"idType\":\"IRI\"}]}}],\"semanticId\":{\"keys\":[]},\"qualifiers\":[]}";
	private static final String expectedAASJson = "{\"modelType\":{\"name\":\"AssetAdministrationShell\"},\"idShort\":\"DeviceAAS\",\"identification\":{\"idType\":\"Custom\",\"id\":\"TestUpdatedDeviceAAS\"},\"dataSpecification\":[],\"embeddedDataSpecifications\":[],\"submodels\":[{\"keys\":[{\"type\":\"Submodel\",\"local\":true,\"value\":\"https://example.com/ids/sm/5280_2110_0112_9176\",\"idType\":\"IRI\"}]}],\"asset\":{\"modelType\":{\"name\":\"Asset\"},\"dataSpecification\":[],\"embeddedDataSpecifications\":[],\"idShort\":\"\",\"identification\":{\"idType\":\"IRDI\",\"id\":\"\"},\"kind\":\"Instance\"},\"views\":[],\"conceptDictionary\":[],\"assetRef\":{\"keys\":[{\"type\":\"Asset\",\"local\":true,\"value\":\"https://example.com/ids/asset/1180_2110_0112_4751\",\"idType\":\"IRI\"}]}}";

	public static String msgReceived = null;
	
	@BeforeClass
	public static void setUp() throws IOException, MqttException {
		startMqttBroker();
		startAasServer();
		System.out.println("STARTING AAS SERVER");
		aasServer.startComponent();
		System.out.println("STARTING MQTT CLIENT");
		startMqttClient();
	}

	@AfterClass
	public static void stopAll() throws MqttException {
		System.out.println("STOPPING COMPONENTS");
		aasServer.stopComponent();
		mqttClient.disconnect();
		mqttClient.close();
		mqttBroker.stopServer();
	}

	/**
	 * 
	 * Tests if a property value is correctly published to a MQTT Broker
	 * 
	 * 
	 * @throws Exception
	 */
	@Test()
	public void testPublishPropertyValue() throws Exception {
		startUpdater("aasserver_datasource_prop.json");
		System.out.println("STARTING UPDATER (Property)");
		updater.startComponent();
		waitPropagation();
		assertEquals(expectedPropValue, msgReceived);
		updater.stopComponent();
	}

	/**
	 * 
	 * Tests if a JSON containing Submodel related data is correctly published to a
	 * MQTT Broker
	 * 
	 * 
	 * @throws Exception
	 */
	@Test()
	public void testPublishSubmodel() throws Exception {
		startUpdater("aasserver_datasource_sm.json");
		System.out.println("STARTING UPDATER (SM)");
		updater.startComponent();
		waitPropagation();
		assertEquals(expectedSMJson, msgReceived);
		updater.stopComponent();
	}

	/**
	 * 
	 * Tests if a JSON containing AAS related data is correctly published to a MQTT
	 * Broker
	 * 
	 * 
	 * @throws Exception
	 */
	@Test()
	public void testPublishAAS() throws Exception {
		startUpdater("aasserver_datasource_aas.json");
		System.out.println("STARTING UPDATER (AAS)");
		updater.startComponent();
		waitPropagation();
		assertEquals(expectedAASJson, msgReceived);
		updater.stopComponent();
	}

	private static void waitPropagation() throws InterruptedException {
		Thread.sleep(100);
	}

	private static void startUpdater(String aasserver_datasource) {
		RoutesConfiguration configuration = new RoutesConfiguration();

		// Extend configutation for connections
		RoutesConfigurationFactory routesFactory = new RoutesConfigurationFactory(loader);
		configuration.addRoutes(routesFactory.create());

		// Extend configutation for AAS data Source
		AASDatasourceDefaultConfigurationFactory aasConfigFactory = new AASDatasourceDefaultConfigurationFactory(aasserver_datasource, loader);
		configuration.addDatasources(aasConfigFactory.create());

		// Extend configuration for MQTT data sink
		MqttDatasinkDefaultConfigurationFactory mqttConfigFactory = new MqttDatasinkDefaultConfigurationFactory(loader);
		configuration.addDatasinks(mqttConfigFactory.create());

		// Extend configuration for Jsonata
		JsonataDefaultConfigurationFactory jsonataConfigFactory = new JsonataDefaultConfigurationFactory(loader);
		configuration.addTransformers(jsonataConfigFactory.create());

		updater = new UpdaterComponent(configuration);
	}

	private static void startMqttBroker() throws IOException, MqttException {
		mqttBroker = new Server();
		IResourceLoader classpathLoader = new ClasspathResourceLoader();
		final IConfig classPathConfig = new ResourceLoaderConfig(classpathLoader);
		mqttBroker.startServer(classPathConfig);

	}

	private static void startMqttClient() throws MqttException {
		mqttClient = new MqttAsyncClient("tcp://localhost:1884", MqttClient.generateClientId(), new MemoryPersistence());
		IMqttMessageListener listener = new IMqttMessageListener() {
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				msgReceived = message.toString();

			}
		};
		IMqttToken token = mqttClient.connect();
		token.waitForCompletion();
		mqttClient.subscribe(topic, 0, listener);
	}

	private static void startAasServer() {
		registry = new InMemoryRegistry();
		BaSyxContextConfiguration aasContextConfig = new BaSyxContextConfiguration(4001, "");
		BaSyxAASServerConfiguration aasConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY, "aasx/updatertest.aasx");
		aasServer = new AASServerComponent(aasContextConfig, aasConfig);
		aasServer.setRegistry(registry);
	}
}
