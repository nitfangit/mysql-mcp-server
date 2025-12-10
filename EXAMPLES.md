# MCP服务器交互示例

## 启动服务器

服务器通过标准输入/输出（stdin/stdout）进行JSON-RPC通信。

### Windows
```bash
# 设置环境变量
set MYSQL_URL=jdbc:mysql://localhost:3306/test
set MYSQL_USERNAME=root
set MYSQL_PASSWORD=root

# 运行服务器
java -jar target\mysql-mcp-server-1.0.0.jar
```

### Linux/Mac
```bash
# 设置环境变量
export MYSQL_URL=jdbc:mysql://localhost:3306/test
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=root

# 运行服务器
java -jar target/mysql-mcp-server-1.0.0.jar
```

## 交互方式

服务器启动后，会等待从标准输入读取JSON-RPC请求，并将响应输出到标准输出。

## 请求示例

### 1. 初始化连接

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

**响应：**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "protocolVersion": "2024-11-05",
    "serverVersion": "1.0.0",
    "serverName": "mysql-mcp-server",
    "capabilities": {
      "tools": {
        "listChanged": true
      }
    }
  }
}
```

### 2. 获取工具列表

```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "tools/list"
}
```

**响应：**
```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "result": {
    "tools": [
      {
        "name": "execute_query",
        "description": "执行SQL查询语句",
        "inputSchema": {
          "type": "object",
          "description": "执行SELECT查询并返回结果",
          "properties": {
            "sql": {
              "type": "string",
              "description": "SQL语句"
            }
          },
          "required": ["sql"]
        }
      },
      ...
    ]
  }
}
```

### 3. 执行SQL查询

```json
{
  "jsonrpc": "2.0",
  "id": 3,
  "method": "tools/call",
  "params": {
    "name": "execute_query",
    "arguments": {
      "sql": "SELECT * FROM users LIMIT 5"
    }
  }
}
```

**响应：**
```json
{
  "jsonrpc": "2.0",
  "id": 3,
  "result": {
    "rows": [
      {
        "id": 1,
        "name": "张三",
        "email": "zhangsan@example.com"
      },
      {
        "id": 2,
        "name": "李四",
        "email": "lisi@example.com"
      }
    ],
    "count": 2
  }
}
```

### 4. 插入数据

```json
{
  "jsonrpc": "2.0",
  "id": 4,
  "method": "tools/call",
  "params": {
    "name": "insert_data",
    "arguments": {
      "table": "users",
      "data": {
        "name": "王五",
        "email": "wangwu@example.com",
        "age": 25
      }
    }
  }
}
```

**响应：**
```json
{
  "jsonrpc": "2.0",
  "id": 4,
  "result": {
    "affectedRows": 1,
    "generatedKey": "3"
  }
}
```

### 5. 更新数据

```json
{
  "jsonrpc": "2.0",
  "id": 5,
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
  "id": 5,
  "result": {
    "affectedRows": 1
  }
}
```

### 6. 删除数据

```json
{
  "jsonrpc": "2.0",
  "id": 6,
  "method": "tools/call",
  "params": {
    "name": "delete_data",
    "arguments": {
      "table": "users",
      "where": "id = 3"
    }
  }
}
```

**响应：**
```json
{
  "jsonrpc": "2.0",
  "id": 6,
  "result": {
    "affectedRows": 1
  }
}
```

### 7. 列出所有表

```json
{
  "jsonrpc": "2.0",
  "id": 7,
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
  "id": 7,
  "result": {
    "tables": ["users", "orders", "products"]
  }
}
```

### 8. 查看表结构

```json
{
  "jsonrpc": "2.0",
  "id": 8,
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
  "id": 8,
  "result": {
    "columns": [
      {
        "name": "id",
        "type": "INT",
        "size": 11,
        "nullable": false,
        "defaultValue": null
      },
      {
        "name": "name",
        "type": "VARCHAR",
        "size": 100,
        "nullable": true,
        "defaultValue": null
      },
      {
        "name": "email",
        "type": "VARCHAR",
        "size": 255,
        "nullable": true,
        "defaultValue": null
      }
    ]
  }
}
```

### 9. 执行更新SQL

```json
{
  "jsonrpc": "2.0",
  "id": 9,
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
  "id": 9,
  "result": {
    "affectedRows": 1
  }
}
```

### 10. 健康检查（Ping）

```json
{
  "jsonrpc": "2.0",
  "id": 10,
  "method": "ping"
}
```

**响应：**
```json
{
  "jsonrpc": "2.0",
  "id": 10,
  "result": {
    "status": "ok"
  }
}
```

## 使用Python脚本测试

创建一个Python脚本来测试MCP服务器：

```python
import json
import subprocess
import sys

# 启动MCP服务器
process = subprocess.Popen(
    ['java', '-jar', 'target/mysql-mcp-server-1.0.0.jar'],
    stdin=subprocess.PIPE,
    stdout=subprocess.PIPE,
    stderr=subprocess.PIPE,
    text=True
)

# 发送初始化请求
init_request = {
    "jsonrpc": "2.0",
    "id": 1,
    "method": "initialize",
    "params": {
        "protocolVersion": "2024-11-05",
        "capabilities": {},
        "clientInfo": {"name": "python-client", "version": "1.0.0"}
    }
}

process.stdin.write(json.dumps(init_request) + "\n")
process.stdin.flush()

# 读取响应
response = process.stdout.readline()
print("初始化响应:", response)

# 发送工具列表请求
tools_request = {
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/list"
}

process.stdin.write(json.dumps(tools_request) + "\n")
process.stdin.flush()

response = process.stdout.readline()
print("工具列表响应:", response)

# 发送查询请求
query_request = {
    "jsonrpc": "2.0",
    "id": 3,
    "method": "tools/call",
    "params": {
        "name": "execute_query",
        "arguments": {
            "sql": "SELECT * FROM users LIMIT 5"
        }
    }
}

process.stdin.write(json.dumps(query_request) + "\n")
process.stdin.flush()

response = process.stdout.readline()
print("查询响应:", response)

# 关闭
process.stdin.close()
process.wait()
```

## 使用命令行测试（Windows）

创建一个测试脚本 `test.bat`：

```batch
@echo off
echo {"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}} | java -jar target\mysql-mcp-server-1.0.0.jar
```

## 使用命令行测试（Linux/Mac）

创建一个测试脚本 `test.sh`：

```bash
#!/bin/bash
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}' | java -jar target/mysql-mcp-server-1.0.0.jar
```

## 注意事项

1. **每行一个请求**：每个JSON-RPC请求必须单独一行，以换行符结尾
2. **JSON格式**：确保JSON格式正确，没有语法错误
3. **连接管理**：服务器会保持数据库连接，直到程序退出
4. **错误处理**：如果请求格式错误或执行失败，服务器会返回错误响应

## 错误响应示例

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "error": {
    "code": -32602,
    "message": "缺少参数: sql"
  }
}
```

