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
package org.eclipse.digitaltwin.basyx.databridge.core.configuration.parser;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * A generic core implementation of Json message parser-
 * Parses the json message according to the given mapper Class
 * 
 * @author haque
 *
 */
public class JsonParser {
	private Gson gson;
	Class<?> mapperClass;
	
	/**
	 * Builds a JsonParser with a class in which the json will 
	 * be mapped into
	 * @param mapperClass
	 */
	public JsonParser(Class<?> mapperClass) {
		gson = new Gson();
		this.mapperClass = mapperClass;
	}
	
	/**
	 * Gets a list of mapper class objects from the json stream reader
	 * @param reader
	 * @return
	 */
	public Object getListConfiguration(Reader reader) {
		Type listType = TypeToken.getParameterized(ArrayList.class, mapperClass).getType(); 
		return gson.fromJson(reader, listType); 
	}
	
	/**
	 * Gets a mapper class object from the json stream reader
	 * @param reader
	 * @return
	 */
	public Object getConfiguration(Reader reader) {
		Type type = TypeToken.getParameterized(mapperClass).getType();
		return gson.fromJson(reader, type); 
	}
}
