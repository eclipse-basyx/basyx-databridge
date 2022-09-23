/*******************************************************************************
* Copyright (C) 2021 the Eclipse BaSyx Authors
* 
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/

* 
* SPDX-License-Identifier: EPL-2.0
******************************************************************************/

package basyx.components.databridge.aas;

import org.apache.camel.Category;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;
import org.apache.camel.support.DefaultEndpoint;
import org.eclipse.basyx.vab.modelprovider.VABPathTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AAS component which can connect to Asset Administration Shells via a given
 * registry
 * 
 * @author haque, mateusmolina-iese
 */
@UriEndpoint(firstVersion = "1.0.0-SNAPSHOT", scheme = "aas", title = "AAS", syntax = "aas:name",
             category = {Category.JAVA})
public class AASEndpoint extends DefaultEndpoint {
	private static final Logger logger = LoggerFactory.getLogger(AASEndpoint.class);
	
	@UriPath
	@Metadata(required = true)
	private String name;

	@UriParam(defaultValue = "")
	private String propertyPath;

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
		AASConsumer consumer = new AASConsumer(this, processor);
		configureConsumer(consumer);
		return consumer;
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
	
	public String getFullProxyUrl() {
		String elemUrl;
		if (!getSubmodelElementId().isEmpty()) {
			elemUrl = String.format("%s/submodels/%s/submodel/submodelElements/%s", this.getAASEndpoint(), this.getSubmodelId(), this.getSubmodelElementId());
		} else if (!getSubmodelId().isEmpty()) {
			elemUrl = String.format("%s/submodels/%s/submodel", this.getAASEndpoint(), this.getSubmodelId());
		} else {
			elemUrl = this.getAASEndpoint();
		}

		logger.debug("Proxy URL: " + elemUrl);
		return elemUrl;
	}
	
	/**
	 * Gets the AAS URL for connection
	 * @return
	 */
	private String getAASEndpoint() {
		String onlyEndpoint = this.getEndpointBaseUri().substring(6); 
		logger.debug("only url " + onlyEndpoint);
		return onlyEndpoint;
	}
	
	/**
	 * Gets the Submodel ID for data dump
	 * @return
	 */
	protected String getSubmodelId() {
		String submodelId = "";
		try {
			submodelId = VABPathTools.getEntry(getPropertyPath(), 0);
			logger.debug("Submodel:" + submodelId);
		} catch (Exception e) {
		}
		return submodelId;
	}
	
	/**
	 * Gets the submodel element id for data dump
	 * @return 
	 */
	protected String getSubmodelElementId() {
		String submodelElementId = "";
		try {
			submodelElementId = VABPathTools.getEntry(getPropertyPath(), 1);
			logger.debug("Submodel Element ID: " + submodelElementId);
		} catch (Exception e) {
		}
		return submodelElementId;
	}
	
}
