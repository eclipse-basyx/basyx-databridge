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
package org.eclipse.digitaltwin.basyx.databridge.plc4x.configuration.factory;

import org.eclipse.digitaltwin.basyx.databridge.core.configuration.factory.DataSourceConfigurationFactory;
import org.eclipse.digitaltwin.basyx.databridge.plc4x.configuration.Plc4XConsumerConfiguration;

/**
 * A default configuration factory for PLC4X from a default file path
 * 
 * @author danish
 *
 */
public class Plc4XDefaultConfigurationFactory extends DataSourceConfigurationFactory {
	public static final String DEFAULT_FILE_PATH = "plc4xconsumer.json";
	
	public Plc4XDefaultConfigurationFactory(ClassLoader loader) {
		super(DEFAULT_FILE_PATH, loader, Plc4XConsumerConfiguration.class);
	}
	
	public Plc4XDefaultConfigurationFactory(String filePath, ClassLoader loader) {
		super(filePath, loader, Plc4XConsumerConfiguration.class);
	}
}
