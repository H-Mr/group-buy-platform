#!/bin/bash
echo "🔄 正在重启..."
docker-compose down
docker-compose up -d
echo "✅ 重启完成。"