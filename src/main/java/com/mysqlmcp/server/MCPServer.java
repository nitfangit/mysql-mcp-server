package com.mysqlmcp.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mysqlmcp.database.DatabaseManager;
import com.mysqlmcp.tools.MCPToolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * MCP服务器主类
 * 处理JSON-RPC请求并调用相应的工具
 */
public class MCPServer {
    private static final Logger logger = LoggerFactory.getLogger(MCPServer.class);
    private static final Gson gson = new Gson();
    private final DatabaseManager databaseManager;
    private final MCPToolHandler toolHandler;

    public MCPServer() {
        this.databaseManager = new DatabaseManager();
        this.toolHandler = new MCPToolHandler(databaseManager);
    }

    public static void main(String[] args) {
        // 设置系统属性确保使用 UTF-8 编码
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("user.language", "en");
        System.setProperty("user.country", "US");
        
        MCPServer server = new MCPServer();
        server.start();
    }

    public void start() {
        logger.info("MySQL MCP Server starting...");
        
        // 使用 stderr 进行调试输出（不会干扰 stdout 的 JSON-RPC 通信）
        PrintStream err;
        try {
            err = new PrintStream(System.err, true, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            err = System.err; // Fallback to default
        }
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true)) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    JsonObject request = JsonParser.parseString(line).getAsJsonObject();
                    String method = request.has("method") ? request.get("method").getAsString() : null;
                    Object id = request.has("id") ? request.get("id") : null;
                    
                    // 检查是否是通知（没有 id 或 id 为 null）
                    boolean isNotification = (id == null || 
                        (request.has("id") && request.get("id").isJsonNull()));
                    
                    err.println("[DEBUG] Received " + (isNotification ? "notification" : "request") + 
                               ": method=" + method + ", id=" + id);
                    
                    // 如果是通知，只处理不响应
                    if (isNotification) {
                        handleNotification(request, method);
                    } else {
                        // 立即处理请求并发送响应，不要有任何延迟
                        JsonObject response = handleRequest(request);
                        String responseJson = gson.toJson(response);
                        
                        err.println("[DEBUG] Sending response: " + responseJson);
                        // 立即发送响应，确保客户端能收到
                        writer.print(responseJson);
                        writer.print('\n');
                        writer.flush();
                        // 强制刷新 System.out
                        System.out.flush();
                    }
                    
                    // 注意：根据 MCP 协议，initialized 通知是可选的
                    // 某些客户端可能不需要或不支持此通知
                    // 如果客户端仍然无法识别服务器，可以尝试不发送此通知
                    // if ("initialize".equals(method)) {
                    //     JsonObject initializedNotification = createNotification("initialized", null);
                    //     String notificationJson = gson.toJson(initializedNotification);
                    //     err.println("[DEBUG] Sending initialized notification: " + notificationJson);
                    //     writer.println(notificationJson);
                    //     writer.flush();
                    // }
                } catch (Exception e) {
                    logger.error("Error processing request", e);
                    err.println("[DEBUG] Error: " + e.getMessage());
                    e.printStackTrace(err);
                    
                    Object requestId = null;
                    try {
                        if (line != null) {
                            JsonObject req = JsonParser.parseString(line).getAsJsonObject();
                            if (req.has("id")) {
                                requestId = req.get("id");
                            }
                        }
                    } catch (Exception ignored) {
                        // Ignore parsing errors when creating error response
                    }
                    JsonObject errorResponse = createErrorResponse(requestId, -32603, "Internal error: " + e.getMessage());
                    String errorJson = gson.toJson(errorResponse);
                    err.println("[DEBUG] Sending error response: " + errorJson);
                    writer.println(errorJson);
                    writer.flush();
                }
            }
        } catch (IOException e) {
            logger.error("Error reading input", e);
        }
    }

    /**
     * 处理通知（没有 id 的请求，不需要响应）
     */
    private void handleNotification(JsonObject notification, String method) {
        if (method == null) {
            logger.warn("Received notification without method");
            return;
        }
        
        // 处理各种通知
        switch (method) {
            case "notifications/initialized":
                logger.info("Received initialized notification from client");
                // 客户端已初始化完成，不需要响应
                break;
            default:
                logger.debug("Received unknown notification: {}", method);
                break;
        }
    }

    private JsonObject handleRequest(JsonObject request) {
        String method = request.has("method") ? request.get("method").getAsString() : null;
        Object id = request.has("id") ? request.get("id") : null;

        if (method == null) {
            return createErrorResponse(id, -32600, "Invalid request");
        }

        switch (method) {
            case "initialize":
                return handleInitialize(request, id);
            case "tools/list":
                return handleToolsList(request, id);
            case "tools/call":
                return handleToolsCall(request, id);
            case "ping":
                return createSuccessResponse(id, createPingResponse());
            default:
                return createErrorResponse(id, -32601, "Method not found: " + method);
        }
    }

    private JsonObject handleInitialize(JsonObject request, Object id) {
        logger.info("Received initialize request");
        JsonObject result = new JsonObject();
        result.addProperty("protocolVersion", "2024-11-05");
        result.addProperty("serverVersion", "1.0.0");
        result.addProperty("serverName", "mysql-mcp-server");
        
        // 添加 serverInfo 对象（某些客户端可能需要）
        JsonObject serverInfo = new JsonObject();
        serverInfo.addProperty("name", "mysql-mcp-server");
        serverInfo.addProperty("version", "1.0.0");
        result.add("serverInfo", serverInfo);
        
        JsonObject capabilities = new JsonObject();
        JsonObject tools = new JsonObject();
        tools.addProperty("listChanged", true);
        capabilities.add("tools", tools);
        result.add("capabilities", capabilities);
        
        return createSuccessResponse(id, result);
    }

    private JsonObject handleToolsList(JsonObject request, Object id) {
        logger.info("Received tools/list request");
        return createSuccessResponse(id, toolHandler.getToolsList());
    }

    private JsonObject handleToolsCall(JsonObject request, Object id) {
        if (!request.has("params")) {
            return createErrorResponse(id, -32602, "Missing params");
        }

        JsonObject params = request.getAsJsonObject("params");
        if (!params.has("name")) {
            return createErrorResponse(id, -32602, "Missing tool name");
        }

        String toolName = params.get("name").getAsString();
        JsonObject arguments = params.has("arguments") 
            ? params.getAsJsonObject("arguments") 
            : new JsonObject();

        logger.info("Calling tool: {}", toolName);
        
        try {
            JsonObject result = toolHandler.callTool(toolName, arguments);
            return createSuccessResponse(id, result);
        } catch (Exception e) {
            logger.error("Error executing tool: {}", toolName, e);
            return createErrorResponse(id, -32603, "Error executing tool: " + e.getMessage());
        }
    }

    private JsonObject createSuccessResponse(Object id, JsonObject result) {
        JsonObject response = new JsonObject();
        response.addProperty("jsonrpc", "2.0");
        // JSON-RPC 2.0: id must be included in response, matching the request id
        // Ensure id is never null - use 0 as default if id is null
        if (id != null) {
            if (id instanceof Number) {
                response.addProperty("id", ((Number) id).doubleValue());
            } else if (id instanceof String) {
                response.addProperty("id", (String) id);
            } else {
                response.add("id", gson.toJsonTree(id));
            }
        } else {
            // If id is null, use 0 as default (some clients require non-null id)
            response.addProperty("id", 0);
        }
        response.add("result", result);
        return response;
    }

    private JsonObject createErrorResponse(Object id, int code, String message) {
        JsonObject response = new JsonObject();
        response.addProperty("jsonrpc", "2.0");
        // JSON-RPC 2.0: id must be included in error response, matching the request id
        // Ensure id is never null - use 0 as default if id is null
        if (id != null) {
            if (id instanceof Number) {
                response.addProperty("id", ((Number) id).doubleValue());
            } else if (id instanceof String) {
                response.addProperty("id", (String) id);
            } else {
                response.add("id", gson.toJsonTree(id));
            }
        } else {
            // If id is null, use 0 as default (some clients require non-null id)
            response.addProperty("id", 0);
        }
        
        JsonObject error = new JsonObject();
        error.addProperty("code", code);
        error.addProperty("message", message);
        response.add("error", error);
        
        return response;
    }

    private JsonObject createPingResponse() {
        JsonObject result = new JsonObject();
        result.addProperty("status", "ok");
        return result;
    }

}

