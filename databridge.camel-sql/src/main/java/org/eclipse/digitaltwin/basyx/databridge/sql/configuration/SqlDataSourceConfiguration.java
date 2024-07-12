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
import org.mariadb.jdbc.MariaDbDataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.sqlite.SQLiteDataSource;

/**
 * Handles registration of DataSource components to the CamelContext
 * 
 * @author jungjan, mateusmolina
 */
public class SqlDataSourceConfiguration {

    private final String user;
    private final String password;
    private final String db;
    private final String dbName;
    private final String serverUrl;
    private final int serverPort;

    SqlDataSourceConfiguration(String user, String password, String db, String dbName, String serverUrl, int serverPort) {
        this.user = user;
        this.password = password;
        this.db = db;
        this.dbName = dbName;
        this.serverUrl = serverUrl;
        this.serverPort = serverPort;
    }

    public static SqlDataSourceConfiguration from(SqlConsumerConfiguration conf) {
        return new SqlDataSourceConfiguration(conf.getUser(), conf.getPassword(), conf.getDb(), conf.getDbName(), conf.getServerUrl(), conf.getServerPort());
    }

    public void registerSqlDataSource(CamelContext context) {
        switch (KnownDb.fromLabel(db)) {
        case MARIADB:
            try {
                configureMariaDbDataSource(context);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            break;
        case POSTGRESQL:
            configurePostgresqlDataSource(context);
            break;
        case SQLITE:
            configureSqliteDataSource(context);
            break;
        default:
            throw new IllegalStateException("Unknown Database");
        }
    }

    private void configureMariaDbDataSource(CamelContext camelContext) throws SQLException {
        MariaDbDataSource dataSource = new MariaDbDataSource();
        dataSource.setUrl(buildJdbcUrl());
        dataSource.setUser(user);
        dataSource.setPassword(password);
        bindSqlDataSource(camelContext, dataSource);
    }

    private void configurePostgresqlDataSource(CamelContext camelContext) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(buildJdbcUrl());
        dataSource.setUser(user);
        dataSource.setPassword(password);
        bindSqlDataSource(camelContext, dataSource);
    }

    private void configureSqliteDataSource(CamelContext camelContext) {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(buildJdbcUrl());
        dataSource.setDatabaseName(dbName);
        bindSqlDataSource(camelContext, dataSource);
    }

    private String buildJdbcUrl() {
        if (!db.equalsIgnoreCase(KnownDb.SQLITE.label)) {
            return String.format("jdbc:%s://%s:%d/%s", db, serverUrl, serverPort, dbName);
        } else {
            return String.format("jdbc:%s:%s", db, serverUrl);
        }
    }

    private void bindSqlDataSource(CamelContext camelContext, DataSource dataSource) {
        camelContext.getRegistry().bind(SqlConsumerConfiguration.DB_CONNECTION, dataSource);
    }

}
