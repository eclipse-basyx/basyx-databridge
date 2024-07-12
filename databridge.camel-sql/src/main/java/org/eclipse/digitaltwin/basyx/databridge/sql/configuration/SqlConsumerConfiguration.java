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
package org.eclipse.digitaltwin.basyx.databridge.sql.configuration;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataSourceConfiguration;

/**
 * @author jungjan, mateusmolina
 */
public class SqlConsumerConfiguration extends DataSourceConfiguration implements CamelContextAware {
	public static final String DB_CONNECTION = "dbcn";

	private String db;
	private String dbName;
	private String user;
	private String password;
	private String query;
	private CamelContext camelContext;

	public SqlConsumerConfiguration() {
		super();
	}

	public SqlConsumerConfiguration(String uniqueId, String serverUrl, int serverPort) {
		super(uniqueId, serverUrl, serverPort);
	}

	@Override
	public String getConnectionURI() {
		SqlDataSourceConfiguration.from(this).registerSqlDataSource(getCamelContext());
		return String.format("sql:%s" + "?dataSource=#%s&outputType=SelectList", getQuery(), DB_CONNECTION);
	}

	public String getDb() {
		return db;
	}

	public void setDb(String db) {
		this.db = db;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String dbUser) {
		this.user = dbUser;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String dbPassword) {
		this.password = dbPassword;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	@Override
	public CamelContext getCamelContext() {
		return camelContext;
	}

	@Override
	public void setCamelContext(CamelContext camelContext) {
		this.camelContext = camelContext;
	}

}
