package basyx.components.databridge.examples.activemqjsonatadelegator;

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

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.usage.SystemUsage;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import basyx.components.databridge.camelactivemq.configuration.factory.ActiveMQDefaultConfigurationFactory;
import basyx.components.databridge.core.component.DataBridgeComponent;
import basyx.components.databridge.core.configuration.factory.RoutesConfigurationFactory;
import basyx.components.databridge.core.configuration.route.core.RoutesConfiguration;
import basyx.components.databridge.transformer.cameljsonata.configuration.factory.JsonataDefaultConfigurationFactory;

public class TestAASUpdater {
	private static DataBridgeComponent updater;
	private static Connection connection;
	private static Session session;
	private static Destination destination;

	@BeforeClass
	public static void setUp() throws IOException {
		startActiveMQBroker();

		startDataBridgeComponent();
	}

	@Test
	public void transformedResponseIsReturnedFromEndpointA() throws Exception {
		publishNewDatapoint("first-topic", "test1.json");

		waitForPropagation();

		callApiAndCheckResult("http://localhost:8090/valueA", "858383");
	}
	
	@Test
	public void transformedResponseIsReturnedFromEndpointB() throws Exception {
		publishNewDatapoint("second-topic", "test2.json");

		waitForPropagation();

		callApiAndCheckResult("http://localhost:8091/valueB", "336.36");
	}

	private static void startDataBridgeComponent() {
		updater = new DataBridgeComponent(addConfigurations());
		updater.startComponent();
	}

	private static RoutesConfiguration addConfigurations() {
		RoutesConfiguration configuration = new RoutesConfiguration();

		ClassLoader loader = TestAASUpdater.class.getClassLoader();

		RoutesConfigurationFactory routesFactory = new RoutesConfigurationFactory(loader);
		configuration.addRoutes(routesFactory.create());

		ActiveMQDefaultConfigurationFactory activeMQConfigFactory = new ActiveMQDefaultConfigurationFactory(loader);
		configuration.addDatasources(activeMQConfigFactory.create());

		JsonataDefaultConfigurationFactory jsonataConfigFactory = new JsonataDefaultConfigurationFactory(loader);
		configuration.addTransformers(jsonataConfigFactory.create());

		return configuration;
	}

	private static void callApiAndCheckResult(String endpoint, String expectedValue)
			throws ClientProtocolException, IOException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet request = new HttpGet(endpoint);
		CloseableHttpResponse resp = client.execute(request);
		String content = new String(resp.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

		assertEquals(expectedValue, content);
		client.close();
	}

	private void waitForPropagation() throws InterruptedException {
		Thread.sleep(5000);
	}

	private void publishNewDatapoint(String queueName, String fileName) {
		try {
			configureAndStartConnection();

			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			destination = session.createQueue(queueName);

			sendMessage(fileName);

			closeConnections();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendMessage(String fileName) throws JMSException, IOException {
		MessageProducer producer = session.createProducer(destination);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

		String json = FileUtils.readFileToString(
				new File(getClass().getClassLoader().getResource(fileName).getFile()), StandardCharsets.UTF_8);

		TextMessage message = session.createTextMessage(json);

		producer.send(message);
	}

	private void configureAndStartConnection() throws JMSException {
		connection = new ActiveMQConnectionFactory("tcp://localhost:61616").createConnection();
		connection.start();
	}

	private void closeConnections() throws JMSException {
		session.close();
		connection.close();
	}

	private static void startActiveMQBroker() {
		try {
			BrokerService broker = new BrokerService();
			broker.addConnector("tcp://localhost:61616");
			broker.setPersistent(false);
			SystemUsage systemUsage = broker.getSystemUsage();
			systemUsage.getStoreUsage().setLimit(1024 * 1024 * 8);
			systemUsage.getTempUsage().setLimit(1024 * 1024 * 8);
			broker.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void tearDown() throws JMSException {
		updater.stopComponent();

		session.close();

		connection.close();
	}
}
