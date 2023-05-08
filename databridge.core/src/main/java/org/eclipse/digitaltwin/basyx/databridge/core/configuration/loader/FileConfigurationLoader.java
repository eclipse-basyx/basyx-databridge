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
package org.eclipse.digitaltwin.basyx.databridge.core.configuration.loader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.eclipse.digitaltwin.basyx.databridge.core.configuration.parser.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A core generic which can load the json configurations from a file
 * given in filepath to the corresponding mapper class
 *  
 * @author haque
 *
 */
public class FileConfigurationLoader {
	private static final Logger logger = LoggerFactory.getLogger(FileConfigurationLoader.class);
	private String filePath;
	private ClassLoader loader;
	private Class<?> mapperClass;
	
	/**
	 * An instance of {@link FileConfigurationLoader} which will load
	 * the jsons from fiven file path, to a mapper class.
	 * 
	 * The json file will be retrieved from the resource folder of given class loader
	 * 
	 * @param filePath
	 * @param loader
	 * @param mapperClass
	 */
	public FileConfigurationLoader(String filePath, ClassLoader loader, Class<?> mapperClass) {
		this.filePath = filePath;
		this.loader = loader;
		this.mapperClass = mapperClass;
	}
	
	/**
	 * Loads the json configuration from the file as a list of mapper object
	 * @return
	 */
	public Object loadListConfiguration() {

		try (Reader reader = getJsonReader()) {
			JsonParser parser = new JsonParser(mapperClass);
			return parser.getListConfiguration(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	/**
	 * Loads the json configuration from the file as a mapper object
	 * @return
	 */
	public Object loadConfiguration() {
		Reader reader = getJsonReader();
		JsonParser parser = new JsonParser(mapperClass);
		return parser.getConfiguration(reader);
	}
	
	/**
	 * Retrieves the input stream after loading the file from given 
	 * file path and the resource loader
	 * @return
	 */
	private InputStreamReader getJsonReader() {

		InputStream stream = null;
		try {
			stream = new FileInputStream(filePath);
		} catch (Exception e1) {
			logger.warn("No exterior config file found in defined path. Trying to load config file from classpath...");
			try {
				stream = loader.getResourceAsStream(filePath);
			} catch (Exception e2) {
				logger.error("No exterior config file found in defined path and no config file found in classpath");
				e2.printStackTrace();
			}
		}

		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(stream, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("Could not find the file");
			e.printStackTrace();
		}
		return reader;
	}
}
