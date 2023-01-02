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
package basyx.components.databridge.integration;

import static org.junit.Assert.assertEquals;

import org.eclipse.basyx.aas.aggregator.api.IAASAggregator;
import org.eclipse.basyx.aas.aggregator.proxy.AASAggregatorProxy;
import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Integration test with OPCUA 
 *
 * @author danish
 */
public class ITTestUpdaterOpcua {
	protected static IIdentifier deviceAAS = new CustomId("TestUpdatedDeviceAAS");
	@Before
	public void setUp() throws Exception {
		System.out.println("Setting up env...");
	}

	@After
	public void tearDown() throws Exception {
		System.out.println("Tearing down env...");
	}

	@Test
	@Ignore
	public void test() throws Exception {
		Thread.sleep(15000);
		System.out.println("CHECK PROPERTY");
		checkProperty();
	}
	
	private void checkProperty() throws InterruptedException {
		IAASAggregator proxy = new AASAggregatorProxy("http://localhost:4001");
		
		IAssetAdministrationShell aas = proxy.getAAS(deviceAAS);
		ISubmodel sm = aas.getSubmodels().get("ConnectedSubmodel");
		
		ISubmodelElement updatedProp = sm.getSubmodelElement("ConnectedPropertyA");

		Object propValue = updatedProp.getValue();
		System.out.println("UpdatedPROPA: " + propValue);
		assertEquals("3.14", propValue);
		
		ISubmodelElement updatedProp2 = sm.getSubmodelElement("ConnectedPropertyB");
		Object propValue2 = updatedProp2.getValue();
		System.out.println("UpdatedPROPB: " + propValue2);
		assertEquals("32", propValue2);
	}
}
