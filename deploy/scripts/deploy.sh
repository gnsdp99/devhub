#!/usr/bin/env bash
set -euo pipefail

IMAGE="${1:?usage: deploy.sh <ecr-image-ref>}"
APP_DIR="${APP_DIR:-/opt/devhub}"
REGION="${AWS_REGION:-ap-northeast-2}"
REGISTRY="${IMAGE%%/*}"

cd "$APP_DIR"

umask 077
"$APP_DIR/render-env.sh" >.env
printf 'DEVHUB_IMAGE=%s\nAWS_REGION=%s\n' "$IMAGE" "$REGION" >>.env

for key in SPRING_DATASOURCE_URL SPRING_DATASOURCE_USERNAME SPRING_DATASOURCE_PASSWORD; do
    if ! grep -q "^$key=" .env; then
        echo "$key not found in SSM Parameter Store" >&2
        exit 1
    fi
done

aws ecr get-login-password --region "$REGION" |
    docker login --username AWS --password-stdin "$REGISTRY"

docker compose -f compose.prod.yaml pull
docker compose -f compose.prod.yaml up -d --wait --remove-orphans
docker image prune -f