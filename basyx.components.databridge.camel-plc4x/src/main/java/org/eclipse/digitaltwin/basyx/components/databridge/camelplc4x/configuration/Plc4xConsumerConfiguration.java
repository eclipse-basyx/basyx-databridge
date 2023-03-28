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
package org.eclipse.digitaltwin.basyx.components.databridge.camelplc4x.configuration;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.camel.Endpoint;
import org.apache.camel.component.plc4x.Plc4XComponent;
import org.apache.camel.component.plc4x.Plc4XEndpoint;
import org.eclipse.digitaltwin.basyx.components.databridge.core.configuration.entity.DataSourceConfiguration;

/**
 * An implementation of PLC4X consumer configuration
 * 
 * @author danish
 *
 */
public class Plc4xConsumerConfiguration extends DataSourceConfiguration {
	private String driver;
	private String servicePath = "";
	private String options = "";
	private List<Tag> tags;

	public Plc4xConsumerConfiguration() {
	}

	public Plc4xConsumerConfiguration(String uniqueId, String serverUrl, int serverPort, String driver,
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

	private Endpoint createPlc4xEndpoint() {
		Endpoint plc4xEndpoint1 = new Plc4XEndpoint(getConnectionString(), new Plc4XComponent());
		
		Map<String, Object> tagMap = this.tags.stream().map(this::toMap).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		((Plc4XEndpoint) plc4xEndpoint1).setTags(tagMap);

		return plc4xEndpoint1;
	}

	private String getConnectionString() {
		return "plc4x:" + this.driver + "://" + getServerUrl() + ":" + getServerPort() + "/" + getServicePath() + "?" + getOptions();
	}
	
	private Map.Entry<String, Object> toMap(Tag tag) {
		return new AbstractMap.SimpleEntry<>(tag.getName(), tag.getValue());
	}
}
