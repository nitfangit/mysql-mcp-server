#!/usr/bin/env python3
"""
简单的MCP服务器测试脚本
演示如何与MySQL MCP服务器进行交互
"""

import json
import subprocess
import sys
import os

def send_request(process, request):
    """发送请求并获取响应"""
    request_json = json.dumps(request, ensure_ascii=False)
    print(f"\n发送请求: {request_json}")
    
    process.stdin.write(request_json + "\n")
    process.stdin.flush()
    
    response_line = process.stdout.readline()
    if response_line:
        response = json.loads(response_line.strip())
        print(f"收到响应: {json.dumps(response, ensure_ascii=False, indent=2)}")
        return response
    return None

def main():
    # 检查JAR文件是否存在
    jar_path = "target/mysql-mcp-server-1.0.0.jar"
    if not os.path.exists(jar_path):
        print(f"错误: 找不到JAR文件 {jar_path}")
        print("请先运行: mvn clean package")
        sys.exit(1)
    
    # 设置环境变量
    env = os.environ.copy()
    env['MYSQL_URL'] = env.get('MYSQL_URL', 'jdbc:mysql://localhost:3306/test')
    env['MYSQL_USERNAME'] = env.get('MYSQL_USERNAME', 'root')
    env['MYSQL_PASSWORD'] = env.get('MYSQL_PASSWORD', 'root')
    
    print("启动MCP服务器...")
    print(f"数据库URL: {env['MYSQL_URL']}")
    print(f"用户名: {env['MYSQL_USERNAME']}")
    
    # 启动服务器进程
    process = subprocess.Popen(
        ['java', '-jar', jar_path],
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        env=env,
        bufsize=1
    )
    
    try:
        # 1. 初始化
        print("\n=== 1. 初始化连接 ===")
        init_request = {
            "jsonrpc": "2.0",
            "id": 1,
            "method": "initialize",
            "params": {
                "protocolVersion": "2024-11-05",
                "capabilities": {},
                "clientInfo": {
                    "name": "python-test-client",
                    "version": "1.0.0"
                }
            }
        }
        send_request(process, init_request)
        
        # 2. 获取工具列表
        print("\n=== 2. 获取工具列表 ===")
        tools_request = {
            "jsonrpc": "2.0",
            "id": 2,
            "method": "tools/list"
        }
        response = send_request(process, tools_request)
        if response and 'result' in response:
            tools = response['result'].get('tools', [])
            print(f"\n可用工具数量: {len(tools)}")
            for tool in tools:
                print(f"  - {tool.get('name')}: {tool.get('description')}")
        
        # 3. 列出所有表
        print("\n=== 3. 列出所有表 ===")
        list_tables_request = {
            "jsonrpc": "2.0",
            "id": 3,
            "method": "tools/call",
            "params": {
                "name": "list_tables",
                "arguments": {}
            }
        }
        send_request(process, list_tables_request)
        
        # 4. 执行查询（如果表存在）
        print("\n=== 4. 执行SQL查询 ===")
        query_request = {
            "jsonrpc": "2.0",
            "id": 4,
            "method": "tools/call",
            "params": {
                "name": "execute_query",
                "arguments": {
                    "sql": "SELECT 1 as test_value, 'Hello MCP' as message"
                }
            }
        }
        send_request(process, query_request)
        
        # 5. Ping测试
        print("\n=== 5. Ping测试 ===")
        ping_request = {
            "jsonrpc": "2.0",
            "id": 5,
            "method": "ping"
        }
        send_request(process, ping_request)
        
    except KeyboardInterrupt:
        print("\n\n用户中断")
    except Exception as e:
        print(f"\n错误: {e}")
        import traceback
        traceback.print_exc()
    finally:
        print("\n关闭服务器...")
        process.stdin.close()
        process.terminate()
        process.wait()
        print("完成")

if __name__ == "__main__":
    main()

