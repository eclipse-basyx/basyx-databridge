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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.digitaltwin.basyx.databridge.component.RoutesConfigurationLoader;

/**
 * Provides test environment variables corresponding to the activeMQ integration
 * config files
 * 
 * @author schnicke
 *
 */
public class RoutesConfigurationTestEnvironmentVariables {
	public static Map<String, String> get() {
		Map<String, String> variables = new HashMap<>();
		putRoutes(variables);
		
		putAasServer(variables);
		
		putJSONataTransformers(variables);
		putJSONataTransformersFiles(variables);

		putActiveMQ(variables);
		
		return variables;
	}

	private static void putJSONataTransformersFiles(Map<String, String> variables) {
		variables.put(RoutesConfigurationLoader.JSONATA_ENV_VAR_NAME, "[\"jsonataA.json\", \"jsonataB.json\"]");
	}

	private static void putActiveMQ(Map<String, String> variables) {
		// Here an underscore is used instead of a "." to enable tests for support for both variants 
		variables.put("activemqconsumer_json", "[\r\n"
				+ "	{\r\n"
				+ "		\"uniqueId\": \"property1\",\r\n"
				+ "		\"serverUrl\": \"127.0.0.1\",\r\n"
				+ "		\"serverPort\": 61616,\r\n"
				+ "		\"queue\": \"first-topic\"\r\n"
				+ "	},\r\n"
				+ "	{\r\n"
				+ "		\"uniqueId\": \"property2\",\r\n"
				+ "		\"serverUrl\": \"127.0.0.1\",\r\n"
				+ "		\"serverPort\": 9092,\r\n"
				+ "		\"queue\": \"second-topic\"\r\n"
				+ "	}\r\n"
				+ "]");
	}

	private static void putJSONataTransformers(Map<String, String> variables) {
		variables.put("jsonatatransformer.json", "[\r\n"
				+ "	{\r\n"
				+ "		\"uniqueId\": \"jsonataA\",\r\n"
				+ "		\"queryPath\": \"jsonataA.json\",\r\n"
				+ "		\"inputType\": \"JsonString\",\r\n"
				+ "		\"outputType\": \"JsonString\"\r\n"
				+ "	},\r\n"
				+ "	{\r\n"
				+ "		\"uniqueId\": \"jsonataB\",\r\n"
				+ "		\"queryPath\": \"jsonataB.json\",\r\n"
				+ "		\"inputType\": \"JsonString\",\r\n"
				+ "		\"outputType\": \"JsonString\"\r\n"
				+ "	}\r\n"
				+ "]");

		variables.put("jsonataA.json", "$sum(Account.Order.Product.(Price * Quantity))");
		variables.put("jsonataB.json", "Account.Order[0].Product[0].ProductID");
	}

	private static void putAasServer(Map<String, String> variables) {
		variables.put("aasserver.json", "[\r\n"
				+ "	{\r\n"
				+ "		\"uniqueId\": \"ConnectedSubmodel/ConnectedPropertyA\",\r\n"
				+ "		\"submodelEndpoint\": \"http://localhost:4001/shells/TestUpdatedDeviceAAS/aas/submodels/ConnectedSubmodel/submodel\",\r\n"
				+ "		\"idShortPath\": \"ConnectedPropertyA\"\r\n"
				+ "	},\r\n"
				+ "	{\r\n"
				+ "		\"uniqueId\": \"ConnectedSubmodel/ConnectedPropertyB\",\r\n"
				+ "		\"submodelEndpoint\": \"http://localhost:4001/shells/TestUpdatedDeviceAAS/aas/submodels/ConnectedSubmodel/submodel\",\r\n"
				+ "		\"idShortPath\": \"ConnectedPropertyB\"\r\n"
				+ "	}\r\n"
				+ "]");
	}

	private static void putRoutes(Map<String, String> variables) {
		variables.put("routes.json", "[\r\n"
				+ "	{\r\n"
				+ "		\"datasource\": \"property1\",\r\n"
				+ "		\"transformers\": [\"jsonataA\"],\r\n"
				+ "		\"datasinks\": [\"ConnectedSubmodel/ConnectedPropertyA\"],\r\n"
				+ "		\"trigger\": \"event\"\r\n"
				+ "	},\r\n"
				+ "	{\r\n"
				+ "		\"datasource\": \"property2\",\r\n"
				+ "		\"transformers\": [\"jsonataB\"],\r\n"
				+ "		\"datasinks\": [\"ConnectedSubmodel/ConnectedPropertyB\"],\r\n"
				+ "		\"trigger\": \"event\"\r\n"
				+ "	}\r\n"
				+ "]");
	}
}
