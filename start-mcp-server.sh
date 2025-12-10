#!/bin/bash
# MySQL MCP Server启动脚本 for Cursor (Linux/Mac)

# 设置数据库连接（请根据实际情况修改）
export MYSQL_URL=jdbc:mysql://localhost:3306/test
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=root

# 获取脚本所在目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JAR_PATH="$SCRIPT_DIR/target/mysql-mcp-server-1.0.0.jar"

# 检查JAR文件是否存在
if [ ! -f "$JAR_PATH" ]; then
    echo "错误: 找不到JAR文件: $JAR_PATH"
    echo "请先运行: mvn clean package"
    exit 1
fi

# 启动服务器
java -jar "$JAR_PATH"

