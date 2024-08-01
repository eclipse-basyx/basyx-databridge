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
import static org.mockserver.model.HttpRequest.request;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.digitaltwin.basyx.databridge.examples.sqlaas.TestDb;
import org.eclipse.digitaltwin.basyx.databridge.sql.configuration.SqlConsumerConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.verify.VerificationTimes;

/**
 * Test suite for {@link SqlConsumerConfiguration}
 * 
 * @author mateusmolina
 */
public abstract class DataBridgeSuiteSQL {
	private static final String VALUE_PATH = "/submodels/dmFsdWU=/submodel-elements/value/$value";
	private static final String TESTDB_PATH = "src/test/resources/sql/values.db";

	private static ClientAndServer mockServer;
	
	private static TestDb testDb;

	@BeforeClass
	public static void setUp() {
		configureAndStartMockserver();

		try (TestDb testDb = new TestDb(TESTDB_PATH)) {
			DataBridgeSuiteSQL.testDb = testDb;

			testDb.startInsertingValues(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void tearDown() {
		testDb.stopInsertingValues();
		mockServer.close();
	}

	@Test
	public void testSqlConfiguration() throws InterruptedException {
		Thread.sleep(6000);
		verifyCalls(VALUE_PATH, 2);
	}

	private static void configureAndStartMockserver() {
		mockServer = ClientAndServer.startClientAndServer(4001);
	}

	@SuppressWarnings("resource")
	private void verifyCalls(String path, int atLeastCalledTimes) {
		MockServerClient mockServerClient = new MockServerClient("localhost", 4001).verify(request().withMethod("PATCH")
				.withPath(path)
				.withHeader("Content-Type", "application/json"), VerificationTimes.atLeast(atLeastCalledTimes));

		verifyUniqueBodies(path, mockServerClient);
	}

	private void verifyUniqueBodies(String path, MockServerClient mockServerClient) {
		HttpRequest[] recordedRequests = retrieveRequests(path, mockServerClient);

		Set<String> uniqueBodies = Arrays.stream(recordedRequests).map(HttpRequest::getBodyAsString).collect(Collectors.toSet());
		assertEquals(recordedRequests.length, uniqueBodies.size());
	}

	private HttpRequest[] retrieveRequests(String path, MockServerClient mockServerClient) {
		return mockServerClient.retrieveRecordedRequests(HttpRequest.request()
				.withMethod("PATCH")
				.withPath(path)
				.withHeader("Content-Type", "application/json"));
	}
}
