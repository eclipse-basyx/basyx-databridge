/*******************************************************************************
 * Copyright (C) 2021 the Eclipse BaSyx Authors
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
package org.eclipse.digitaltwin.basyx.databridge.aas.configuration;

import org.eclipse.digitaltwin.basyx.databridge.aas.api.ApiType;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataSinkConfiguration;

/**
 * An implementation of AAS data sink configuration
 * 
 * @author haque, kammognie
 *
 */
public class AASDatasinkConfiguration extends DataSinkConfiguration {
	private static final String PROPERTY_TYPE = "PROPERTY";

	private String type;
	private String submodelEndpoint;
	private String idShortPath;
	private String api;

	public AASDatasinkConfiguration() {}
	
	public AASDatasinkConfiguration(String submodelEndpoint, String idShortPath, String uniqueId, String api) {
		super(uniqueId);
		this.type = PROPERTY_TYPE;
		this.submodelEndpoint = submodelEndpoint;
		this.idShortPath = idShortPath;
		this.api = api;
	}
	
	public AASDatasinkConfiguration(String submodelEndpoint, String idShortPath) {
		this(submodelEndpoint, idShortPath, null, ApiType.BASYX.getName());
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSubmodelEndpoint() {
		return submodelEndpoint;
	}

	public void setSubmodelEndpoint(String endpoint) {
		this.submodelEndpoint = endpoint;
	}

	public String getIdShortPath() {
		return idShortPath;
	}

	public void setIdShortPath(String path) {
		this.idShortPath = path;
	}

	public String getApi() {
		return api;
	}

	public void setApi(String api) {
		this.api = api;
	}

	@Override
	public String getConnectionURI() {
		String endpointDefinition = "aas:";
		endpointDefinition += this.submodelEndpoint;
		endpointDefinition += "?propertyPath=" + this.idShortPath;
		endpointDefinition += "&api=" + getApiIfConfigured();
		return endpointDefinition;
	}

	private String getApiIfConfigured() {
		return api != null ? api : ApiType.BASYX.getName();
	}
}
