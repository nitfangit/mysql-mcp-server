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

## 环境要求

- Java 11 或更高版本
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

## 构建项目

```bash
mvn clean package
```

构建完成后，可执行JAR文件位于 `target/mysql-mcp-server-1.0.0.jar`

## 运行

```bash
java -jar target/mysql-mcp-server-1.0.0.jar
```

或者使用Maven运行：

```bash
mvn exec:java -Dexec.mainClass="com.mysqlmcp.server.MCPServer"
```

## MCP协议支持

服务器支持以下MCP方法：

- `initialize` - 初始化MCP连接
- `tools/list` - 获取可用工具列表
- `tools/call` - 调用指定工具
- `ping` - 健康检查

## 可用工具

### 1. execute_query
执行SQL查询语句

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

### 2. execute_update
执行SQL更新语句

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

### 3. insert_data
向指定表插入数据

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

### 4. update_data
更新指定表中的数据

**参数：**
- `table` (string): 表名
- `data` (object): 要更新的数据（键值对）
- `where` (string): WHERE条件

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

### 5. delete_data
从指定表中删除数据

**参数：**
- `table` (string): 表名
- `where` (string): WHERE条件

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

### 6. list_tables
获取数据库中的所有表名

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

### 7. describe_table
获取指定表的列信息

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

## 安全注意事项

⚠️ **重要**：此服务器直接执行SQL语句，请确保：

1. 仅在有信任的环境中运行
2. 使用具有最小必要权限的数据库用户
3. 在生产环境中添加SQL注入防护
4. 考虑添加身份验证和授权机制

## 许可证

MIT License

