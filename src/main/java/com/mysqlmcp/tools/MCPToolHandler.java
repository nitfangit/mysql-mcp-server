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

        // 查询工具
        tools.add(createToolDefinition(
            "execute_query",
            "执行SQL查询语句",
            "执行SELECT查询并返回结果",
            new String[]{"sql"}
        ));

        // 更新工具
        tools.add(createToolDefinition(
            "execute_update",
            "执行SQL更新语句",
            "执行INSERT、UPDATE或DELETE语句",
            new String[]{"sql"}
        ));

        // 插入工具
        tools.add(createToolDefinition(
            "insert_data",
            "插入数据",
            "向指定表插入数据",
            new String[]{"table", "data"}
        ));

        // 更新数据工具
        tools.add(createToolDefinition(
            "update_data",
            "更新数据",
            "更新指定表中的数据",
            new String[]{"table", "data", "where"}
        ));

        // 删除数据工具
        tools.add(createToolDefinition(
            "delete_data",
            "删除数据",
            "从指定表中删除数据",
            new String[]{"table", "where"}
        ));

        // 获取表列表工具
        tools.add(createToolDefinition(
            "list_tables",
            "列出所有表",
            "获取数据库中的所有表名",
            new String[]{}
        ));

        // 获取表结构工具
        tools.add(createToolDefinition(
            "describe_table",
            "描述表结构",
            "获取指定表的列信息",
            new String[]{"table"}
        ));

        result.add("tools", tools);
        return result;
    }

    /**
     * 创建工具定义
     */
    private JsonObject createToolDefinition(String name, String description, String detailedDescription, String[] parameters) {
        JsonObject tool = new JsonObject();
        tool.addProperty("name", name);
        tool.addProperty("description", description);
        
        JsonObject inputSchema = new JsonObject();
        inputSchema.addProperty("type", "object");
        inputSchema.addProperty("description", detailedDescription);
        
        JsonObject properties = new JsonObject();
        JsonArray required = new JsonArray();
        
        for (String param : parameters) {
            JsonObject paramSchema = new JsonObject();
            switch (param) {
                case "sql":
                    paramSchema.addProperty("type", "string");
                    paramSchema.addProperty("description", "SQL语句");
                    break;
                case "table":
                    paramSchema.addProperty("type", "string");
                    paramSchema.addProperty("description", "表名");
                    break;
                case "data":
                    paramSchema.addProperty("type", "object");
                    paramSchema.addProperty("description", "要插入或更新的数据（键值对）");
                    break;
                case "where":
                    paramSchema.addProperty("type", "string");
                    paramSchema.addProperty("description", "WHERE条件（例如：id=1）");
                    break;
            }
            properties.add(param, paramSchema);
            required.add(param);
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
        result.addProperty("affectedRows", affectedRows);
        return result;
    }

    private JsonObject handleListTables(JsonObject arguments) throws SQLException {
        List<String> tables = databaseManager.getTables();
        
        JsonObject result = new JsonObject();
        JsonArray tableArray = new JsonArray();
        for (String table : tables) {
            tableArray.add(table);
        }
        result.add("tables", tableArray);
        result.addProperty("count", tables.size());
        return result;
    }

    private JsonObject handleDescribeTable(JsonObject arguments) throws SQLException {
        if (!arguments.has("table")) {
            throw new IllegalArgumentException("Missing parameter: table");
        }
        
        String table = arguments.get("table").getAsString();
        List<Map<String, Object>> columns = databaseManager.getTableColumns(table);
        
        JsonObject result = new JsonObject();
        JsonArray columnArray = new JsonArray();
        
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
        }
        
        result.add("columns", columnArray);
        return result;
    }

    private String escapeSql(String str) {
        return str.replace("'", "''").replace("\\", "\\\\");
    }
}

