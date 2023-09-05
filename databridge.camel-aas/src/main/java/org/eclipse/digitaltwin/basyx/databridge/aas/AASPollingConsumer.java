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

package org.eclipse.digitaltwin.basyx.databridge.aas;

import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultMessage;
import org.apache.camel.support.PollingConsumerSupport;
import org.eclipse.basyx.submodel.metamodel.connected.ConnectedSubmodel;
import org.eclipse.basyx.submodel.metamodel.connected.submodelelement.dataelement.ConnectedProperty;
import org.eclipse.basyx.submodel.metamodel.facade.SubmodelElementMapCollectionConverter;
import org.eclipse.basyx.vab.coder.json.serialization.DefaultTypeFactory;
import org.eclipse.basyx.vab.coder.json.serialization.GSONTools;
import org.eclipse.basyx.vab.modelprovider.VABElementProxy;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnectorFactory;

/**
 * An implementation of AAS Consumer
 * @author rana
 *
 */
public class AASPollingConsumer extends PollingConsumerSupport {
		
	private static final int WAIT_INDEFINITELY = -1;
	private static final int NO_WAIT = 0;
	private VABElementProxy proxy;
	private AASEndpoint endpoint;
	
	public AASPollingConsumer(AASEndpoint endpoint) {
		super(endpoint);
		this.endpoint = endpoint;
		
		connectToAasElement();
	}
	
	@Override
	public AASEndpoint getEndpoint() {
		return (AASEndpoint) super.getEndpoint();
	}
	

	@Override
	public Exchange receive() {
		return receive(WAIT_INDEFINITELY);
	}

	@Override
	public Exchange receiveNoWait() {
		return receive(NO_WAIT);
	}

	@Override
	public Exchange receive(long timeout) {
		
		Exchange exchange = createExchange(getSerializedMetamodel());
		
		defaultConsumerCallback(exchange, true);
		return exchange;
	}
	
	/**
	 * Connect to AAS Element for data dumping 
	 */
	private void connectToAasElement() {

		HTTPConnectorFactory factory = new HTTPConnectorFactory();
    	String proxyUrl = this.getAASUrl();
    	
    	IModelProvider provider = factory.getConnector(proxyUrl);
    	this.proxy = new VABElementProxy("", provider);
	}
	
	private Exchange createExchange(String exchangeProperty) {
		
		Exchange exchange = endpoint.createExchange();
		
		DefaultMessage exMsg = new DefaultMessage(exchange.getContext());
		exMsg.setBody(exchangeProperty);
		exchange.setIn(exMsg);
		
		return exchange;
	}
	
	/**
	 * Construct meta model
	 * @return serialized meta model
	 */
	private String getSerializedMetamodel() {
		
		if (!getEndpoint().getPropertyPath().isEmpty()) {
			ConnectedProperty prop = new ConnectedProperty(getProxy());
			return new GSONTools(new DefaultTypeFactory()).serialize(prop.getLocalCopy());
		}
    	
		ConnectedSubmodel sm = new ConnectedSubmodel(getProxy());
		return new GSONTools(new DefaultTypeFactory()).serialize(SubmodelElementMapCollectionConverter.smToMap(sm.getLocalCopy()));
	}
	
	/**
	 * This method returns current proxy is pointing to
	 * @return proxy
	 */
	private VABElementProxy getProxy() {
		return proxy;
	}
	
	/**
	 * Without specified property, it will return getSubmodelEndpoint
	 * With specified property, it will return getFullProxyUrl
	 * @return url
	 */
	protected String getAASUrl() {
		
		if (!this.getEndpoint().getPropertyPath().isEmpty())
			return this.getEndpoint().getFullProxyUrl();
		
		return this.getEndpoint().getSubmodelEndpoint();
	}
}
