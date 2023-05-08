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


package org.eclipse.digitaltwin.basyx.databridge.executable.regression;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.usage.SystemUsage;
import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;

public class DataBridgeActiveMQTestHelper {
	public static BrokerService startActiveMQBroker(String brokerUrl) {
		try {
			BrokerService activeMQBroker = new BrokerService();
			activeMQBroker.addConnector(brokerUrl);
			activeMQBroker.setPersistent(false);

			setSystemUsageLimit(activeMQBroker);

			activeMQBroker.start();

			return activeMQBroker;
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

	private static void setSystemUsageLimit(BrokerService activeMQBroker) {
		SystemUsage systemUsage = activeMQBroker.getSystemUsage();
		systemUsage.getStoreUsage().setLimit(1024 * 1024 * 8);
		systemUsage.getTempUsage().setLimit(1024 * 1024 * 8);
	}

	public static AASServerComponent configureAndStartAASServer() {
		BaSyxContextConfiguration aasContextConfig = new BaSyxContextConfiguration(4001, "");

		BaSyxAASServerConfiguration aasConfig = new BaSyxAASServerConfiguration(AASServerBackend.INMEMORY, "aasx/updatertest.aasx");

		AASServerComponent aasServer = new AASServerComponent(aasContextConfig, aasConfig);
		aasServer.startComponent();

		return aasServer;
	}

}
