/*******************************************************************************
 * Copyright (C) 2024 the Eclipse BaSyx Authors
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
package org.eclipse.digitaltwin.basyx.databridge.opcua.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.basyx.vab.protocol.opcua.types.SecurityPolicy;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataSinkConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.opcua.configuration.factory.OpcuaProducerConfigurationDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.JsonAdapter;

/**
 * An implementation of OpcUa Producer configuration
 *
 * @author zielstor
 *
 */
@JsonAdapter(OpcuaProducerConfigurationDeserializer.class)
public class OpcuaProducerConfiguration extends DataSinkConfiguration {
    protected static Logger logger = LoggerFactory.getLogger(OpcuaProducerConfiguration.class);

	private String serverUrl;
	private int serverPort;
    private String pathToService;
    private String nodeInformation;
    private String username;
    private String password;
    private Map<String, String> configuration = new HashMap<>();

    public OpcuaProducerConfiguration() {
    }

    public OpcuaProducerConfiguration(
            String uniqueId,
            String serverUrl,
            int serverPort,
            String pathToService,
            String nodeInformation,
            String username,
            String password,
            Map<String, String> configuration
    ) {
		super(uniqueId);

		this.serverUrl = serverUrl;
		this.serverPort = serverPort;
        this.pathToService = pathToService;
        this.nodeInformation = nodeInformation;
        this.username = username;
        this.password = password;
        this.configuration = configuration;

        if (!this.configuration.containsKey("allowedSecurityPolicies"))
            this.configuration.put("allowedSecurityPolicies", SecurityPolicy.None.name());
        if (!this.configuration.containsKey("requestedPublishingInterval"))
            this.configuration.put("requestedPublishingInterval", "1000");
    }

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

    public String getPathToService() {
        return pathToService;
    }

    public void setPathToService(String pathToService) {
        this.pathToService = pathToService;
    }

    public String getNodeInformation() {
        return nodeInformation;
    }

    public void setNodeInformation(String nodeInformation) {
        this.nodeInformation = nodeInformation;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, String> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
    }

    public String getConnectionURI() {
        String credentials = username == null || password == null ? StringUtils.EMPTY : username + ":" + password + "@";
        String parameters = this.configuration.entrySet()
                .parallelStream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("&"));

        return String.format(
                "milo-client:opc.tcp://%s%s:%d/%s?node=RAW(%s)%s",
                credentials,
				getServerUrl(),
				getServerPort(),
                getPathToService(),
                getNodeInformation(),
                parameters.length() > 0 ? "&" + parameters : StringUtils.EMPTY);
    }
}
