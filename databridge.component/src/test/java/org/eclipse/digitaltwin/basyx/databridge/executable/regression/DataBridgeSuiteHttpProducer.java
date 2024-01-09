package org.eclipse.digitaltwin.basyx.databridge.executable.regression;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.basyx.aas.aggregator.api.IAASAggregator;
import org.eclipse.digitaltwin.basyx.databridge.examples.aasjsonatahttp.test.HttpDummyServlet;
import org.eclipse.digitaltwin.basyx.databridge.examples.httpserver.HttpDataSource;
import org.junit.Test;

public abstract class DataBridgeSuiteHttpProducer {
	
	protected abstract IAASAggregator getAASAggregatorProxy();
	
	protected abstract void startHttpDummyServlet() throws InterruptedException;
	
	@Test
	public void CheckPropertyValueA() throws  InterruptedException, IOException {
		
		String expected_value = wrapStringValue("103.5585973");
		
		waitForPropagation();
		
		String actualValue = getContentFromEndPoint();
		
		assertEquals(expected_value, actualValue);
	}
	
	private static String getContentFromEndPoint() throws ClientProtocolException, IOException, InterruptedException {
		
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet request = new HttpGet("http://localhost:8091");
		CloseableHttpResponse resp = client.execute(request);
		String content = new String(resp.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
		
		client.close();
		return content;
	}

	private String wrapStringValue(String value) {
		
		return "\"" + value + "\"";
	}
	
	private static void waitForPropagation() throws InterruptedException {
		Thread.sleep(10000);
	}

}
