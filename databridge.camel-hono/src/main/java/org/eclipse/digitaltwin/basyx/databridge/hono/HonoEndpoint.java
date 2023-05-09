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
package org.eclipse.digitaltwin.basyx.databridge.hono;

import org.apache.camel.Category;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;
import org.apache.camel.support.DefaultEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UriEndpoint(firstVersion = "1.0.0-SNAPSHOT", scheme = "hono", title = "HONO", syntax = "hono:name",
category = {Category.JAVA})
public class HonoEndpoint extends DefaultEndpoint {
	private static final Logger logger = LoggerFactory.getLogger(HonoEndpoint.class);
	
    @UriPath @Metadata(required = true)
    private String name;

	@UriParam(defaultValue = "15671")
    private String port = "15671";

	@UriParam(defaultValue = "consumer@HONO")
	private String userName = "consumer@HONO";

	@UriParam(defaultValue = "")
	private String tenantId = "";
	
	@UriParam(defaultValue = "")
	private String deviceId = "";
	
	@UriParam(defaultValue = "verysecret")
	private String password = "verysecret";

	public HonoEndpoint() {
    }

	public HonoEndpoint(String uri, HonoComponent component) {
        super(uri, component);
        logger.info("Hono URI: " + uri);
    }

    @Override
	public Producer createProducer() throws Exception {
		return null;
    }

	@Override
	public Consumer createConsumer(Processor processor) throws Exception {
		return new HonoConsumer(this, processor);
	}

    /**
     * Sets the name of the hono tenant
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the hono tenant
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Gets connection port
     * @return
     */
	public String getPort() {
		return port;
	}

	/**
	 * Sets connection port
	 * @param port
	 */
	public void setPort(String port) {
		this.port = port;
	}

	/**
	 * Gets user name for the connection
	 * @return
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets user name for the connection
	 * @param userName
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Gets the password to connect to the device
	 * @return
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password to connect to the device
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Sets tenant id
	 * @return
	 */
	public String getTenantId() {
		return tenantId;
	}

	/**
	 * Gets tenant id
	 * @param tenantId
	 */
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	/**
	 * Gets device id
	 * @return
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * Sets device id
	 * @param deviceId
	 */
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	/**
	 * Gets the Hono host for connection
	 * @return
	 */
	public String getHonoHost() {
		String onlyEndpoint = this.getEndpointBaseUri().substring(7); 
    	logger.info("only url " + onlyEndpoint);
		return onlyEndpoint;
	}
	
}
