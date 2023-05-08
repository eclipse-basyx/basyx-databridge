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
package org.eclipse.digitaltwin.basyx.databridge.core.health.dto;

import java.util.Map;
import org.apache.camel.health.HealthCheck;
import org.apache.camel.health.HealthCheck.Result;

/**
 * A data transfer object (DTO) for mapping required data elements from
 * HealthCheck.Result
 *
 * @author danish
 *
 */
public class HealthCheckResultDTO {

	private String message;
	private Throwable error;
	private Map<String, Object> details;
	private HealthCheck.State state;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Throwable getError() {
		return error;
	}

	public void setError(Throwable error) {
		this.error = error;
	}

	public Map<String, Object> getDetails() {
		return details;
	}

	public void setDetails(Map<String, Object> details) {
		this.details = details;
	}

	public HealthCheck.State getState() {
		return state;
	}

	public void setState(HealthCheck.State state) {
		this.state = state;
	}

	public static HealthCheckResultDTO toDTO(HealthCheck.Result result) {
		HealthCheckResultDTO healthCheckResultDTO = new HealthCheckResultDTO();
		healthCheckResultDTO.setMessage(getOptionalMessage(result));
		healthCheckResultDTO.setError(getOptionalError(result));
		healthCheckResultDTO.setDetails(result.getDetails());
		healthCheckResultDTO.setState(result.getState());

		return healthCheckResultDTO;
	}

	private static String getOptionalMessage(HealthCheck.Result result) {
		if (!result.getMessage().isPresent()) {
			return "";
		}

		return result.getMessage().get();
	}

	private static Throwable getOptionalError(Result result) {
		if (!result.getError().isPresent()) {
			return null;
		}

		return result.getError().get();
	}
}
