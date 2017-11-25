#!/usr/bin/env bash
SERVICE_NAME=sheldon \
SERVICE_ROOT=go \
PORT=8085 \
SERVICE_PORT=8085 \
REDIS_URL=redis://127.0.0.1:6379 \
java  -jar target/sheldon-1.0-SNAPSHOT-fat.jar
