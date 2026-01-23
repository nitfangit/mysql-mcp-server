# 日志配置说明

## 日志输出方式

本项目使用 **Logback** 作为日志实现，支持文件和控制台双重输出。

**配置方式：**
- 使用 `logback.xml` 配置文件
- 日志会同时输出到：
  - **控制台（stderr）**：符合MCP协议要求
  - **日志文件**：`logs/mysql-mcp-server.log`
  - **错误日志文件**：`logs/mysql-mcp-server-error.log`

**日志文件特性：**
- ✅ 按日期和大小滚动
- ✅ 单个文件最大 10MB
- ✅ 保留 30 天历史日志
- ✅ 总大小限制 1GB
- ✅ 错误日志单独存储

**日志文件位置：**
```
logs/
├── mysql-mcp-server.log          # 所有日志
├── mysql-mcp-server-error.log     # 仅错误日志
├── mysql-mcp-server.2024-01-01.0.log  # 历史日志（按日期）
└── mysql-mcp-server-error.2024-01-01.0.log
```

## 配置说明

1. 确保 `pom.xml` 中包含 logback 依赖：
```xml
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.5.19</version>
</dependency>
```

2. 确保 `src/main/resources/logback.xml` 存在

3. 重新编译项目：
```bash
mvn clean package
```

## 日志级别配置

### Logback 配置

在 `logback.xml` 中修改：
```xml
<!-- 项目包日志级别 -->
<logger name="com.mysqlmcp" level="INFO"/>

<!-- 根日志级别 -->
<root level="INFO">
```

可选级别：`TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`

## 日志格式

### Logback 格式
```
2024-01-01 12:00:00.123 [INFO] com.mysqlmcp.server.MCPServer - MySQL MCP Server starting...
```

### Simple Logger 格式
```
2024-01-01 12:00:00.123 [INFO] MCPServer - MySQL MCP Server starting...
```

## 自定义日志配置

### 修改日志文件路径

在 `logback.xml` 中修改：
```xml
<property name="LOG_HOME" value="logs"/>  <!-- 日志目录 -->
<property name="LOG_FILE" value="mysql-mcp-server"/>  <!-- 日志文件名 -->
```

### 修改日志保留策略

在 `logback.xml` 中修改：
```xml
<maxFileSize>10MB</maxFileSize>      <!-- 单个文件大小 -->
<maxHistory>30</maxHistory>         <!-- 保留天数 -->
<totalSizeCap>1GB</totalSizeCap>     <!-- 总大小限制 -->
```

## 常见问题

### Q: 日志文件没有生成？

**A:** 检查以下几点：
1. 确保 `logs` 目录存在或程序有创建权限
2. 确保使用的是 logback（检查依赖）
3. 检查 `logback.xml` 配置是否正确

### Q: 如何只输出到文件，不输出到控制台？

**A:** 在 `logback.xml` 中移除 CONSOLE appender：
```xml
<root level="INFO">
    <!-- <appender-ref ref="CONSOLE"/> -->  <!-- 注释掉这行 -->
    <appender-ref ref="FILE"/>
    <appender-ref ref="ERROR_FILE"/>
</root>
```

### Q: 如何临时提高日志级别进行调试？

**A:** 修改 `logback.xml` 中的日志级别：
```xml
<logger name="com.mysqlmcp" level="DEBUG"/>
<root level="DEBUG">
```

## 最佳实践

1. **生产环境**：使用 Logback，日志级别设置为 INFO
2. **开发环境**：可以使用 DEBUG 级别，便于调试
3. **日志文件管理**：定期清理旧日志，避免磁盘空间不足
4. **错误监控**：关注 `*-error.log` 文件，及时发现错误

