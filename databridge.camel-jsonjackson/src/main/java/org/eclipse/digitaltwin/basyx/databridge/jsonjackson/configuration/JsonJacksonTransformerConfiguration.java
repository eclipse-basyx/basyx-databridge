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
package org.eclipse.digitaltwin.basyx.databridge.jsonjackson.configuration;

import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataTransformerConfiguration;

/**
 * An implementation of JsonJackson transformer configuration using camel
 * jsonjackson component
 * 
 * @author Daniele Rossi
 *
 */
public class JsonJacksonTransformerConfiguration extends DataTransformerConfiguration {
	private String operation;
	private String jacksonModules;

	public JsonJacksonTransformerConfiguration() {
	}

	public JsonJacksonTransformerConfiguration(String uniqueId, String operation) {
		super(uniqueId);
		this.operation = operation;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getJacksonModules() {
		return jacksonModules;
	}

	public void setJacksonModules(String jacksonModules) {
		this.jacksonModules = jacksonModules;
	}

	public String getConnectionURI() {
		String url = "dataformat:jackson:" + getOperation() + "?moduleClassNames=" + getJacksonModules();
		return url;
	}

}
