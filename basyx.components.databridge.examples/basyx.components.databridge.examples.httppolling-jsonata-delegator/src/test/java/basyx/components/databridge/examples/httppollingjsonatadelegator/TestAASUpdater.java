package basyx.components.databridge.examples.httppollingjsonatadelegator;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.HttpStatusCode;

import basyx.components.databridge.camelhttppolling.configuration.factory.HttpPollingDefaultConfigurationFactory;
import basyx.components.databridge.core.component.DataBridgeComponent;
import basyx.components.databridge.core.configuration.factory.RoutesConfigurationFactory;
import basyx.components.databridge.core.configuration.route.core.RoutesConfiguration;
import basyx.components.databridge.transformer.cameljsonata.configuration.factory.JsonataDefaultConfigurationFactory;

public class TestAASUpdater {
	private static DataBridgeComponent updater;

	@BeforeClass
	public static void setUp() throws IOException {
		startHTTPServer();

		startDataBridgeComponent();
	}
	
	@Test
	public void transformedResponseIsReturned() throws ClientProtocolException, IOException {
		callApiAndCheckResult();
	}
	
	private static void startHTTPServer() {
		ClientAndServer clientServer = ClientAndServer.startClientAndServer(2018);
		
		clientServer.when(HttpRequest.request().withMethod("GET"))
				.respond(HttpResponse.response().withStatusCode(HttpStatusCode.OK_200.code()).withBody(
						"{\"objects\": \n" + "      [\n" + "        {\"name\":\"object1\", \"value\":858383},\n"
								+ "        {\"name\":\"object2\", \"value\":42}\n" + "      ]\n" + "    }"));
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

		HttpPollingDefaultConfigurationFactory httpPollingConfigFactory = new HttpPollingDefaultConfigurationFactory(
				loader);
		configuration.addDatasources(httpPollingConfigFactory.create());

		JsonataDefaultConfigurationFactory jsonataConfigFactory = new JsonataDefaultConfigurationFactory(loader);
		configuration.addTransformers(jsonataConfigFactory.create());
		
		return configuration;
	}

	private static void callApiAndCheckResult() throws ClientProtocolException, IOException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet request = new HttpGet("http://localhost:8090/valueA");
		CloseableHttpResponse resp = client.execute(request);
		String content = new String(resp.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

		assertEquals("858383", content);
		client.close();
	}
	
	@AfterClass
	public static void tearDown() {
		updater.stopComponent();
	}
}
