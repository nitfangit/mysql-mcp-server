# Cursor IDE 配置指南

本指南将帮助您在 Cursor IDE 中配置和使用 MySQL MCP 服务器。

## 前置步骤

### 1. 构建项目

首先确保项目已构建：

```bash
mvn clean package
```

构建完成后，JAR文件位于：`target/mysql-mcp-server-1.0.0.jar`

### 2. 准备启动脚本

创建启动脚本以便Cursor调用。脚本需要设置环境变量并启动Java进程。

## Windows 配置

### 步骤1：创建启动脚本

创建文件 `start-mcp-server.bat`（放在项目根目录或系统PATH中）：

```batch
@echo off
REM MySQL MCP Server启动脚本 for Cursor
REM 请根据实际情况修改数据库连接信息

set MYSQL_URL=jdbc:mysql://localhost:3306/test
set MYSQL_USERNAME=root
set MYSQL_PASSWORD=root

REM 获取JAR文件的绝对路径（假设脚本在项目根目录）
set JAR_PATH=%~dp0target\mysql-mcp-server-1.0.0.jar

REM 启动服务器
java -jar "%JAR_PATH%"
```

### 步骤2：配置Cursor

1. 打开 Cursor IDE
2. 打开设置（Settings）
3. 找到 MCP 或 Model Context Protocol 相关设置
4. 添加新的MCP服务器配置

**配置文件位置：**
- Windows: `%APPDATA%\Cursor\User\globalStorage\mcp.json` 或类似位置
- 或者通过 Cursor 的设置界面添加

**配置示例（JSON格式）：**

```json
{
  "mcpServers": {
    "mysql": {
      "command": "cmd",
      "args": [
        "/c",
        "D:\\projects\\mysql-mcp\\start-mcp-server.bat"
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

**或者使用直接命令方式：**

```json
{
  "mcpServers": {
    "mysql": {
      "command": "java",
      "args": [
        "-jar",
        "D:\\projects\\mysql-mcp\\target\\mysql-mcp-server-1.0.0.jar"
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

## Linux/Mac 配置

### 步骤1：创建启动脚本

创建文件 `start-mcp-server.sh`（放在项目根目录）：

```bash
#!/bin/bash
# MySQL MCP Server启动脚本 for Cursor

# 设置数据库连接（请根据实际情况修改）
export MYSQL_URL=jdbc:mysql://localhost:3306/test
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=root

# 获取脚本所在目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JAR_PATH="$SCRIPT_DIR/target/mysql-mcp-server-1.0.0.jar"

# 启动服务器
java -jar "$JAR_PATH"
```

使脚本可执行：
```bash
chmod +x start-mcp-server.sh
```

### 步骤2：配置Cursor

**配置文件位置：**
- Linux: `~/.config/Cursor/User/globalStorage/mcp.json` 或类似位置
- Mac: `~/Library/Application Support/Cursor/User/globalStorage/mcp.json` 或类似位置

**配置示例：**

```json
{
  "mcpServers": {
    "mysql": {
      "command": "/home/username/projects/mysql-mcp/start-mcp-server.sh",
      "env": {
        "MYSQL_URL": "jdbc:mysql://localhost:3306/test",
        "MYSQL_USERNAME": "root",
        "MYSQL_PASSWORD": "root"
      }
    }
  }
}
```

**或者使用直接命令方式：**

```json
{
  "mcpServers": {
    "mysql": {
      "command": "java",
      "args": [
        "-jar",
        "/home/username/projects/mysql-mcp/target/mysql-mcp-server-1.0.0.jar"
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

## 通过Cursor设置界面配置（推荐）

如果Cursor支持图形界面配置：

1. 打开 Cursor IDE
2. 按 `Ctrl+,` (Windows/Linux) 或 `Cmd+,` (Mac) 打开设置
3. 搜索 "MCP" 或 "Model Context Protocol"
4. 点击 "Add MCP Server" 或类似按钮
5. 填写以下信息：
   - **Name**: `mysql` 或 `MySQL Database`
   - **Command**: `java`
   - **Arguments**: `["-jar", "/path/to/mysql-mcp-server-1.0.0.jar"]`
   - **Environment Variables**:
     - `MYSQL_URL`: `jdbc:mysql://localhost:3306/your_database`
     - `MYSQL_USERNAME`: `your_username`
     - `MYSQL_PASSWORD`: `your_password`

## 验证配置

配置完成后：

1. 重启 Cursor IDE
2. 在聊天界面中，你应该能看到可用的MCP工具
3. 尝试询问："列出数据库中的所有表" 或 "查询users表的前10条记录"

## 使用示例

配置成功后，你可以在Cursor的AI聊天中直接使用：

### 示例1：查询数据
```
请帮我查询users表中的所有数据
```

### 示例2：查看表结构
```
显示users表的结构
```

### 示例3：插入数据
```
向users表插入一条新记录：name=张三, email=zhangsan@example.com
```

### 示例4：更新数据
```
更新users表中id=1的记录的email为newemail@example.com
```

## 故障排除

### 问题1：Cursor无法连接到MCP服务器

**解决方案：**
- 检查Java是否在系统PATH中：`java -version`
- 检查JAR文件路径是否正确
- 检查环境变量是否正确设置
- 查看Cursor的日志文件以获取详细错误信息

### 问题2：数据库连接失败

**解决方案：**
- 确认MySQL服务正在运行
- 验证数据库连接信息（URL、用户名、密码）
- 检查数据库用户是否有足够的权限
- 确认防火墙设置允许连接

### 问题3：工具不可用

**解决方案：**
- 重启Cursor IDE
- 检查MCP服务器是否正常启动
- 在Cursor设置中验证MCP服务器配置
- 查看Cursor的开发者工具控制台（如果有）

## 安全建议

⚠️ **重要安全提示：**

1. **不要将密码硬编码在配置文件中**
   - 考虑使用环境变量
   - 或使用Cursor的加密配置功能（如果支持）

2. **使用最小权限原则**
   - 为MCP服务器创建专用的数据库用户
   - 只授予必要的权限

3. **生产环境配置**
   - 使用配置文件或密钥管理服务
   - 避免在版本控制中提交包含密码的配置

## 高级配置

### 使用配置文件

你可以修改 `DatabaseManager` 类以支持从配置文件读取：

```java
// 在DatabaseManager中添加配置文件支持
Properties props = new Properties();
try (FileInputStream fis = new FileInputStream("config.properties")) {
    props.load(fis);
    this.url = props.getProperty("mysql.url");
    this.username = props.getProperty("mysql.username");
    this.password = props.getProperty("mysql.password");
}
```

### 多数据库支持

如果需要连接多个数据库，可以创建多个MCP服务器配置，每个使用不同的环境变量。

## 更多资源

- [MCP协议文档](https://modelcontextprotocol.io/)
- [Cursor文档](https://cursor.sh/docs)
- 项目README: 查看 `README.md` 了解更多功能

