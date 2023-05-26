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
package org.eclipse.digitaltwin.basyx.databridge.aas;

import java.io.IOException;

import org.apache.camel.Category;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;
import org.apache.camel.support.DefaultEndpoint;
import org.eclipse.basyx.submodel.metamodel.connected.submodelelement.dataelement.ConnectedProperty;
import org.eclipse.basyx.vab.modelprovider.VABElementProxy;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnectorFactory;
import org.eclipse.digitaltwin.basyx.databridge.aas.api.ApiType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.eclipse.digitaltwin.basyx.databridge.aas.utils.AASEndpointUtil.createBaSyxApiProxyUrl;
import static org.eclipse.digitaltwin.basyx.databridge.aas.utils.AASEndpointUtil.createDotAasApiProxyUrl;
import static org.eclipse.digitaltwin.basyx.databridge.aas.utils.AASEndpointUtil.wrapStringValue;
import static org.eclipse.digitaltwin.basyx.databridge.aas.utils.AASEndpointUtil.setPropertyValueUsingBaSyxAPI;
import static org.eclipse.digitaltwin.basyx.databridge.aas.utils.AASEndpointUtil.setPropertyValueUsingDotAasV3Api;

/**
 * AAS component which can connect to Asset Administration Shells via a given
 * registry
 */
@UriEndpoint(firstVersion = "1.0.0-SNAPSHOT", scheme = "aas", title = "AAS", syntax = "aas:name", category = {
		Category.JAVA })
public class AASEndpoint extends DefaultEndpoint {
	private static final Logger logger = LoggerFactory.getLogger(AASEndpoint.class);

	private ConnectedProperty connectedProperty;
	private static final String API_V3_SUFFIX = "/$value";

	@UriPath
	@Metadata(required = true)
	private String name;

	@UriParam(defaultValue = "")
	private String propertyPath;

	@UriParam(defaultValue = "BaSyx")
	private ApiType api;

	public AASEndpoint() {
	}

	public AASEndpoint(String uri, AASComponent component) {
		super(uri, component);
	}

	@Override
	public Producer createProducer() throws Exception {
		return new AASProducer(this);
	}

	@Override
	public Consumer createConsumer(Processor processor) throws Exception {
		return null;
	}

	/**
	 * Sets the name
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/**
	 * The path to the property relative to the target AAS
	 */
	public String getPropertyPath() {
		return propertyPath;
	}

	public void setPropertyPath(String propertyPath) {
		this.propertyPath = propertyPath;
	}

	/**
	 * The Api type for this endpoint
	 * 
	 * @return ApiType
	 */
	protected ApiType getApi() {
		return api;
	}

	/**
	 * Sets the Api type for this endpoint
	 * 
	 * @param api
	 */
	protected void setApi(ApiType api) {
		this.api = api;
	}

	public void setPropertyValue(Object content) throws IOException {
		if (api.equals(ApiType.BASYX)) {
			setPropertyValueUsingBaSyxAPI(content, connectedProperty);
		} else {
			setPropertyValueUsingDotAasV3Api(getFullProxyUrl() + API_V3_SUFFIX, wrapStringValue(content.toString()));
		}

		logger.info("Transferred message={}", content.toString());
	}

	/**
	 * Connect the Submodel Element for data dumping
	 */
	protected void connectToElement() {
		if (!api.equals(ApiType.BASYX))
			return;

		HTTPConnectorFactory factory = new HTTPConnectorFactory();
		String proxyUrl = getFullProxyUrl();
		IModelProvider provider = factory.getConnector(proxyUrl);
		VABElementProxy proxy = new VABElementProxy("", provider);
		this.connectedProperty = new ConnectedProperty(proxy);
	}
	
	private String getFullProxyUrl() {
		if (api.equals(ApiType.BASYX))
			return createBaSyxApiProxyUrl(this.getEndpointBaseUri(), this.propertyPath);

		return createDotAasApiProxyUrl(this.getEndpointBaseUri(), this.propertyPath);
	}

}
