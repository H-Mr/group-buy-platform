#!/bin/bash
echo "🚀 正在启动服务..."
docker-compose up -d
echo "✅ 服务已后台运行。查看日志：docker-compose logs -f --tail=100 app"