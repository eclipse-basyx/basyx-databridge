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
package org.eclipse.digitaltwin.basyx.databridge.examples.aasjsonatahttp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.digitaltwin.basyx.databridge.examples.httpserver.DummyServlet;

/**
 * A customized DummyServlet
 * 
 * @author rana
 *
 */
public class Server extends DummyServlet {

	private static final long serialVersionUID = 4918478763760299634L;
	private String requestBodyValue = null;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		setAPIResponseProperty(resp);

		BufferedReader reader = req.getReader();
		StringBuilder requestBody = new StringBuilder();
		String line;

		while ((line = reader.readLine()) != null)
			requestBody.append(line);

		requestBodyValue = requestBody.toString();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		setAPIResponseProperty(resp);
		setMessageToResponse(resp);
	}

	/**
	 * Sets {@link HttpServletResponse} properties
	 * 
	 * @param resp
	 * @throws IOException
	 */
	private void setAPIResponseProperty(HttpServletResponse resp) throws IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
	}

	/**
	 * Sets retrieved message as a response
	 * 
	 * @param response
	 * @param resp
	 * @throws IOException
	 */
	private void setMessageToResponse(HttpServletResponse resp) throws IOException {

		PrintWriter out = resp.getWriter();
		out.print(requestBodyValue);
		out.flush();
		out.close();
	}
}
