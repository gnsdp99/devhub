#!/usr/bin/env bash
set -euo pipefail

IMAGE="${1:?usage: deploy.sh <ecr-image-ref>}"
APP_DIR="${APP_DIR:-/opt/devhub}"
REGION="${AWS_REGION:-ap-northeast-2}"
REGISTRY="${IMAGE%%/*}"

cd "$APP_DIR"

# .env에는 이미지 참조와 SSM 값이 함께 들어가므로, 되돌리면 직전 배포 상태로 돌아간다.
backup=""
if [ -f .env ]; then
    backup="$(mktemp)"
    cp .env "$backup"
fi

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

if ! docker compose -f compose.prod.yaml up -d --wait --remove-orphans; then
    if [ -n "$backup" ]; then
        echo "기동에 실패해 직전 설정으로 되돌린다" >&2
        cp "$backup" .env
        docker compose -f compose.prod.yaml up -d --wait --remove-orphans
    fi
    exit 1
fi

docker image prune -f