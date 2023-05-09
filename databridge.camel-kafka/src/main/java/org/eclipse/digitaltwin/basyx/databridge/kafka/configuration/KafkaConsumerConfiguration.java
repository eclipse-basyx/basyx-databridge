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
package org.eclipse.digitaltwin.basyx.databridge.kafka.configuration;

import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataSourceConfiguration;

/**
 * An implementation of kafka consumer configuration
 * @author haque
 *
 */
public class KafkaConsumerConfiguration extends DataSourceConfiguration {
	private String topic;
	private int maxPollRecords;
	private String groupId;
	private int consumersCount;
	private String seekTo;
	
	public KafkaConsumerConfiguration() {}
	
	public KafkaConsumerConfiguration(String uniqueId, String serverUrl, int serverPort, String topic,
			int maxPollRecords, String groupId, int consumersCount, String seekTo) {
		super(uniqueId, serverUrl, serverPort);
		this.topic = topic;
		this.maxPollRecords = maxPollRecords;
		this.groupId = groupId;
		this.consumersCount = consumersCount;
		this.seekTo = seekTo;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public int getMaxPollRecords() {
		return maxPollRecords;
	}

	public void setMaxPollRecords(int maxPollRecords) {
		this.maxPollRecords = maxPollRecords;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public int getConsumersCount() {
		return consumersCount;
	}

	public void setConsumersCount(int consumersCount) {
		this.consumersCount = consumersCount;
	}

	public String getSeekTo() {
		return seekTo;
	}

	public void setSeekTo(String seekTo) {
		this.seekTo = seekTo;
	}

	public String getConnectionURI() {
		return 
		"kafka:" + getTopic() + "?brokers=" + getServerUrl() + ":" + getServerPort()
		+ "&maxPollRecords=" + getMaxPollRecords()
        + "&consumersCount=" + getConsumersCount()
        + "&seekTo=" + getSeekTo()
        + "&groupId="  + getGroupId();
	}
}
