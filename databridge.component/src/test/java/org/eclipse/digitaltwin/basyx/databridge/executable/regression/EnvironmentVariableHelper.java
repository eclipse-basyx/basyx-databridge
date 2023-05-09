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

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

/**
 * Enables test cases to work with environment variables
 * 
 * @author schnicke
 *
 */
public class EnvironmentVariableHelper {
	public static void setEnvironmentVariablesForTesting(Map<String, String> newenv) {
		try {
			Class<?> processEnvironmentClass = getProcessEnvironmentClass();
			Field theEnvironment = getAccessibleField(processEnvironmentClass, "theEnvironment");
			setNewEnvironmentVariables(theEnvironment, newenv);
			Field theCaseInsensitiveEnvironmentField = getAccessibleField(processEnvironmentClass, "theCaseInsensitiveEnvironment");
			setNewEnvironmentVariables(theCaseInsensitiveEnvironmentField, newenv);
		} catch (NoSuchFieldException e) {
			setVariableToUnmodifiableMap(newenv);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Class<?> getProcessEnvironmentClass() throws ClassNotFoundException {
		Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
		return processEnvironmentClass;
	}

	private static Field getAccessibleField(Class<?> processEnvironmentClass, String fieldName) throws NoSuchFieldException, SecurityException {
		Field theEnvironmentField = processEnvironmentClass.getDeclaredField(fieldName);
		theEnvironmentField.setAccessible(true);
		return theEnvironmentField;
	}

	private static void setNewEnvironmentVariables(Field field, Map<String, String> newenv) throws IllegalArgumentException, IllegalAccessException {
		@SuppressWarnings("unchecked")
		Map<String, String> env = (Map<String, String>) field.get(null);
		env.clear();
		env.putAll(newenv);
	}

	private static void setVariableToUnmodifiableMap(Map<String, String> newenv) {
		try {
			Class<?>[] classes = Collections.class.getDeclaredClasses();
			Map<String, String> currentEnvironmentVariables = System.getenv();
			for (Class<?> cl : classes) {
				if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
					Field field = cl.getDeclaredField("m");
					field.setAccessible(true);
					Object obj = field.get(currentEnvironmentVariables);
					@SuppressWarnings("unchecked")
					Map<String, String> map = (Map<String, String>) obj;
					map.clear();
					map.putAll(newenv);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
