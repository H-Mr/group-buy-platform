#!/bin/bash
echo "♻️  开始本地强制重装..."
docker-compose down
docker image prune -f
docker-compose up -d --build
echo "✅ 重装完成。"