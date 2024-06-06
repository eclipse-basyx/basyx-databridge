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

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultMessage;
import org.apache.camel.support.PollingConsumerSupport;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.basyx.submodel.metamodel.connected.ConnectedSubmodel;
import org.eclipse.basyx.submodel.metamodel.connected.submodelelement.dataelement.ConnectedProperty;
import org.eclipse.basyx.submodel.metamodel.facade.SubmodelElementMapCollectionConverter;
import org.eclipse.basyx.vab.coder.json.serialization.DefaultTypeFactory;
import org.eclipse.basyx.vab.coder.json.serialization.GSONTools;
import org.eclipse.basyx.vab.modelprovider.VABElementProxy;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnectorFactory;
import org.eclipse.digitaltwin.basyx.databridge.aas.api.ApiType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of AASPolling Consumer
 * @author rana
 *
 */
public class AASPollingConsumer extends PollingConsumerSupport {
	
	private static final Logger logger = LoggerFactory.getLogger(AASPollingConsumer.class);
	
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
		return doReceive(WAIT_INDEFINITELY);
	}

	@Override
	public Exchange receiveNoWait() {
		return doReceive(NO_WAIT);
	}

	@Override
	public Exchange receive(long timeout) {
		return doReceive(timeout);
	}
	
	protected Exchange doReceive(long timeout) {
		
		String result;
		
		ExecutorService execService = Executors.newSingleThreadExecutor();
        Callable<String> runWithTimeout = this::getSerializedMetamodel;
        
        Future<String> responseFuture = execService.submit(runWithTimeout);
        
        try {
        	result = responseFuture.get(timeout, TimeUnit.SECONDS); 
        	
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
        	
        	logger.info("Time out after waiting");
        	
        	getExceptionHandler().handleException(e);
        	
        	result = "";
        }
        
		Exchange exchange = createExchange(result);
		defaultConsumerCallback(exchange, true);
	
		return exchange;
	}
	
	/**
	 * Connect to AAS Element for data dumping 
	 */
	private void connectToAasElement() {

		HTTPConnectorFactory factory = new HTTPConnectorFactory();
    	String proxyUrl = this.getMetamodelUrl();
    	
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
	 * Get serialized metamodel
	 * @return serialized meta model
	 */
	private String getSerializedMetamodel() throws IOException {
		if(this.endpoint.getApi() == ApiType.BASYX) {
			if (!getEndpoint().getPropertyPath().isEmpty()) {
				ConnectedProperty prop = new ConnectedProperty(this.proxy);
				return new GSONTools(new DefaultTypeFactory()).serialize(prop.getLocalCopy());
			}

			ConnectedSubmodel sm = new ConnectedSubmodel(this.proxy);
			return new GSONTools(new DefaultTypeFactory()).serialize(SubmodelElementMapCollectionConverter.smToMap(sm.getLocalCopy()));
		}else{
			String metamodelUrl = getMetamodelUrl().substring(2);
			return getResponseAsString(executeGetOnURL(metamodelUrl));
		}
	}

	public static String getResponseAsString(CloseableHttpResponse retrievalResponse) throws IOException, ParseException {
		return EntityUtils.toString(retrievalResponse.getEntity(), "UTF-8");
	}

	public static CloseableHttpResponse executeGetOnURL(String url) throws IOException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet getRequest = createGetRequestWithHeader(url);
		return client.execute(getRequest);
	}
	private static HttpGet createGetRequestWithHeader(String url) {
		HttpGet aasCreateRequest = new HttpGet(url);
		aasCreateRequest.setHeader("Content-type", "application/json");
		aasCreateRequest.setHeader("Accept", "application/json");
		return aasCreateRequest;
	}

	/**
	 * Get metamodel url
	 * @return url
	 */
	protected String getMetamodelUrl() {
		
		if (!this.getEndpoint().getPropertyPath().isEmpty())
			return this.getEndpoint().getFullProxyUrl();
		
		return this.getEndpoint().getSubmodelEndpoint();
	}
}
