#!/usr/bin/env bash
set -euo pipefail
ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"

docker run --rm \
  -v "${ROOT}":/app \
  -v gradle_cache:/root/.gradle \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -w /app \
  gradle:8.10.2-jdk21 \
  gradle test --no-daemon
