/*******************************************************************************
* Copyright (C) 2022 the Eclipse BaSyx Authors
* 
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/

* 
* SPDX-License-Identifier: EPL-2.0
******************************************************************************/

package basyx.components.databridge.aas;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.support.DefaultConsumer;
import org.apache.camel.support.DefaultMessage;
import org.eclipse.basyx.aas.metamodel.connected.ConnectedAssetAdministrationShell;
import org.eclipse.basyx.submodel.metamodel.connected.ConnectedSubmodel;
import org.eclipse.basyx.submodel.metamodel.connected.submodelelement.dataelement.ConnectedProperty;
import org.eclipse.basyx.submodel.metamodel.facade.SubmodelElementMapCollectionConverter;
import org.eclipse.basyx.vab.coder.json.serialization.DefaultTypeFactory;
import org.eclipse.basyx.vab.coder.json.serialization.GSONTools;
import org.eclipse.basyx.vab.modelprovider.VABElementProxy;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Camel consumer implementation for AAS
 * 
 * This consumer can be used in 3 different operation modes: ---- Fetch full JSON
 * of a AAS, when path="" ---- Fetch full JSON of a SM, when
 * path="submodel_shortId" ---- Fetch value of a specific submodelElement, when
 * path="submodel_shortId/submodelElement_shortId"
 * 
 * @author mateusmolina-iese
 *
 */
public class AASConsumer extends DefaultConsumer {
	private static final Logger logger = LoggerFactory.getLogger(AASConsumer.class);

	abstract class ConnectedObject {
		private VABElementProxy proxy;

		ConnectedObject(AASEndpoint endpoint) {
			HTTPConnectorFactory factory = new HTTPConnectorFactory();
			String proxyUrl = endpoint.getFullProxyUrl();
			IModelProvider provider = factory.getConnector(proxyUrl);
			proxy = new VABElementProxy("", provider);
		}

		VABElementProxy getProxy() {
			return proxy;
		}

		abstract String getPayload();
	}

	private ConnectedObject connectedObject;

	public AASConsumer(Endpoint endpoint, Processor processor) {
		super(endpoint, processor);

		if (!getEndpoint().getSubmodelElementId().isEmpty()) {
			logger.debug("Connecting to SubmodelElement @ " + getEndpoint().getFullProxyUrl());
			connectToSmElement();
			return;
		}
		if (!getEndpoint().getSubmodelId().isEmpty()) {
			logger.debug("Connecting to Submodel @ " + getEndpoint().getFullProxyUrl());
			connectToSm();
			return;
		} else {
			logger.debug("Connecting to AAS @ " + getEndpoint().getFullProxyUrl());
			connectToAas();
		}
	}

	@Override
	public AASEndpoint getEndpoint() {
		return (AASEndpoint) super.getEndpoint();
	}

	public Exchange createExchange(String msg) {
		Exchange exchange = createExchange(true);
		DefaultMessage exMsg = new DefaultMessage(exchange.getContext());
		exMsg.setBody(msg);
		exchange.setIn(exMsg);
		return exchange;
	}

	@Override
	protected void doStart() throws Exception {
		super.doStart();

		Exchange exchange = createExchange(connectedObject.getPayload());
		AsyncCallback cb = defaultConsumerCallback(exchange, true);
		getAsyncProcessor().process(exchange, cb);

		logger.debug("Received message={}", connectedObject.getPayload());
	}

	@Override
	protected void doStop() throws Exception {
		super.doStop();
	}


	private void connectToSmElement() {
		connectedObject = new ConnectedObject(getEndpoint()) {
			@Override
			String getPayload() {
				ConnectedProperty prop = new ConnectedProperty(getProxy());
				return prop.getValue().toString();
			}
		};
	}

	private void connectToSm() {
		connectedObject = new ConnectedObject(getEndpoint()) {
			@Override
			String getPayload() {
				ConnectedSubmodel sm = new ConnectedSubmodel(getProxy());
				return new GSONTools(new DefaultTypeFactory())
						.serialize(SubmodelElementMapCollectionConverter.smToMap(sm.getLocalCopy()));
			}
		};
	}

	private void connectToAas() {
		connectedObject = new ConnectedObject(getEndpoint()) {
			@Override
			String getPayload() {
				ConnectedAssetAdministrationShell aas = new ConnectedAssetAdministrationShell(getProxy());
				return new GSONTools(new DefaultTypeFactory()).serialize(aas.getLocalCopy());
			}
		};
	}


}
