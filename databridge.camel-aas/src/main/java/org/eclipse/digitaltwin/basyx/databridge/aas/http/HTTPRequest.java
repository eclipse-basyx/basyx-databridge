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
package org.eclipse.digitaltwin.basyx.databridge.aas.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

/**
 * A helper class for sending HTTP requests
 * 
 * @author danish
 *
 */
public class HTTPRequest {
	
	public static void patchRequest(String url, String content) throws IOException {
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpPatch patchRequest = createPatchRequest(url, content);
		
        HttpResponse response = client.execute(patchRequest);

        HttpEntity responseEntity = response.getEntity();

        EntityUtils.consume(responseEntity);
	}

	private static HttpPatch createPatchRequest(String url, String content) throws UnsupportedEncodingException {
		HttpPatch patchRequest = new HttpPatch(url);

		patchRequest.setHeader("Content-type", "application/json");
		patchRequest.setEntity(new StringEntity(content));

		return patchRequest;
	}
	
	public static void putRequest(String url, String content) throws IOException {
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpPut httpPutRequest = createPutRequest(url, content);
		
        HttpResponse response = client.execute(httpPutRequest);

        HttpEntity responseEntity = response.getEntity();

        EntityUtils.consume(responseEntity);
	}

	private static HttpPut createPutRequest(String url, String content) throws UnsupportedEncodingException {
		HttpPut putRequest = new HttpPut(url);

		putRequest.setHeader("Content-type", "application/json");
		putRequest.setEntity(new StringEntity(content));

		return putRequest;
	}

}
