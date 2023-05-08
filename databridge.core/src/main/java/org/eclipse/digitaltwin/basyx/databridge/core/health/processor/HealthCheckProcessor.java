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
package org.eclipse.digitaltwin.basyx.databridge.core.health.processor;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.health.HealthCheck;
import org.apache.camel.health.HealthCheckHelper;
import org.apache.http.HttpStatus;
import org.eclipse.digitaltwin.basyx.databridge.core.health.dto.HealthCheckResultDTO;

/**
 * A processor class for processing the health status
 *
 * @author danish
 *
 */
public class HealthCheckProcessor {

	@Handler
	public void processHealthCheck(Exchange exchange) {
		Collection<HealthCheck.Result> healthCheckResults = HealthCheckHelper.invoke(exchange.getContext());

		if (!areAllServicesAndRoutesHealthy(healthCheckResults)) {
			exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, HttpStatus.SC_SERVICE_UNAVAILABLE);
		}

		List<HealthCheckResultDTO> healthCheckResultDTOs = prepareHealthCheckResults(healthCheckResults);

		exchange.getMessage().setBody(healthCheckResultDTOs);
	}

	private boolean areAllServicesAndRoutesHealthy(Collection<HealthCheck.Result> results) {
		return results.stream().allMatch(healthCheckResult -> healthCheckResult.getState() == HealthCheck.State.UP);
	}

	private List<HealthCheckResultDTO> prepareHealthCheckResults(Collection<HealthCheck.Result> healthCheckResults) {
		return healthCheckResults.stream().map(HealthCheckResultDTO::toDTO).collect(Collectors.toList());
	}
}
