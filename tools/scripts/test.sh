#!/usr/bin/env bash
set -euo pipefail
ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"

docker run --rm \
  -v "${ROOT}":/app \
  -v gradle_cache:/root/.gradle \
  -v /var/run/docker.sock:/var/run/docker.sock \
  --add-host=host.docker.internal:host-gateway \
  -e TESTCONTAINERS_RYUK_DISABLED=true \
  -e TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal \
  -w /app \
  gradle:8.14.1-jdk21 \
  gradle test --no-daemon
