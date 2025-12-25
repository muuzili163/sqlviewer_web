#!/bin/bash

# SQLViewer Web - 启动脚本

JAR_NAME="sqlviewer-web-1.0.0.jar"
JAR_PATH="target/$JAR_NAME"

# 检查JAR文件是否存在
if [ ! -f "$JAR_PATH" ]; then
    echo "错误：找不到JAR文件 $JAR_PATH"
    echo "请先运行：mvn clean package"
    exit 1
fi

# 启动应用
echo "正在启动 SQLViewer Web..."
echo "访问地址：http://localhost:8080"
echo "按 Ctrl+C 停止应用"
echo ""

java -jar "$JAR_PATH"
