@echo off
REM 简单的Windows批处理测试脚本
REM 演示如何通过命令行与MCP服务器交互

echo ========================================
echo MySQL MCP Server 简单测试
echo ========================================
echo.

REM 设置数据库连接
set MYSQL_URL=jdbc:mysql://localhost:3306/test
set MYSQL_USERNAME=root
set MYSQL_PASSWORD=root

echo 数据库配置:
echo   URL: %MYSQL_URL%
echo   用户名: %MYSQL_USERNAME%
echo.

echo 启动服务器并发送测试请求...
echo.

REM 注意：Windows的管道处理JSON比较复杂，建议使用Python脚本
echo 提示: 建议使用 test_simple.py 进行更完整的测试
echo.

REM 简单的初始化请求测试
echo {"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}} | java -jar target\mysql-mcp-server-1.0.0.jar

pause

