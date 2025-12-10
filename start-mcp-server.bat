@echo off
REM MySQL MCP Server启动脚本 for Cursor (Windows)
REM 请根据实际情况修改数据库连接信息

REM 设置数据库连接
set MYSQL_URL=jdbc:mysql://localhost:3306/test
set MYSQL_USERNAME=root
set MYSQL_PASSWORD=root

REM 获取脚本所在目录
set SCRIPT_DIR=%~dp0
set JAR_PATH=%SCRIPT_DIR%target\mysql-mcp-server-1.0.0.jar

REM 检查JAR文件是否存在
if not exist "%JAR_PATH%" (
    echo 错误: 找不到JAR文件: %JAR_PATH%
    echo 请先运行: mvn clean package
    exit /b 1
)

REM 启动服务器
java -jar "%JAR_PATH%"

