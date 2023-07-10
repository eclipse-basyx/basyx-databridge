package org.eclipse.digitaltwin.basyx.databridge.examples.aasjsonatamqtt.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

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
import org.eclipse.digitaltwin.basyx.databridge.aas.configuration.factory.AASDatasourceDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.component.DataBridgeComponent;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.factory.RoutesConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RoutesConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.jsonata.configuration.factory.JsonataDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.paho.configuration.factory.MqttDatasinkDefaultConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.timer.configuration.factory.TimerDefaultConfigurationFactory;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.moquette.broker.Server;
import io.moquette.broker.config.ClasspathResourceLoader;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.IResourceLoader;
import io.moquette.broker.config.ResourceLoaderConfig;

public class TestAASUpdater {

	private static Logger logger = LoggerFactory.getLogger(TestAASUpdater.class);

	private static AASServerComponent aasServer;
	protected static IIdentifier deviceAASPlainId = new CustomId("TestUpdatedDeviceAAS");

	private static BaSyxContextConfiguration aasContextConfig;
	private static InMemoryRegistry registry = new InMemoryRegistry();
	
	protected static Server mqttBroker;
	private static DataBridgeComponent updater;
	private static String mqtt_broker_url = "tcp://broker.mqttdashboard.com:1883";
	private static String user_name = "test1";
	private static String password = "1234567";
	private static String client_id = UUID.randomUUID().toString();;
	
	@BeforeClass
	public static void setUp() throws Exception {

		configureAndStartMqttBroker();
		
		configureAndStartAasServer();

		configureAndStartUpdaterComponent();
		
	}
	
	@AfterClass
	public static void tearDown() {
		updater.stopComponent();
		aasServer.stopComponent();
	}
	
	private static void configureAndStartAasServer() throws InterruptedException {
		aasContextConfig = new BaSyxContextConfiguration(4001, "");
		BaSyxAASServerConfiguration aasConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY, "aasx/telemeteryTest.aasx");
		aasServer = new AASServerComponent(aasContextConfig, aasConfig);
		aasServer.setRegistry(registry);
		
		aasServer.startComponent();
	}

	public static void configureAndStartUpdaterComponent() throws Exception {

		ClassLoader loader = TestAASUpdater.class.getClassLoader();
		RoutesConfiguration configuration = new RoutesConfiguration();

		RoutesConfigurationFactory routesFactory = new RoutesConfigurationFactory(loader);
		configuration.addRoutes(routesFactory.create());

		TimerDefaultConfigurationFactory timerConfigFactory = new TimerDefaultConfigurationFactory(loader);
		configuration.addDatasources(timerConfigFactory.create());

		AASDatasourceDefaultConfigurationFactory aasSourceConfigFactory = new AASDatasourceDefaultConfigurationFactory(
				loader);
		configuration.addDatasources(aasSourceConfigFactory.create());

		MqttDatasinkDefaultConfigurationFactory mqttConfigFactory = new MqttDatasinkDefaultConfigurationFactory(loader);
		configuration.addDatasinks(mqttConfigFactory.create());

		JsonataDefaultConfigurationFactory jsonataConfigFactory = new JsonataDefaultConfigurationFactory(loader);
		configuration.addTransformers(jsonataConfigFactory.create());

		updater = new DataBridgeComponent(configuration);
		updater.startComponent();
	}

	private static void waitForPropagation() throws InterruptedException {
		Thread.sleep(3000);
	}
	
	@Test
	public void checkIfPropertyIsSent() throws MqttException, MqttSecurityException, MqttPersistenceException, InterruptedException {

		String idShortA = "pressure";
		String idShortB = "rotation";
		String topicA = "aas/pressure";
		String topicB = "aas/rotation";
		
		// Checking idShortA
		Object propValueA = checkAASsubmodelElementsProperty(idShortA);
		receiveNewData(topicA, propValueA, "test-1");
		
		waitForPropagation();
			
		// Checking idShortB 
		Object propValueB = checkAASsubmodelElementsProperty(idShortB);
		receiveNewData(topicB, propValueB, "test-1");
		
		
		// Checking idShortB 
		//Object subModelA = checkAASsubmodelElements();
		//receiveNewData(topicA, subModelA, "test-2");
		
		// Checking idShortB 
		//Object subModelB = checkAASsubmodelElements();
		//receiveNewData(topicB, subModelB, "test-2");
		
	}
	
	private Object checkAASsubmodelElements() throws JsonProcessingException {
		
		StringBuilder propertyString = new StringBuilder();
		
		/*ConnectedAssetAdministrationShell aas = getAAS(deviceAASPlainId);
		ConnectedSubmodel sm = (ConnectedSubmodel) aas.getSubmodel(new Identifier(IdentifierType.IRI,"https://example.com/ids/sm/8583_3140_7032_9766"));
		
		Map<String, ISubmodelElement> subModelCollection = sm.getSubmodelElements();
		
		if(subModelCollection.size()>0) {
			subModelCollection.forEach((k,v) -> propertyString.append(v));
		}*/
		
		
		return (Object) propertyString;
	}
	
	
	private Object checkAASsubmodelElementsProperty(String idShort) throws InterruptedException {
		
		ConnectedAssetAdministrationShell aas = getAAS(deviceAASPlainId);
		ISubmodelElement updatedProp = getSubmodelElement(aas, "telemetryDataStructureTest", idShort);
		
		Object propValue = updatedProp.getValue();
		return propValue;
	}
	
	private static void receiveNewData(String topic, Object propValue, String testType) throws MqttException, MqttSecurityException, MqttPersistenceException, InterruptedException {

		try {
			
			MqttClient mqttClient = new MqttClient(mqtt_broker_url, client_id, new MemoryPersistence());
			MqttConnectOptions connOpts = setUpMqttConnection(user_name, password);
			mqttClient.connect(connOpts);

			mqttClient.setCallback(new MqttCallback() {

				@Override
				public void connectionLost(Throwable cause) {
					logger.info("Connection Lost : "+cause.getMessage());
				}

				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					if(testType.equals("test-1")) {
						String receivedMessage = new String(message.getPayload(), StandardCharsets.UTF_8);
						receivedMessage = receivedMessage.substring(1, receivedMessage.length() - 1);
						System.out.println(propValue+" : "+receivedMessage);
						assertEquals((String)propValue, receivedMessage);
					}else {
						
						String receivedMessage = new String(message.getPayload(), StandardCharsets.UTF_8);
						System.out.println("Got : "+receivedMessage);
					
						System.out.println("Prop : " +propValue);
					}
					
				}

				@Override
				public void deliveryComplete(IMqttDeliveryToken token) {
					
				}

				
			});

			mqttClient.subscribe(topic);
			waitForPropagation();
			mqttClient.disconnect();
			mqttClient.close();

		} catch (MqttException me) {
            me.printStackTrace();
		}
	}
	
	private static void configureAndStartMqttBroker() throws IOException {
		mqttBroker = new Server();
		IResourceLoader classpathLoader = new ClasspathResourceLoader();
		final IConfig classPathConfig = new ResourceLoaderConfig(classpathLoader);
		mqttBroker.startServer(classPathConfig);
	}

	public static MqttConnectOptions setUpMqttConnection(String username, String password) {
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(true);
		connOpts.setUserName(username);
		connOpts.setPassword(password.toCharArray());
		return connOpts;
	}

	private ISubmodelElement getSubmodelElement(ConnectedAssetAdministrationShell aas, String submodelId, String submodelElementId) {
		ISubmodel sm = aas.getSubmodels().get(submodelId);
		ISubmodelElement updatedProp = sm.getSubmodelElement(submodelElementId);
		
		return updatedProp;
	}

	private ConnectedAssetAdministrationShell getAAS(IIdentifier identifier) {
		ConnectedAssetAdministrationShellManager manager = new ConnectedAssetAdministrationShellManager(registry);
		ConnectedAssetAdministrationShell aas = manager.retrieveAAS(identifier);
		return aas;
	}
}
