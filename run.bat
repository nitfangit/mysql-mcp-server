@echo off
REM Windows批处理脚本 - 运行MySQL MCP服务器

REM 设置数据库连接（根据需要修改）
set MYSQL_URL=jdbc:mysql://localhost:3306/test
set MYSQL_USERNAME=root
set MYSQL_PASSWORD=root

REM 运行服务器
java -jar target\mysql-mcp-server-1.0.0.jar

pause

