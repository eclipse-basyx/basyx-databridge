/*******************************************************************************
 * Copyright (C) 2024 the Eclipse BaSyx Authors
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

package org.eclipse.digitaltwin.basyx.databridge.executable.regression;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServlet;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.basyx.aas.aggregator.api.IAASAggregator;
import org.eclipse.digitaltwin.basyx.databridge.examples.aasjsonatahttp.Server;
import org.eclipse.digitaltwin.basyx.databridge.examples.httpserver.HttpDataSource;
import org.junit.Test;

/**
 * Suite for testing that the DataBridge is setup correctly with HTTPP Producer
 * 
 * @author rana
 *
 */
public abstract class DataBridgeSuiteHttpProducer {
	
	protected abstract IAASAggregator getAASAggregatorProxy();
	protected abstract String getEndpoint();
	protected abstract String getHost();
	private static HttpDataSource httpData;
	private static HttpServlet httpServlet = new Server();
	private static int PORT = 8091;
	
	@Test
	public void CheckPropertyValueA() throws  InterruptedException, IOException {
		
		serverStart();
		
		String expected_value = wrapStringValue("103.5585973");
		
		waitForPropagation();
		
		String actualValue = getContentFromEndPoint();
		
		assertEquals(expected_value, actualValue);
		
		serverStop();
	}
	
	private String getContentFromEndPoint() throws ClientProtocolException, IOException, InterruptedException {
		
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet request = new HttpGet(getEndpoint());
		CloseableHttpResponse resp = client.execute(request);
		String content = new String(resp.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
		client.close();
		
		return content;
	}

	private void serverStart() throws InterruptedException {
		
		httpData = new HttpDataSource();
		httpData.runHttpServer(getHost(), PORT, httpServlet);	
	}
	
	private void serverStop() {
		
		httpData.stopHttpServer();
	}
	
	private String wrapStringValue(String value) {
		
		return "\"" + value + "\"";
	}
	
	private void waitForPropagation() throws InterruptedException {
		Thread.sleep(10000);
	}
}
