package com.mysqlmcp.tools;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mysqlmcp.database.DatabaseManager;

/**
 * MCP工具处理器
 * 定义和实现所有可用的数据库操作工具
 */
public class MCPToolHandler {
    private static final Logger logger = LoggerFactory.getLogger(MCPToolHandler.class);
    private final DatabaseManager databaseManager;

    public MCPToolHandler(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * 获取所有可用工具的列表
     */
    public JsonObject getToolsList() {
        JsonObject result = new JsonObject();
        JsonArray tools = new JsonArray();

        // Query tool
        tools.add(createToolDefinition(
            "execute_query",
            "Execute SQL query",
            "Execute SELECT query and return results",
            new String[]{"sql"}
        ));

        // Update tool
        tools.add(createToolDefinition(
            "execute_update",
            "Execute SQL update",
            "Execute INSERT, UPDATE or DELETE statement",
            new String[]{"sql"}
        ));

        // Insert tool
        tools.add(createToolDefinition(
            "insert_data",
            "Insert data",
            "Insert data into specified table",
            new String[]{"table", "data"}
        ));

        // Update data tool
        tools.add(createToolDefinition(
            "update_data",
            "Update data",
            "Update data in specified table",
            new String[]{"table", "data", "where"}
        ));

        // Delete data tool
        tools.add(createToolDefinition(
            "delete_data",
            "Delete data",
            "Delete data from specified table",
            new String[]{"table", "where"}
        ));

        // List tables tool
        tools.add(createToolDefinition(
            "list_tables",
            "List all tables",
            "Get all table names in the database",
            new String[]{},
            new String[]{"database"}
        ));

        // Describe table tool
        tools.add(createToolDefinition(
            "describe_table",
            "Describe table structure",
            "Get column information for specified table",
            new String[]{"table"}
        ));

        // Get table DDL tool
        tools.add(createToolDefinition(
            "get_table_ddl",
            "Get table DDL",
            "Get CREATE TABLE statement for specified table",
            new String[]{"table"},
            new String[]{"database"}
        ));

        result.add("tools", tools);
        return result;
    }

    /**
     * 创建工具定义
     */
    private JsonObject createToolDefinition(String name, String description, String detailedDescription, String[] parameters) {
        return createToolDefinition(name, description, detailedDescription, parameters, new String[]{});
    }

    /**
     * 创建工具定义（支持可选参数）
     */
    private JsonObject createToolDefinition(String name, String description, String detailedDescription, 
                                           String[] requiredParams, String[] optionalParams) {
        JsonObject tool = new JsonObject();
        tool.addProperty("name", name);
        tool.addProperty("description", description);
        
        JsonObject inputSchema = new JsonObject();
        inputSchema.addProperty("type", "object");
        inputSchema.addProperty("description", detailedDescription);
        
        JsonObject properties = new JsonObject();
        JsonArray required = new JsonArray();
        
        // 添加必需参数
        for (String param : requiredParams) {
            JsonObject paramSchema = new JsonObject();
            switch (param) {
                case "sql":
                    paramSchema.addProperty("type", "string");
                    paramSchema.addProperty("description", "SQL statement");
                    break;
                case "table":
                    paramSchema.addProperty("type", "string");
                    paramSchema.addProperty("description", "Table name");
                    break;
                case "data":
                    paramSchema.addProperty("type", "object");
                    paramSchema.addProperty("description", "Data to insert or update (key-value pairs)");
                    break;
                case "where":
                    paramSchema.addProperty("type", "string");
                    paramSchema.addProperty("description", "WHERE condition (e.g., id=1)");
                    break;
                case "database":
                    paramSchema.addProperty("type", "string");
                    paramSchema.addProperty("description", "Database name (optional, uses current database if not specified)");
                    break;
            }
            properties.add(param, paramSchema);
            required.add(param);
        }
        
        // 添加可选参数
        for (String param : optionalParams) {
            JsonObject paramSchema = new JsonObject();
            switch (param) {
                case "database":
                    paramSchema.addProperty("type", "string");
                    paramSchema.addProperty("description", "Database name (optional, uses current database if not specified)");
                    break;
            }
            properties.add(param, paramSchema);
        }
        
        inputSchema.add("properties", properties);
        inputSchema.add("required", required);
        tool.add("inputSchema", inputSchema);
        
        return tool;
    }

    /**
     * 调用指定的工具
     */
    public JsonObject callTool(String toolName, JsonObject arguments) throws SQLException {
        logger.info("Calling tool: {}, arguments: {}", toolName, arguments);

        switch (toolName) {
            case "execute_query":
                return handleExecuteQuery(arguments);
            case "execute_update":
                return handleExecuteUpdate(arguments);
            case "insert_data":
                return handleInsertData(arguments);
            case "update_data":
                return handleUpdateData(arguments);
            case "delete_data":
                return handleDeleteData(arguments);
            case "list_tables":
                return handleListTables(arguments);
            case "describe_table":
                return handleDescribeTable(arguments);
            case "get_table_ddl":
                return handleGetTableDDL(arguments);
            default:
                throw new IllegalArgumentException("Unknown tool: " + toolName);
        }
    }

    private JsonObject handleExecuteQuery(JsonObject arguments) throws SQLException {
        if (!arguments.has("sql")) {
            throw new IllegalArgumentException("Missing parameter: sql");
        }
        
        String sql = arguments.get("sql").getAsString();
        List<Map<String, Object>> results = databaseManager.executeQuery(sql);
        
        JsonObject result = new JsonObject();
        JsonArray content = new JsonArray();
        JsonArray rows = new JsonArray();
        
        for (Map<String, Object> row : results) {
            JsonObject rowObj = new JsonObject();
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                Object value = entry.getValue();
                if (value == null) {
                    rowObj.add(entry.getKey(), null);
                } else if (value instanceof Number) {
                    rowObj.addProperty(entry.getKey(), (Number) value);
                } else if (value instanceof Boolean) {
                    rowObj.addProperty(entry.getKey(), (Boolean) value);
                } else {
                    rowObj.addProperty(entry.getKey(), value.toString());
                }
            }
            rows.add(rowObj);
        }
        
        // 按照 MCP 协议规范，添加 content 数组
        JsonObject textContent = new JsonObject();
        textContent.addProperty("type", "text");
        textContent.addProperty("text", "Query returned " + rows.size() + " row(s)");
        content.add(textContent);
        result.add("content", content);
        
        // 同时保留原始数据格式以便兼容
        result.add("rows", rows);
        result.addProperty("count", rows.size());
        return result;
    }

    private JsonObject handleExecuteUpdate(JsonObject arguments) throws SQLException {
        if (!arguments.has("sql")) {
            throw new IllegalArgumentException("Missing parameter: sql");
        }
        
        String sql = arguments.get("sql").getAsString();
        int affectedRows = databaseManager.executeUpdate(sql);
        
        JsonObject result = new JsonObject();
        JsonArray content = new JsonArray();
        
        // 按照 MCP 协议规范，添加 content 数组
        JsonObject textContent = new JsonObject();
        textContent.addProperty("type", "text");
        textContent.addProperty("text", "Update completed. Affected rows: " + affectedRows);
        content.add(textContent);
        result.add("content", content);
        
        // 同时保留原始数据格式以便兼容
        result.addProperty("affectedRows", affectedRows);
        return result;
    }

    private JsonObject handleInsertData(JsonObject arguments) throws SQLException {
        if (!arguments.has("table") || !arguments.has("data")) {
            throw new IllegalArgumentException("Missing parameter: table or data");
        }
        
        String table = arguments.get("table").getAsString();
        JsonObject data = arguments.getAsJsonObject("data");
        
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(table).append(" (");
        StringBuilder values = new StringBuilder(" VALUES (");
        boolean first = true;
        
        for (String key : data.keySet()) {
            if (!first) {
                sql.append(", ");
                values.append(", ");
            }
            sql.append(key);
            JsonElement value = data.get(key);
            if (value.isJsonNull()) {
                values.append("NULL");
            } else if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                values.append("'").append(escapeSql(value.getAsString())).append("'");
            } else {
                values.append(value.getAsString());
            }
            first = false;
        }
        
        sql.append(")").append(values).append(")");
        
        Map<String, Object> result = databaseManager.executeUpdateWithKeys(sql.toString());
        
        JsonObject jsonResult = new JsonObject();
        JsonArray content = new JsonArray();
        
        // 按照 MCP 协议规范，添加 content 数组
        JsonObject textContent = new JsonObject();
        textContent.addProperty("type", "text");
        StringBuilder text = new StringBuilder("Insert completed. Affected rows: " + result.get("affectedRows"));
        if (result.containsKey("generatedKey")) {
            text.append(", Generated key: ").append(result.get("generatedKey"));
        }
        textContent.addProperty("text", text.toString());
        content.add(textContent);
        jsonResult.add("content", content);
        
        // 同时保留原始数据格式以便兼容
        jsonResult.addProperty("affectedRows", (Integer) result.get("affectedRows"));
        if (result.containsKey("generatedKey")) {
            jsonResult.addProperty("generatedKey", result.get("generatedKey").toString());
        }
        return jsonResult;
    }

    private JsonObject handleUpdateData(JsonObject arguments) throws SQLException {
        if (!arguments.has("table") || !arguments.has("data") || !arguments.has("where")) {
            throw new IllegalArgumentException("Missing parameter: table, data or where");
        }
        
        String table = arguments.get("table").getAsString();
        JsonObject data = arguments.getAsJsonObject("data");
        String where = arguments.get("where").getAsString();
        
        StringBuilder sql = new StringBuilder("UPDATE ").append(table).append(" SET ");
        boolean first = true;
        
        for (String key : data.keySet()) {
            if (!first) {
                sql.append(", ");
            }
            sql.append(key).append(" = ");
            JsonElement value = data.get(key);
            if (value.isJsonNull()) {
                sql.append("NULL");
            } else if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                sql.append("'").append(escapeSql(value.getAsString())).append("'");
            } else {
                sql.append(value.getAsString());
            }
            first = false;
        }
        
        sql.append(" WHERE ").append(where);
        
        int affectedRows = databaseManager.executeUpdate(sql.toString());
        
        JsonObject result = new JsonObject();
        JsonArray content = new JsonArray();
        
        // 按照 MCP 协议规范，添加 content 数组
        JsonObject textContent = new JsonObject();
        textContent.addProperty("type", "text");
        textContent.addProperty("text", "Update completed. Affected rows: " + affectedRows);
        content.add(textContent);
        result.add("content", content);
        
        // 同时保留原始数据格式以便兼容
        result.addProperty("affectedRows", affectedRows);
        return result;
    }

    private JsonObject handleDeleteData(JsonObject arguments) throws SQLException {
        if (!arguments.has("table") || !arguments.has("where")) {
            throw new IllegalArgumentException("Missing parameter: table or where");
        }
        
        String table = arguments.get("table").getAsString();
        String where = arguments.get("where").getAsString();
        
        String sql = "DELETE FROM " + table + " WHERE " + where;
        int affectedRows = databaseManager.executeUpdate(sql);
        
        JsonObject result = new JsonObject();
        JsonArray content = new JsonArray();
        
        // 按照 MCP 协议规范，添加 content 数组
        JsonObject textContent = new JsonObject();
        textContent.addProperty("type", "text");
        textContent.addProperty("text", "Delete completed. Affected rows: " + affectedRows);
        content.add(textContent);
        result.add("content", content);
        
        // 同时保留原始数据格式以便兼容
        result.addProperty("affectedRows", affectedRows);
        return result;
    }

    private JsonObject handleListTables(JsonObject arguments) throws SQLException {
        String databaseName = arguments.has("database") ? arguments.get("database").getAsString() : null;
        List<String> tables = databaseManager.getTables(databaseName);
        
        // 按照 MCP 协议规范，工具调用响应应该包含 content 数组
        JsonObject result = new JsonObject();
        JsonArray content = new JsonArray();
        
        // 创建文本内容项
        JsonObject textContent = new JsonObject();
        textContent.addProperty("type", "text");
        
        // 将表列表格式化为文本
        String dbInfo = databaseName != null ? "Database '" + databaseName + "'" : "Current database";
        StringBuilder text = new StringBuilder(dbInfo + " tables (" + tables.size() + "):\n");
        JsonArray tableArray = new JsonArray();
        for (String table : tables) {
            tableArray.add(table);
            text.append("- ").append(table).append("\n");
        }
        textContent.addProperty("text", text.toString());
        content.add(textContent);
        
        result.add("content", content);
        // 同时保留原始数据格式以便兼容
        result.add("tables", tableArray);
        result.addProperty("count", tables.size());
        if (databaseName != null) {
            result.addProperty("database", databaseName);
        }
        return result;
    }

    private JsonObject handleDescribeTable(JsonObject arguments) throws SQLException {
        if (!arguments.has("table")) {
            throw new IllegalArgumentException("Missing parameter: table");
        }
        
        String table = arguments.get("table").getAsString();
        List<Map<String, Object>> columns = databaseManager.getTableColumns(table);
        
        JsonObject result = new JsonObject();
        JsonArray content = new JsonArray();
        JsonArray columnArray = new JsonArray();
        
        StringBuilder text = new StringBuilder("Table structure for '" + table + "' (" + columns.size() + " columns):\n");
        for (Map<String, Object> column : columns) {
            JsonObject colObj = new JsonObject();
            colObj.addProperty("name", (String) column.get("name"));
            colObj.addProperty("type", (String) column.get("type"));
            colObj.addProperty("size", (Integer) column.get("size"));
            colObj.addProperty("nullable", (Boolean) column.get("nullable"));
            if (column.get("defaultValue") != null) {
                colObj.addProperty("defaultValue", column.get("defaultValue").toString());
            }
            columnArray.add(colObj);
            
            // 添加到文本描述
            text.append("- ").append(column.get("name"))
                .append(" (").append(column.get("type"));
            if (column.get("size") != null) {
                text.append("(").append(column.get("size")).append(")");
            }
            text.append(")");
            if (!(Boolean) column.get("nullable")) {
                text.append(" NOT NULL");
            }
            if (column.get("defaultValue") != null) {
                text.append(" DEFAULT ").append(column.get("defaultValue"));
            }
            text.append("\n");
        }
        
        // 按照 MCP 协议规范，添加 content 数组
        JsonObject textContent = new JsonObject();
        textContent.addProperty("type", "text");
        textContent.addProperty("text", text.toString());
        content.add(textContent);
        result.add("content", content);
        
        // 同时保留原始数据格式以便兼容
        result.add("columns", columnArray);
        return result;
    }

    private JsonObject handleGetTableDDL(JsonObject arguments) throws SQLException {
        if (!arguments.has("table")) {
            throw new IllegalArgumentException("Missing parameter: table");
        }
        
        String tableName = arguments.get("table").getAsString();
        String databaseName = arguments.has("database") ? arguments.get("database").getAsString() : null;
        
        String ddl = databaseManager.getTableDDL(databaseName, tableName);
        
        JsonObject result = new JsonObject();
        JsonArray content = new JsonArray();
        
        // 按照 MCP 协议规范，添加 content 数组
        JsonObject textContent = new JsonObject();
        textContent.addProperty("type", "text");
        textContent.addProperty("text", ddl);
        content.add(textContent);
        result.add("content", content);
        
        // 同时保留原始数据格式以便兼容
        result.addProperty("ddl", ddl);
        result.addProperty("table", tableName);
        if (databaseName != null) {
            result.addProperty("database", databaseName);
        }
        
        return result;
    }

    private String escapeSql(String str) {
        return str.replace("'", "''").replace("\\", "\\\\");
    }
}

