# MySQL MCP Server

一个用Java开发的MCP（Model Context Protocol）服务器，用于操作MySQL数据库。

## 功能特性

- ✅ 执行SQL查询（SELECT）
- ✅ 执行SQL更新（INSERT、UPDATE、DELETE）
- ✅ 插入数据到指定表
- ✅ 更新表中的数据
- ✅ 删除表中的数据
- ✅ 列出所有数据库表
- ✅ 获取表结构信息
- ✅ 完全符合 MCP 协议 2024-11-05 规范
- ✅ 使用 SLF4J 进行日志记录
- ✅ 支持 UTF-8 编码

## 环境要求

- **Java 17** 或更高版本（必需）
- Maven 3.6 或更高版本
- MySQL 5.7 或更高版本

## 配置

### 方式1：环境变量

```bash
export MYSQL_URL=jdbc:mysql://localhost:3306/your_database
export MYSQL_USERNAME=your_username
export MYSQL_PASSWORD=your_password
```

### 方式2：系统属性

```bash
java -Dmysql.url=jdbc:mysql://localhost:3306/your_database \
     -Dmysql.username=your_username \
     -Dmysql.password=your_password \
     -jar mysql-mcp-server.jar
```

### 方式3：Cursor MCP 配置

在 Cursor 的 MCP 配置文件中添加（例如 `cursor-mcp-config.json`）：

```json
{
  "mcpServers": {
    "mysql": {
      "command": "java",
      "args": [
        "-jar",
        "D:\\path\\to\\mysql-mcp-server-1.0.0.jar"
      ],
      "env": {
        "MYSQL_URL": "jdbc:mysql://localhost:3306/test",
        "MYSQL_USERNAME": "root",
        "MYSQL_PASSWORD": "root"
      }
    }
  }
}
```

## 构建项目

```bash
mvn clean package
```

构建完成后，可执行JAR文件位于 `target/mysql-mcp-server-1.0.0.jar`

**注意**：如果遇到 Java 版本相关的编译错误，请确保：
1. 已安装 Java 17
2. `JAVA_HOME` 环境变量指向 Java 17 目录
3. 或者在 `pom.xml` 中配置 `maven-compiler-plugin` 使用正确的 Java 路径

## 运行

```bash
java -jar target/mysql-mcp-server-1.0.0.jar
```

或者使用Maven运行：

```bash
mvn exec:java -Dexec.mainClass="com.mysqlmcp.server.MCPServer"
```

## MCP协议支持

服务器支持 MCP 协议版本 **2024-11-05**，实现了以下方法：

- `initialize` - 初始化MCP连接
- `tools/list` - 获取可用工具列表
- `tools/call` - 调用指定工具
- `ping` - 健康检查
- `notifications/initialized` - 客户端初始化通知

所有工具响应都符合 MCP 协议规范，包含 `content` 数组字段。

## 可用工具

### 1. execute_query

执行SQL SELECT查询并返回结果。

**参数：**
- `sql` (string): SQL查询语句

**示例：**
```json
{
  "method": "tools/call",
  "params": {
    "name": "execute_query",
    "arguments": {
      "sql": "SELECT * FROM users LIMIT 10"
    }
  }
}
```

**响应：**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "Query returned 10 row(s)"
      }
    ],
    "rows": [...],
    "count": 10
  }
}
```

### 2. execute_update

执行SQL更新语句（INSERT、UPDATE、DELETE、CREATE、DROP等）。

**参数：**
- `sql` (string): SQL更新语句

**示例：**
```json
{
  "method": "tools/call",
  "params": {
    "name": "execute_update",
    "arguments": {
      "sql": "UPDATE users SET status = 'active' WHERE id = 1"
    }
  }
}
```

**响应：**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "Update completed. Affected rows: 1"
      }
    ],
    "affectedRows": 1
  }
}
```

### 3. insert_data

向指定表插入数据。

**参数：**
- `table` (string): 表名
- `data` (object): 要插入的数据（键值对）

**示例：**
```json
{
  "method": "tools/call",
  "params": {
    "name": "insert_data",
    "arguments": {
      "table": "users",
      "data": {
        "name": "John Doe",
        "email": "john@example.com",
        "age": 30
      }
    }
  }
}
```

**响应：**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "Insert completed. Affected rows: 1, Generated key: 123"
      }
    ],
    "affectedRows": 1,
    "generatedKey": "123"
  }
}
```

### 4. update_data

更新指定表中的数据。

**参数：**
- `table` (string): 表名
- `data` (object): 要更新的数据（键值对）
- `where` (string): WHERE条件（例如："id=1"）

**示例：**
```json
{
  "method": "tools/call",
  "params": {
    "name": "update_data",
    "arguments": {
      "table": "users",
      "data": {
        "email": "newemail@example.com"
      },
      "where": "id = 1"
    }
  }
}
```

**响应：**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "Update completed. Affected rows: 1"
      }
    ],
    "affectedRows": 1
  }
}
```

### 5. delete_data

从指定表中删除数据。

**参数：**
- `table` (string): 表名
- `where` (string): WHERE条件（例如："id=1"）

**示例：**
```json
{
  "method": "tools/call",
  "params": {
    "name": "delete_data",
    "arguments": {
      "table": "users",
      "where": "id = 1"
    }
  }
}
```

**响应：**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "Delete completed. Affected rows: 1"
      }
    ],
    "affectedRows": 1
  }
}
```

### 6. list_tables

获取数据库中的所有表名。

**参数：** 无

**示例：**
```json
{
  "method": "tools/call",
  "params": {
    "name": "list_tables",
    "arguments": {}
  }
}
```

**响应：**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "Database tables (10):\n- users\n- orders\n..."
      }
    ],
    "tables": ["users", "orders", ...],
    "count": 10
  }
}
```

### 7. describe_table

获取指定表的列信息。

**参数：**
- `table` (string): 表名

**示例：**
```json
{
  "method": "tools/call",
  "params": {
    "name": "describe_table",
    "arguments": {
      "table": "users"
    }
  }
}
```

**响应：**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "Table structure for 'users' (5 columns):\n- id (INT(10)) NOT NULL\n- name (VARCHAR(50))\n..."
      }
    ],
    "columns": [
      {
        "name": "id",
        "type": "INT",
        "size": 10,
        "nullable": false
      },
      ...
    ]
  }
}
```

## 日志配置

服务器使用 SLF4J Simple Logger 进行日志记录。可以通过 `src/main/resources/simplelogger.properties` 自定义日志配置。

**默认日志配置：**
- 日志级别：INFO
- 输出位置：stderr（MCP协议要求）
- 格式：`yyyy-MM-dd HH:mm:ss.SSS [LEVEL] ClassName - Message`
- 所有日志消息都带有 `[LOG-INFO]` 前缀以便识别

**注意**：在 Cursor 中，stderr 输出会被标记为 `[error]`，但这只是一个显示标记。实际的日志级别（INFO、DEBUG、ERROR）会在日志消息中显示。

## 编码

服务器对所有 I/O 操作使用 UTF-8 编码：
- 输入/输出流明确设置为 UTF-8
- 系统属性配置为 UTF-8
- 所有工具描述和消息使用英文以避免编码问题（工具定义中的描述为英文，但功能说明文档为中文）

## 使用示例

### 初始化连接

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "initialize",
  "params": {
    "protocolVersion": "2024-11-05",
    "capabilities": {},
    "clientInfo": {
      "name": "test-client",
      "version": "1.0.0"
    }
  }
}
```

### 获取工具列表

```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "tools/list"
}
```

## 测试

所有 7 个工具都已测试并验证可以正常工作：

- ✅ `list_tables` - 成功列出所有数据库表
- ✅ `describe_table` - 成功获取表结构
- ✅ `execute_query` - 成功执行 SELECT 查询
- ✅ `execute_update` - 成功执行 DDL/DML 语句
- ✅ `insert_data` - 成功插入数据并返回生成的主键
- ✅ `update_data` - 成功更新数据
- ✅ `delete_data` - 成功删除数据

## 安全注意事项

⚠️ **重要**：此服务器直接执行SQL语句，请确保：

1. 仅在有信任的环境中运行
2. 使用具有最小必要权限的数据库用户
3. 在生产环境中考虑添加SQL注入防护
4. 考虑添加身份验证和授权机制
5. 在执行前审查和验证所有SQL语句

## 故障排除

### Java 版本问题

如果遇到 `UnsupportedClassVersionError`：
- 确保已安装 Java 17
- 将 `JAVA_HOME` 环境变量设置为 Java 17 目录
- 或者配置 Maven 使用 Java 17 编译器

### 编码问题

如果看到乱码：
- 确保系统编码为 UTF-8
- 检查 `file.encoding` 系统属性是否设置为 UTF-8
- 工具定义中的描述使用英文以避免编码问题

### MCP 协议问题

如果工具无法正常工作：
- 验证 MCP 协议版本是否为 `2024-11-05`
- 检查响应是否包含 `content` 数组字段
- 确保符合 JSON-RPC 2.0 规范（正确处理 `id` 字段）

## 许可证

MIT License
