/*******************************************************************************
 * Copyright (C) 2022 the Eclipse BaSyx Authors
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
package org.eclipse.digitaltwin.basyx.databridge.component.prometheus.configuration;

import com.bdwise.prometheus.client.builder.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.digitaltwin.basyx.databridge.component.httppolling.configuration.HttpPollingConsumerConfiguration;


/**
 * An implementation of prometheus consumer configuration
 * @author n14s - Niklas Mertens
 *
 */
public class PrometheusConsumerConfiguration extends HttpPollingConsumerConfiguration {
	Map<String, String> queryMap = new HashMap<>();



	public PrometheusConsumerConfiguration() {
		addQueryMapEntries();
	}
	
	public PrometheusConsumerConfiguration(String uniqueId, String serverUrl, int serverPort, String query) {
		super(uniqueId, serverUrl, serverPort, query);
		addQueryMapEntries();
	}

	public String getConnectionURI() {
		InstantQueryBuilder iqb = QueryBuilderType.InstantQuery.newInstance(getServerUrl() + ":" + getServerPort());
		URI targetUri = iqb.withQuery(queryMap.get(getQuery())).build();
		System.out.println(targetUri.toString());
		return targetUri.toString();
	}

	private void addQueryMapEntries(){

		queryMap.put("cpu-arch", "node_uname_info");
		queryMap.put("cpu-cores", "count(count(node_cpu_seconds_total) by (cpu))");
		queryMap.put("cpu-usage", "(((count(count(node_cpu_seconds_total) by (cpu))) - avg(sum by (mode)(irate(node_cpu_seconds_total{mode='idle'}[1m])))) * 100) / count(count(node_cpu_seconds_total) by (cpu))");
		queryMap.put("system-uptime", "node_time_seconds - node_boot_time_seconds");
		queryMap.put("ram-installed", "node_memory_MemTotal_bytes");
		queryMap.put("ram-usage", "100 - ((node_memory_MemAvailable_bytes * 100) / node_memory_MemTotal_bytes)");
		queryMap.put("rootfs-type", "node_filesystem_size_bytes{mountpoint=\"/\"}");
		queryMap.put("rootfs-usage", "100 - ((node_filesystem_avail_bytes{mountpoint=\"/\",fstype!=\"rootfs\"} * 100) / node_filesystem_size_bytes{mountpoint=\"/\",fstype!=\"rootfs\"})");
		queryMap.put("docker-version", "cadvisor_version_info");
		queryMap.put("docker-runningContainers", "irate(container_network_transmit_bytes_total{image!=\"\"}[1m])");
	}
}
