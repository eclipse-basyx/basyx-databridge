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
package org.eclipse.digitaltwin.basyx.databridge.component;

import org.eclipse.digitaltwin.basyx.databridge.core.component.DataBridgeComponent;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.route.core.RoutesConfiguration;

/**
 * Starts the stand-alone databridge component
 *
 * @author danish
 */
public class DataBridgeExecutable {

	private static final String DEFAULT_CONFIG_PATH = "/usr/share/config";
	private static DataBridgeComponent dataBridgeComponent;
	
	public static void main(String[] args) throws IllegalArgumentException, SecurityException {
		String configPath = getConfigPath(args);
		
		RoutesConfigurationLoader routesConfigurationLoader = new RoutesConfigurationLoader(configPath);

		RoutesConfiguration config = routesConfigurationLoader.create();

		dataBridgeComponent = new DataBridgeComponent(config);
		dataBridgeComponent.startComponent();
	}

	private static String getConfigPath(String[] args) {
		if (args.length == 0) {
			return DEFAULT_CONFIG_PATH;
		} else {
			return args[0];
		}
	}

	public static DataBridgeComponent getDataBridgeComponent() {
		return dataBridgeComponent;
	}
}
