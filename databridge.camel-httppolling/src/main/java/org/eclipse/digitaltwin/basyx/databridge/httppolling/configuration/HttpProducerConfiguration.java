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
package org.eclipse.digitaltwin.basyx.databridge.httppolling.configuration;

import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataSinkConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.httppolling.configuration.api.ApiType;

/**
 * An implementation of HTTP producer configuration
 * 
 * @author rana
 *
 */
public class HttpProducerConfiguration extends DataSinkConfiguration{
	
	private String httpUri;
	private String api;
	
	public HttpProducerConfiguration() {}
	
	public HttpProducerConfiguration(String uniqueId, String httpUri, String api) {
		super(uniqueId);
		this.httpUri = httpUri;
		this.api = api;
	}
	
	public HttpProducerConfiguration(String httpUri) {
		this(httpUri, null, ApiType.BASYX.getName());
	}
	
	public String gethttpUri() {
		return httpUri;
	}

	public void sethttpUri(String httpUri) {
		this.httpUri = httpUri;
	}

	public String getApi() {
		return api;
	}

	public void setApi(String api) {
		this.api = api;
	}

	@Override
	public String getConnectionURI() {
		
		StringBuilder endpointDefinition = new StringBuilder();
		
		endpointDefinition.append(this.httpUri);
		endpointDefinition.append("/?httpMethod=POST");
		endpointDefinition.append("&api=");
		endpointDefinition.append(getApiIfConfigured());
		
		return endpointDefinition.toString();
	}

	private String getApiIfConfigured() {
		return api != null ? api : ApiType.BASYX.getName();
	}
}
