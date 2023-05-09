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
package org.eclipse.digitaltwin.basyx.databridge.hono.configuration;

import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataSourceConfiguration;

/**
 * An implementation of ActiveMQ consumer configuration
 * @author haque
 *
 */
public class HonoConsumerConfiguration extends DataSourceConfiguration {
	private String userName;
	private String password;
	private String tenantId;
	private String deviceId;

	public HonoConsumerConfiguration() {}

	public HonoConsumerConfiguration(String uniqueId, String serverUrl, int serverPort, String userName, String password, String tenantId, String deviceId) {
		super(uniqueId, serverUrl, serverPort);
		this.userName = userName;
		this.password = password;
		this.tenantId = tenantId;
		this.deviceId = deviceId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getConnectionURI() {
		return "hono:" + getServerUrl() + 
				"?port=" + getServerPort() +
				"&userName=" + getUserName() +
				"&password=" + getPassword() +
				"&deviceId=" + getDeviceId() +
				"&tenantId=" + getTenantId();
	}
}
