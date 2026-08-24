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
package org.eclipse.digitaltwin.basyx.databridge.kafka.configuration;

import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataSinkConfiguration;

/**
 * An implementation of kafka producer configuration
 * @author iamaccosta
 *
 */
public class KafkaProducerConfiguration extends DataSinkConfiguration {
	private String serverUrl;
	private int serverPort;
	private String topic;
	private String groupId;
	private String seekTo;
	private int maxPublishRecords;
	private int producersCount;
	private int bufferMemorySize;
	private int maxRequestSize;
	
	public KafkaProducerConfiguration() {}
	
	public KafkaProducerConfiguration(
		String uniqueId, 
		String serverUrl, 
		int serverPort, 
		String topic,
		String groupId, 
		String seekTo,
		int maxPublishRecords, 
		int producersCount,
		int bufferMemorySize,
		int maxRequestSize 
	) {
		super(uniqueId);

		this.serverUrl = serverUrl;
		this.serverPort = serverPort;
		this.topic = topic;
		this.groupId = groupId;
		this.seekTo = seekTo;
		this.maxPublishRecords = maxPublishRecords;
		this.producersCount = producersCount;
		this.bufferMemorySize = bufferMemorySize;
		this.maxRequestSize = maxRequestSize;
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

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	public String getSeekTo() {
		return seekTo;
	}

	public void setSeekTo(String seekTo) {
		this.seekTo = seekTo;
	}

	public int getMaxPublishRecords() {
		return maxPublishRecords;
	}

	public void setMaxPublishRecords(int maxPublishRecords) {
		this.maxPublishRecords = maxPublishRecords;
	}

	public int getProducersCount() {
		return producersCount;
	}

	public void setProducersCount(int producersCount) {
		this.producersCount = producersCount;
	}

	public int getBufferMemorySize() {
		return bufferMemorySize;
	}

	public void setBufferMemorySize(int bufferMemorySize) {
		this.bufferMemorySize = bufferMemorySize;
	}

	public int getMaxRequestSize() {
		return maxRequestSize;
	}

	public void setMaxRequestSize(int maxRequestSize) {
		this.maxRequestSize = maxRequestSize;
	}

	public String getConnectionURI() {
		if (isNullOrEmpty(getTopic()) || isNullOrEmpty(getServerUrl()) || isNullOrEmpty(getGroupId()) ||
			getServerPort() == 0 || getMaxPublishRecords() == 0 || getProducersCount() == 0 || 
			isNullOrEmpty(getSeekTo()) || getBufferMemorySize() == 0 || getMaxRequestSize() == 0) {
			throw new IllegalArgumentException("Missing configuration for Kafka DataSink. Please check that all required parameters are provided.");
		}
	
		return "kafka:" + getTopic() + "?brokers=" + getServerUrl() + ":" + getServerPort()
			+ "&seekTo=" + getSeekTo()
			+ "&groupId=" + getGroupId()
			+ "&maxPublishRecords=" + getMaxPublishRecords()
			+ "&producersCount=" + getProducersCount()
			+ "&bufferMemorySize=" + getBufferMemorySize()
			+ "&maxRequestSize=" + getMaxRequestSize();

	}
	
	private boolean isNullOrEmpty(String str) {
		return str == null || str.trim().isEmpty();
	}
}
