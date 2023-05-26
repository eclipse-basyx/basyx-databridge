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
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import org.eclipse.basyx.submodel.metamodel.connected.submodelelement.dataelement.ConnectedProperty;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetype.ValueType;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetype.ValueTypeHelper;
import org.eclipse.basyx.vab.modelprovider.VABElementProxy;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnectorFactory;
import org.eclipse.digitaltwin.basyx.databridge.aas.api.ApiType;
import org.eclipse.digitaltwin.basyx.databridge.aas.http.HTTPRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Producer implementation of AAS
 *
 */
public class AASProducer extends DefaultProducer {
	private static final Logger LOG = LoggerFactory.getLogger(AASProducer.class);
	
	private static final String API_V3_SUFFIX = "/$value";
	
	private AASEndpoint endpoint;
	private ConnectedProperty connectedProperty;

	public AASProducer(AASEndpoint endpoint) {
		super(endpoint);
		this.endpoint = endpoint;
		LOG.info("Creating AAS Producer for endpoint " + endpoint.getEndpointUri());
		connectToElement();
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		Object messageBody = exchange.getMessage().getBody(String.class);

		if (endpoint.getApi().equals(ApiType.BASYX)) {
			setPropertyValueUsingBaSyxAPI(messageBody);
		} else {
			setPropertyValueUsingDotAasV3Api(wrapStringValue(messageBody.toString()));
		}

		LOG.info("Transferred message={}", messageBody.toString());
	}

	private void setPropertyValueUsingBaSyxAPI(Object messageBody) {
		connectedProperty.setValue(getContent(messageBody));
	}

	private void setPropertyValueUsingDotAasV3Api(String content) throws IOException {
		HTTPRequest.patchRequest(endpoint.getFullProxyUrl() + API_V3_SUFFIX, content);
	}

	private Object getContent(Object messageBody) {
		if (connectedProperty.getValueType().equals(ValueType.String)) {
			return removeQuotesFromString(messageBody.toString());
		}

		return ValueTypeHelper.getJavaObject(messageBody, connectedProperty.getValueType());
	}

	private String removeQuotesFromString(String messageBody) {
		String fixedMessageBody = "";
		if (messageBody != null) {
			if (messageBody.startsWith("\"") && messageBody.endsWith("\"")) {
				fixedMessageBody = messageBody.substring(1, messageBody.length() - 1);
			} else {
				fixedMessageBody = messageBody;
			}
		}
		return fixedMessageBody;
	}
	
	private String wrapStringValue(String content) {
		if (content.isEmpty())
			return content;
		
		return "\"" + content + "\"";
	}

	/**
	 * Connect the Submodel Element for data dumping
	 */
	private void connectToElement() {
		if (!endpoint.getApi().equals(ApiType.BASYX))
			return;
		
		HTTPConnectorFactory factory = new HTTPConnectorFactory();
		String proxyUrl = this.endpoint.getFullProxyUrl();
		IModelProvider provider = factory.getConnector(proxyUrl);
		VABElementProxy proxy = new VABElementProxy("", provider);
		this.connectedProperty = new ConnectedProperty(proxy);
	}

}
