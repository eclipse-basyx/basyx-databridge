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
package org.eclipse.digitaltwin.basyx.databridge.examples.sqlaas;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class creates a SQLite Database and with a table "data" with two
 * columns "timestamp" and "value". It provides a Methods to periodically insert
 * increasing values with a timestamp and to stop the insertion.
 * 
 * To ensure, created artifacts are deleted properly after usage, this class
 * should be instantiated within a try with resources block
 * 
 * @author jungjan
 */
public class TestDb implements AutoCloseable {
	public static final String TABLE_NAME = "data";
	public static final String COLUMN_TIMESTAMP = "timestamp";
	public static final String COLUMN_VALUE = "value";
	
	private final String DB_PATH;
	private final String DB_URL;

	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	private int nextValue = 0;

	public TestDb(String dbPath) {
		DB_PATH = dbPath;
		DB_URL = "jdbc:sqlite:" + DB_PATH;
		initializeDatabase();
	}

	
	public String getDbPath() {
		return DB_PATH;
	}
	
	public String getDbUrl() {
		return DB_URL;
	}

	private void initializeDatabase() {
		try (Connection conn = DriverManager.getConnection(DB_URL)) {
			if (conn != null) {
				Statement stmt = conn.createStatement();
				dropTablesIfExist(stmt);
				createTestTable(stmt);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void dropTablesIfExist(Statement stmt) throws SQLException {
		stmt.execute(String.format("DROP TABLE IF EXISTS %s", TABLE_NAME));
	}

	private void createTestTable(Statement stmt) throws SQLException {
		stmt.execute(String.format("CREATE TABLE %s(%s TEXT, %s INTEGER)", TABLE_NAME, COLUMN_TIMESTAMP, COLUMN_VALUE));
	}

	public void startInsertingValues(int intervalSeconds) {
		scheduler.scheduleAtFixedRate(() -> {
			try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement pstmt = conn.prepareStatement(String.format("INSERT INTO %s(%s, %s) VALUES(?, ?)", TABLE_NAME, COLUMN_TIMESTAMP, COLUMN_VALUE))) {
				String timestamp = Instant.now()
						.toString();
				pstmt.setString(1, timestamp);
				pstmt.setInt(2, nextValue++);
				pstmt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}, 0, intervalSeconds, TimeUnit.SECONDS);
	}

	public void stopInsertingValues() {
		scheduler.shutdown();
		try {
			if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
				scheduler.shutdownNow();
			}
		} catch (InterruptedException e) {
			scheduler.shutdownNow();
		}
	}

	@Override
	public void close() throws Exception {
		File dbFile = new File("DB_PATH");
		if (dbFile.exists() && !dbFile.delete()) {
			System.out.println("Failed to delete database file.");
		}
	}
}