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
package org.eclipse.digitaltwin.basyx.databridge.jsonata.configuration;

import java.io.File;

import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataTransformerConfiguration;

/**
 * An implementation of Jsonata transformer configuration
 * using camel jsonata component
 * 
 * @author haque
 *
 */
public class JsonataTransformerConfiguration extends DataTransformerConfiguration {
	private String queryPath;
	private String inputType;
	private String outputType;
	
	public JsonataTransformerConfiguration() {}
	
	public JsonataTransformerConfiguration(String uniqueId, String queryPath, String inputType, String outputType) {
		super(uniqueId);
		this.queryPath = queryPath;
		this.inputType = inputType;
		this.outputType = outputType;
	}

	public String getQueryPath() {
		return queryPath;
	}

	public void setQueryPath(String queryPath) {
		this.queryPath = queryPath;
	}

	@Override
	public String getConnectionURI() {
		File jsonataFile = new File(getQueryPath());
		if (jsonataFile.exists()) {
			return "jsonata:file:" + getQueryPath() + "?inputType=" + getInputType() + "&outputType=" + getOutputType();
		} else {
			return "jsonata:" + getQueryPath() + "?inputType=" + getInputType() + "&outputType=" + getOutputType();
		}
	}

	public String getInputType() {
		return inputType;
	}

	public void setInputType(String inputType) {
		this.inputType = inputType;
	}

	public String getOutputType() {
		return outputType;
	}

	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}
}
