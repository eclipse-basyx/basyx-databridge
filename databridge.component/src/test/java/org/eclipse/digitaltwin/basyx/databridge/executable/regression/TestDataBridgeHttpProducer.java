package org.eclipse.digitaltwin.basyx.databridge.executable.regression;

import javax.servlet.http.HttpServlet;

import org.eclipse.basyx.aas.aggregator.api.IAASAggregator;
import org.eclipse.basyx.aas.aggregator.proxy.AASAggregatorProxy;
import org.eclipse.basyx.aas.registration.memory.InMemoryRegistry;
import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.component.DataBridgeExecutable;
import org.eclipse.digitaltwin.basyx.databridge.examples.aasjsonatahttp.test.HttpDummyServlet;
import org.eclipse.digitaltwin.basyx.databridge.examples.httpserver.HttpDataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class TestDataBridgeHttpProducer extends DataBridgeSuiteHttpProducer{
	
	private static final String AAS_AGGREGATOR_URL = "http://localhost:4001";
	private static AASServerComponent aasServer;
	private static BaSyxContextConfiguration aasContextConfig;
	private static InMemoryRegistry registry = new InMemoryRegistry();
	private static HttpDataSource httpData;
	private static HttpServlet httpServlet = new HttpDummyServlet();

	@BeforeClass
	public static void setUp() throws Exception {

		configureAndStartAasServer();
		
		server_start();
	
		startUpdaterComponent();
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		
		aasServer.stopComponent();
		
		server_stop();
		
		stopDataBridgeComponent();
	}
	
	@Override
	protected IAASAggregator getAASAggregatorProxy() {
		return new AASAggregatorProxy(AAS_AGGREGATOR_URL);
	}
	
	private static void configureAndStartAasServer() throws InterruptedException {
		
		aasContextConfig = new BaSyxContextConfiguration(4001, "");
		
		BaSyxAASServerConfiguration aasConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY, "aasx/telemeteryTest.aasx");
		
		aasServer = new AASServerComponent(aasContextConfig, aasConfig);
		aasServer.setRegistry(registry);
		aasServer.startComponent();
		
	}
	
	private static void startUpdaterComponent() {
		DataBridgeExecutable.main(new String[] {"src/test/resources/httpproducer/databridge"});
	}
	
	private static void stopDataBridgeComponent() {
		if(DataBridgeExecutable.getDataBridgeComponent() != null) {
			DataBridgeExecutable.getDataBridgeComponent().stopComponent();
		}
	}
	
	private static void server_start() throws InterruptedException {
		
		httpData = new HttpDataSource();
		httpData.runHttpServer(httpServlet);	
	}
	
	private static void server_stop() {
		
		httpData.stopHttpServer();
	}

	@Override
	protected void startHttpDummyServlet() {
		
	}
}
