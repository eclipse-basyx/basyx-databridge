package basyx.components.databridge.examples.opcuajsonatajacksondelegator;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.milo.examples.server.ExampleServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import basyx.components.databridge.camelopcua.configuration.factory.OpcuaDefaultConfigurationFactory;
import basyx.components.databridge.core.component.DataBridgeComponent;
import basyx.components.databridge.core.configuration.factory.RoutesConfigurationFactory;
import basyx.components.databridge.core.configuration.route.core.RoutesConfiguration;
import basyx.components.databridge.transformer.cameljsonata.configuration.factory.JsonataDefaultConfigurationFactory;
import basyx.components.databridge.transformer.cameljsonjackson.configuration.factory.JsonJacksonDefaultConfigurationFactory;

public class TestAASUpdater {
	private static DataBridgeComponent updater;
	protected static ExampleServer opcUaServer;

	@BeforeClass
	public static void setUp() throws Exception {
		startOpcUaServer();

		startDataBridgeComponent();
	}

	@Test
	public void test() throws Exception {
		waitForPropagation();
		
		callApiAndCheckResult("http://localhost:8090/valueA", "3.14");
		
		callApiAndCheckResult("http://localhost:8091/valueA", "32");
	}
	
	private void waitForPropagation() throws InterruptedException {
		Thread.sleep(5000);
	}

	private static RoutesConfiguration addConfigurations() {
		RoutesConfiguration configuration = new RoutesConfiguration();

		ClassLoader loader = TestAASUpdater.class.getClassLoader();

		RoutesConfigurationFactory routesFactory = new RoutesConfigurationFactory(loader);
		configuration.addRoutes(routesFactory.create());

		OpcuaDefaultConfigurationFactory opcuaConfigFactory = new OpcuaDefaultConfigurationFactory(loader);
		configuration.addDatasources(opcuaConfigFactory.create());

		JsonJacksonDefaultConfigurationFactory jsonJacksonConfigFactory = new JsonJacksonDefaultConfigurationFactory(
				loader);
		configuration.addTransformers(jsonJacksonConfigFactory.create());

		JsonataDefaultConfigurationFactory jsonataConfigFactory = new JsonataDefaultConfigurationFactory(loader);
		configuration.addTransformers(jsonataConfigFactory.create());

		return configuration;
	}
	
	private static void startDataBridgeComponent() {
		updater = new DataBridgeComponent(addConfigurations());
		updater.startComponent();
	}

	private static void callApiAndCheckResult(String endpoint, String expectedValue) throws ClientProtocolException, IOException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet request = new HttpGet(endpoint);
		CloseableHttpResponse resp = client.execute(request);
		String content = new String(resp.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

		assertEquals(expectedValue, content);
		client.close();
	}

	private static void startOpcUaServer() throws Exception {
		opcUaServer = new ExampleServer();
		opcUaServer.startup().get();
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		updater.stopComponent();
		
		opcUaServer.shutdown().get();
	}
}
