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
package org.eclipse.digitaltwin.basyx.databridge.plc4x.configuration;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.camel.Endpoint;
import org.apache.camel.component.plc4x.Plc4XComponent;
import org.apache.camel.component.plc4x.Plc4XEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataSourceConfiguration;

/**
 * An implementation of PLC4X consumer configuration
 * 
 * @author danish
 *
 */
public class Plc4XConsumerConfiguration extends DataSourceConfiguration {
	private String driver;
	private String servicePath = "";
	private String options = "";
	private List<Tag> tags;

	public Plc4XConsumerConfiguration() {
	}

	public Plc4XConsumerConfiguration(String uniqueId, String serverUrl, int serverPort, String driver,
			String servicePath, String options, List<Tag> tags) {
		super(uniqueId, serverUrl, serverPort);
		this.driver = driver;
		this.servicePath = servicePath;
		this.options = options;
		this.tags = tags;
	}
	
	public String getServicePath() {
		return servicePath;
	}

	public String getDriver() {
		return driver;
	}

	public String getOptions() {
		return options;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public Endpoint getConnectionURI() {
		return createPlc4xEndpoint();
	}

	private Plc4XEndpoint createPlc4xEndpoint() {
		Plc4XEndpoint plc4xEndpoint = new Plc4XEndpoint(getConnectionString(), new Plc4XComponent());
		
		Map<String, Object> tagMap = this.tags.stream().map(this::toMap).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		plc4xEndpoint.setTags(tagMap);
		plc4xEndpoint.setCamelContext(new DefaultCamelContext());

		return plc4xEndpoint;
	}

	private String getConnectionString() {
		return "plc4x:" + this.driver + "://" + getServerUrl() + ":" + getServerPort() + getServicePathIfAvailable() + "?" + getOptions();
	}
	
	private Map.Entry<String, Object> toMap(Tag tag) {
		return new AbstractMap.SimpleEntry<>(tag.getName(), tag.getValue());
	}
	
	private String getServicePathIfAvailable() {
		return getServicePath().isEmpty() ? "" : "/" + getServicePath();
	}
}
