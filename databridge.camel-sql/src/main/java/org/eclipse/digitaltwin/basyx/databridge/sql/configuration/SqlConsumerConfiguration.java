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

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.camel.CamelContext;
import org.eclipse.digitaltwin.basyx.databridge.core.configuration.entity.DataSourceConfiguration;
import org.mariadb.jdbc.MariaDbDataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.sqlite.SQLiteDataSource;

/**
 * @author jungjan
 */
public class SqlConsumerConfiguration extends DataSourceConfiguration {
	public static final String DB_CONNECTION = "dbcn";
	
	private String db;
	private String dbName;
	private String user;
	private String password;
	private String query;

	public SqlConsumerConfiguration() {
		super();
	}

	public SqlConsumerConfiguration(String uniqueId, String serverUrl, int serverPort) {
		super(uniqueId, serverUrl, serverPort);
	}

	@Override
	public String getConnectionURI() {
		// addSqlDataSourceContext(DataBridgeComponent.camelContext());
		return String.format("sql:%s" + "?dataSource=#%s", getQuery(), DB_CONNECTION); 
	}
	
	private void addSqlDataSourceContext(CamelContext context) {
		switch (KnownDb.fromLabel(getDb())) {
		case mariaDB:
			try {
				configureMariaDbDataSource(context);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			break;
		case postgreSQL:
			configurePostgresqlDataSource(context);
			break;
		case SQLite:
			configureSqliteDataSource(context);
			break;
		default:
			throw new IllegalStateException("Unknown Database");
		}
	}
    

	private void configureMariaDbDataSource(CamelContext camelContext) throws SQLException {
        MariaDbDataSource dataSource = new MariaDbDataSource();
        dataSource.setUrl(buildJdbcUrl());
        dataSource.setUser(getUser());
        dataSource.setPassword(getPassword());
        bindSqlDataSource(camelContext, dataSource);
    }

    private void configurePostgresqlDataSource(CamelContext camelContext) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(buildJdbcUrl());
        dataSource.setUser(getUser());
        dataSource.setPassword(getPassword());
        bindSqlDataSource(camelContext, dataSource);
    }

    private void configureSqliteDataSource(CamelContext camelContext) {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(buildJdbcUrl());
        dataSource.setDatabaseName(getDbName());
        bindSqlDataSource(camelContext, dataSource);
    }
    
    private String buildJdbcUrl() {
    	if (!getDb().equalsIgnoreCase(KnownDb.SQLite.label)) {
    		return String.format("jdbc:%s://%s:%d/%s", getDb(), getServerUrl(), getServerPort(), getDbName());
    	} else {
    		return String.format("jdbc:%s:%s", getDb(), getServerUrl());
    	}
    }

	private void bindSqlDataSource(CamelContext camelContext, DataSource dataSource) {
		camelContext.getRegistry().bind(SqlConsumerConfiguration.DB_CONNECTION, dataSource);
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

}
