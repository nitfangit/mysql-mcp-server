package com.mysqlmcp.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MySQL数据库管理器
 * 负责数据库连接和基本操作
 */
public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    
    private String url;
    private String username;
    private String password;
    private Connection connection;

    public DatabaseManager() {
        // 从环境变量或系统属性读取配置
        this.url = System.getProperty("mysql.url", 
            System.getenv().getOrDefault("MYSQL_URL", "jdbc:mysql://localhost:3306/test"));
        this.username = System.getProperty("mysql.username", 
            System.getenv().getOrDefault("MYSQL_USERNAME", "root"));
        this.password = System.getProperty("mysql.password", 
            System.getenv().getOrDefault("MYSQL_PASSWORD", "root"));
    }

    public DatabaseManager(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * 获取数据库连接
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, username, password);
            logger.info("Connected to database: {}", url);
        }
        return connection;
    }

    /**
     * 执行查询SQL
     */
    public List<Map<String, Object>> executeQuery(String sql) throws SQLException {
        logger.debug("Executing query: {}", sql);
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);
            }
        }
        
        return results;
    }

    /**
     * 执行更新SQL（INSERT, UPDATE, DELETE）
     */
    public int executeUpdate(String sql) throws SQLException {
        logger.debug("Executing update: {}", sql);
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            return stmt.executeUpdate();
        }
    }

    /**
     * 执行更新SQL并返回生成的键
     */
    public Map<String, Object> executeUpdateWithKeys(String sql) throws SQLException {
        logger.debug("Executing update with keys: {}", sql);
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            int affectedRows = stmt.executeUpdate();
            Map<String, Object> result = new HashMap<>();
            result.put("affectedRows", affectedRows);
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    result.put("generatedKey", generatedKeys.getObject(1));
                }
            }
            
            return result;
        }
    }

    /**
     * 获取数据库表列表
     */
    public List<String> getTables() throws SQLException {
        return getTables(null);
    }

    /**
     * 获取指定数据库的表列表
     * @param databaseName 数据库名，如果为 null 则使用当前连接的数据库
     */
    public List<String> getTables(String databaseName) throws SQLException {
        logger.debug("Getting table list for database: {}", databaseName);
        List<String> tables = new ArrayList<>();
        
        try (Connection conn = getConnection();
             ResultSet rs = conn.getMetaData().getTables(databaseName, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        }
        
        return tables;
    }

    /**
     * 获取表的列信息
     */
    public List<Map<String, Object>> getTableColumns(String tableName) throws SQLException {
        logger.debug("Getting table columns: {}", tableName);
        List<Map<String, Object>> columns = new ArrayList<>();
        
        try (Connection conn = getConnection();
             ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                Map<String, Object> column = new HashMap<>();
                column.put("name", rs.getString("COLUMN_NAME"));
                column.put("type", rs.getString("TYPE_NAME"));
                column.put("size", rs.getInt("COLUMN_SIZE"));
                column.put("nullable", rs.getBoolean("NULLABLE"));
                column.put("defaultValue", rs.getString("COLUMN_DEF"));
                columns.add(column);
            }
        }
        
        return columns;
    }

    /**
     * 获取表的 CREATE TABLE DDL 语句
     * @param tableName 表名
     * @return CREATE TABLE 语句
     */
    public String getTableDDL(String tableName) throws SQLException {
        return getTableDDL(null, tableName);
    }

    /**
     * 获取指定数据库的表的 CREATE TABLE DDL 语句
     * @param databaseName 数据库名，如果为 null 则使用当前连接的数据库
     * @param tableName 表名
     * @return CREATE TABLE 语句
     */
    public String getTableDDL(String databaseName, String tableName) throws SQLException {
        logger.debug("Getting DDL for table: {}.{}", databaseName, tableName);
        
        String sql;
        if (databaseName != null && !databaseName.isEmpty()) {
            sql = "SHOW CREATE TABLE `" + databaseName + "`.`" + tableName + "`";
        } else {
            sql = "SHOW CREATE TABLE `" + tableName + "`";
        }
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                // SHOW CREATE TABLE 返回两列：Table 和 Create Table
                // 第二列包含完整的 CREATE TABLE 语句
                String ddl = rs.getString(2);
                return ddl;
            } else {
                throw new SQLException("Table not found: " + (databaseName != null ? databaseName + "." : "") + tableName);
            }
        }
    }

    /**
     * 关闭连接
     */
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            logger.info("Database connection closed");
        }
    }
}

