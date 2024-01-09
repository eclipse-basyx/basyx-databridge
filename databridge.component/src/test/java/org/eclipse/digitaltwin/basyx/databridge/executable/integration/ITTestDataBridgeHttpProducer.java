package org.eclipse.digitaltwin.basyx.databridge.executable.integration;

import javax.servlet.http.HttpServlet;

import org.eclipse.basyx.aas.aggregator.api.IAASAggregator;
import org.eclipse.basyx.aas.aggregator.proxy.AASAggregatorProxy;
import org.eclipse.digitaltwin.basyx.databridge.examples.aasjsonatahttp.test.HttpDummyServlet;
import org.eclipse.digitaltwin.basyx.databridge.examples.httpserver.HttpDataSource;
import org.eclipse.digitaltwin.basyx.databridge.executable.regression.DataBridgeSuiteHttpProducer;


public class ITTestDataBridgeHttpProducer extends DataBridgeSuiteHttpProducer{

	private static String HOST = "localhost";
	private static HttpDataSource httpData;
	private static HttpServlet httpServlet = new HttpDummyServlet();
	
	@Override
	protected IAASAggregator getAASAggregatorProxy() {
		return new AASAggregatorProxy("http://" + HOST + ":4001");
	}

	@Override
	protected void startHttpDummyServlet() throws InterruptedException {
		
		httpData = new HttpDataSource();
		httpData.runHttpServer(httpServlet);
	}
}
