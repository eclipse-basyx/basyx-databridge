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
package org.eclipse.digitaltwin.basyx.databridge.examples.httpserver;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class exposes an Http API endpoint according to given
 * host, port, path.
 * @author haque
 *
 */
public class HttpServer {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);
	
    private Tomcat tomcat;
	
    public HttpServer(int port, String host, String path, DummyServlet servlet) {
    	initializeServer(port, host, path, servlet);
	}
    
    /**
     * Initializes a tomcat server based on given properties
     * @param port
     * @param host
     * @param path
     * @param servlet
     */
    public void initializeServer(int port, String host, String path, DummyServlet servlet) {
    	tomcat = new Tomcat();
		tomcat.setPort(port);
		tomcat.setHostname(host);
		String appBase = ".";
		tomcat.getHost().setAppBase(appBase);
		File docBase = new File(System.getProperty("java.io.tmpdir"));
		Context context = tomcat.addContext("", docBase.getAbsolutePath());

		Tomcat.addServlet(
		  context, Integer.toString(servlet.hashCode()), servlet);
		context.addServletMappingDecoded(
		  path + "/*", Integer.toString(servlet.hashCode()));
    }
    
    /**
     * Starts the tomcat server
     */
    public void start() {
    	try {
    		tomcat.start();
    	} catch (Exception e) {
			LOGGER.error(e.getMessage());
			e.printStackTrace();
		}
    }
    
    public void stop() {
    	try {
			tomcat.stop();
		} catch (LifecycleException e) {
			LOGGER.error(e.getMessage());
			e.printStackTrace();
		}
    }
}
