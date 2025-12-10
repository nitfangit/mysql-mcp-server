#!/bin/bash
# Linux/Mac shell脚本 - 运行MySQL MCP服务器

# 设置数据库连接（根据需要修改）
export MYSQL_URL=jdbc:mysql://localhost:3306/test
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=

# 运行服务器
java -jar target/mysql-mcp-server-1.0.0.jar

