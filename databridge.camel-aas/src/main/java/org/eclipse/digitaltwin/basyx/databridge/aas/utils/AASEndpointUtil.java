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
package org.eclipse.digitaltwin.basyx.databridge.aas.utils;

import java.io.IOException;

import org.eclipse.basyx.submodel.metamodel.connected.submodelelement.dataelement.ConnectedProperty;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetype.ValueType;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetype.ValueTypeHelper;
import org.eclipse.digitaltwin.basyx.databridge.aas.AASEndpoint;
import org.eclipse.digitaltwin.basyx.databridge.aas.http.HTTPRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for {@link AASEndpoint}
 * 
 * @author danish
 *
 */
public class AASEndpointUtil {
	private static Logger logger = LoggerFactory.getLogger(AASEndpointUtil.class);

	private AASEndpointUtil() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Sets the value of the property using BaSyx type API
	 * 
	 * @param messageBody
	 * @param connectedProperty
	 */
	public static void setPropertyValueUsingBaSyxAPI(Object messageBody, ConnectedProperty connectedProperty) {
		connectedProperty.setValue(getContent(messageBody, connectedProperty));
	}

	/**
	 * Sets the value of the property using DotAAS-V3 conformant type API
	 * 
	 * @param url
	 * @param content
	 */
	public static void setPropertyValueUsingDotAasV3Api(String url, String content) throws IOException {
		HTTPRequest.patchRequest(url, content);
	}

	/**
	 * Creates the DotAAS-V3 conformant type URL
	 * 
	 * @param baseUri
	 * @param submodelElementIdShortPath
	 * @return proxyUrl DotAAS-V3 conformant type URL
	 */
	public static String createDotAasApiProxyUrl(String baseUri, String submodelElementIdShortPath) {
		String proxyUrl = String.format("%s/submodel-elements/%s", getSubmodelEndpoint(baseUri),
				submodelElementIdShortPath);

		logger.info("Proxy URL: " + proxyUrl);

		return proxyUrl;
	}

	/**
	 * Creates BaSyx api type URL
	 * 
	 * @param baseUri
	 * @param submodelElementIdShortPath
	 * @return proxyUrl BaSyx api type URL
	 */
	public static String createBaSyxApiProxyUrl(String baseUri, String submodelElementIdShortPath) {
		String proxyUrl = String.format("%s/submodelElements/%s", getSubmodelEndpoint(baseUri),
				submodelElementIdShortPath);

		logger.info("Proxy URL: " + proxyUrl);

		return proxyUrl;
	}

	/**
	 * Wraps provided string in double quotes
	 * 
	 * @param content
	 * @return
	 */
	public static String wrapStringValue(String content) {
		if (content.isEmpty())
			return content;

		return "\"" + content + "\"";
	}

	private static Object getContent(Object messageBody, ConnectedProperty connectedProperty) {
		if (connectedProperty.getValueType().equals(ValueType.String)) {
			return removeQuotesFromString(messageBody.toString());
		}

		return ValueTypeHelper.getJavaObject(messageBody, connectedProperty.getValueType());
	}

	private static String removeQuotesFromString(String messageBody) {
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

	private static String getSubmodelEndpoint(String baseUri) {
		String submodelEndpoint = baseUri.substring(4);

		logger.info("SubmodelEndpoint " + submodelEndpoint);

		return submodelEndpoint;
	}

}
