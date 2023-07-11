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

import java.net.URISyntaxException;
import java.util.List;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataSourceConfiguration;
import org.eclipse.digitaltwin.basyx.databridge.plc4x.configuration.deserializer.OptionDeserializer;

/**
 * An implementation of PLC4X consumer configuration
 * 
 * @author danish
 *
 */
public class Plc4XConsumerConfiguration extends DataSourceConfiguration {
	
	private static final String PLC4X_PROTOCOL_NAME = "plc4x:";
	private static final String TAG_PREFIX = "tag.";
	
	private String driver;
	private String servicePath = "";
	private Object options;
	private List<Tag> tags;

	public Plc4XConsumerConfiguration() {
	}

	public Plc4XConsumerConfiguration(String uniqueId, String serverUrl, int serverPort, String driver,
			String servicePath, Object options, List<Tag> tags) {
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

	public List<Option> getOptions() {
		return new OptionDeserializer().deserialize(options);
	}

	public List<Tag> getTags() {
		return tags;
	}

	public String getConnectionURI() {
		URIBuilder uriBuilder = new URIBuilder();
		uriBuilder.setScheme(PLC4X_PROTOCOL_NAME + driver);
		uriBuilder.setHost(getServerUrl());
		uriBuilder.setPort(getServerPort());
		uriBuilder.setPath(servicePath);
		
		addOptionsIfConfigured(uriBuilder);
		
		addTagsIfConfigured(uriBuilder);
		
		return buildConnectionUri(uriBuilder);
	}

	private void addTagsIfConfigured(URIBuilder uriBuilder) {
		if (tags == null || tags.isEmpty())
			return;
		
		tags.stream().forEach(tag -> uriBuilder.addParameter(TAG_PREFIX + tag.getName(), tag.getValue()));
	}

	private void addOptionsIfConfigured(URIBuilder uriBuilder) {
		List<Option> optionList = getOptions();
		
		if (optionList.isEmpty())
			return;
		
		optionList.stream().forEach(option -> uriBuilder.addParameter(option.getName(), option.getValue()));
	}
	
	private String buildConnectionUri(URIBuilder uriBuilder) {
		try {
			return uriBuilder.build().toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new RuntimeException("Exception occurred while creating Plc4XEndpoint");
		}
	}
}
