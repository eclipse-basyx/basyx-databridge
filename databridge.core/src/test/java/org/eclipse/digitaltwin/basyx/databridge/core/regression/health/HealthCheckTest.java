/*******************************************************************************
 * Copyright (C) 2023 the Eclipse BaSyx Authors
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
package org.eclipse.digitaltwin.basyx.databridge.core.regression.health;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.health.HealthCheckRegistry;
import org.apache.camel.impl.health.DefaultHealthCheckRegistry;
import org.apache.camel.impl.health.RoutesHealthCheckRepository;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.eclipse.digitaltwin.basyx.databridge.core.health.routebuilder.HealthCheckRouteBuilder;
import org.eclipse.digitaltwin.basyx.databridge.core.health.utility.HealthCheckUtils;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Test class for health check implementation behavior
 *
 * @author danish
 *
 */
class HealthCheckTest extends CamelTestSupport {

	CamelContext context;

	@Override
	public boolean isUseAdviceWith() {
		return true;
	}

	@Override
	protected CamelContext createCamelContext() throws Exception {
		context = super.createCamelContext();

		DefaultHealthCheckRegistry registry = configureHealthCheckRegistry();

		context.setExtension(HealthCheckRegistry.class, registry);

		return context;
	}

	private DefaultHealthCheckRegistry configureHealthCheckRegistry() {
		DefaultHealthCheckRegistry registry = new DefaultHealthCheckRegistry();
		registry.register(new RoutesHealthCheckRepository());
		return registry;
	}

	@Override
	protected RouteBuilder createRouteBuilder() {
		return new HealthCheckRouteBuilder();
	}

	@Test
	void responseOkWhenServiceIsHealthy() throws InterruptedException, ClientProtocolException, IOException {
		context.start();

		HttpResponse response = getResponseFromHealthEndpoint(
				HealthCheckUtils.getHealthCheckEndpointRequestURI());

		String expectedRouteId = getRouteIdFromResponse(response, 0);

		assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

		assertThat(expectedRouteId, equalTo(HealthCheckUtils.ROUTE_ID));
	}

	@Test
	void responseServiceUnavailableWhenServiceIsUnhealthy() throws Exception {
		addDummyRoute();

		context.start();

		stopDummyRoute();

		HttpResponse response = getResponseFromHealthEndpoint(
				HealthCheckUtils.getHealthCheckEndpointRequestURI());

		String expectedRouteId = getRouteIdFromResponse(response, 0);

		assertEquals(HttpStatus.SC_SERVICE_UNAVAILABLE, response.getStatusLine().getStatusCode());

		assertThat(expectedRouteId, equalTo("test.endpoint"));
	}

	private void stopDummyRoute() throws Exception {
		context.getRouteController().stopRoute("test.endpoint");
	}

	private HttpResponse getResponseFromHealthEndpoint(String endpointUrl) throws IOException, ClientProtocolException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(endpointUrl);

		return client.execute(request);
	}

	private void addDummyRoute() throws Exception {
		context.addRoutes(new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				from("direct:test").routeId("test.endpoint").log("mock endpoint");
			}
		});
	}

	private String getRouteIdFromResponse(HttpResponse response, int index)
			throws JsonSyntaxException, ParseException, IOException {
		JsonArray jsonArray = JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonArray();
		JsonObject jsonObject = jsonArray.get(index).getAsJsonObject();
		String expectedValue = jsonObject.getAsJsonObject("details").get("route.id").getAsString();

		return expectedValue;
	}
}
