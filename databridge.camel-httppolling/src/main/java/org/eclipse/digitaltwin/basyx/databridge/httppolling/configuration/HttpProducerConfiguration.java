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

import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataSinkConfiguration;

/**
 * An implementation of HTTP producer configuration
 * 
 * @author rana
 *
 */
public class HttpProducerConfiguration extends DataSinkConfiguration{
	
	private String scheme;
	private String ServerUrl;
	private String path;
	private int port;
	
	public HttpProducerConfiguration() {}
	
	public HttpProducerConfiguration(String uniqueId, String scheme,  String ServerUrl, String path, int port) {
		super(uniqueId);
		this.scheme = scheme;
		this.ServerUrl = ServerUrl;
		this.path = path;
		this.port = port;
	}
	
	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getServerUrl() {
		return ServerUrl;
	}

	public void setServerUrl(String hostUrl) {
		this.ServerUrl = hostUrl;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String getConnectionURI() {
	
		URIBuilder uriBuilder = new URIBuilder();
		uriBuilder.setScheme(getScheme());
		uriBuilder.setHost(getServerUrl());
		uriBuilder.setPort(getPort());
		uriBuilder.setPath(getPath());
		
		return buildConnectionUri(uriBuilder);
	}
	
	private String buildConnectionUri(URIBuilder uriBuilder) {
		
		try {
			return uriBuilder.build().toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new RuntimeException("Exception occurred while creating HttpProducerEndPoint ");
		}
	}

}
