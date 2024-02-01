/*******************************************************************************
 * Copyright (C) 2024 the Eclipse BaSyx Authors
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

package org.eclipse.digitaltwin.basyx.databridge.aas.util;

/**
 * An utility class for the AAS Component
 * 
 * @author danish
 */
public class AASComponentUtil {

	private AASComponentUtil() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * A convenient method to wrap the content as String
	 * 
	 * e.g.,
	 * 
	 * <pre>
	 * 72 -> "72"
	 * 56.23 -> "56.23"
	 * "example" -> "example"
	 * "example -> RuntimeException (Malformed from the right)
	 * example" -> RuntimeException (Malformed from the left)
	 * 
	 * </pre>
	 * 
	 * @param content
	 * @return
	 */
	public static String wrapContent(String content) {

		if (content == null || content.isEmpty())
			return "";

		if (isAlreadyWrapped(content))
			return content;

		throwExceptionIfMalformedWrapping(content);

		return wrapStringValue(content);
	}
	
	/**
	 * Checks whether the provided string value is already wrapped in quotes ("") or not
	 * 
	 * e.g.,
	 * 
	 * <pre>
	 * 56.23 -> false
	 * "example" -> true
	 * example -> false
	 * 
	 * </pre>
	 * 
	 * @param content
	 * @return
	 */
	public static boolean isAlreadyWrapped(String content) {
		return content.startsWith("\"") && content.endsWith("\"");
	}

	private static String wrapStringValue(String content) {
		if (content.isEmpty())
			return content;

		return "\"" + content + "\"";
	}

	private static void throwExceptionIfMalformedWrapping(String content) {

		if (isRightMalformed(content) || isLeftMalformed(content))
			throw new RuntimeException("The content's: " + content + " formatting is malformed.");
	}

	private static boolean isLeftMalformed(String content) {
		return !content.startsWith("\"") && content.endsWith("\"");
	}

	private static boolean isRightMalformed(String content) {
		return content.startsWith("\"") && !content.endsWith("\"");
	}

}
