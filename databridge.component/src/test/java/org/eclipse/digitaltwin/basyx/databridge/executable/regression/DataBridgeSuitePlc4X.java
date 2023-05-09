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

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.eclipse.basyx.aas.aggregator.api.IAASAggregator;
import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.junit.Test;

/**
 * Suite for testing that the DataBridge is setup correctly with PLC4X
 * 
 * @author danish
 *
 */
public abstract class DataBridgeSuitePlc4X {
	protected static IIdentifier deviceAAS = new CustomId("TestUpdatedDeviceAAS");

	protected abstract IAASAggregator getAASAggregatorProxy();

	@Test
	public void getUpdatedPropertyValue() throws Exception {
		String expectedValue = wrapStringValue("251");

		assertPropertyValue(expectedValue);
	}

	private void assertPropertyValue(String expectedValue) {
		Awaitility.await().with().pollInterval(2, TimeUnit.SECONDS).atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> assertEquals(expectedValue, fetchPropertyValue()));
	}

	private String fetchPropertyValue() throws InterruptedException {
		IAASAggregator proxy = getAASAggregatorProxy();

		IAssetAdministrationShell aas = proxy.getAAS(deviceAAS);
		ISubmodel sm = aas.getSubmodels().get("ConnectedSubmodel");

		ISubmodelElement updatedProp = sm.getSubmodelElement("ConnectedPropertyA");

		return (String) updatedProp.getValue();
	}
	
	private String wrapStringValue(String value) {
		return "\"" + value + "\"";
	}
}
