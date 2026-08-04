#!/usr/bin/env bash
set -euo pipefail

# 저장소의 호스트 설정 파일을 배치한다. CD가 이미지 배포 직전에 호출한다.
# 배포된 이미지와 같은 커밋에서 온 파일임이 보장되어야 하므로 여기서 따로 받지 않는다.

REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
APP_DIR="${APP_DIR:-/opt/devhub}"
ALLOY_CONF=/etc/alloy/config.alloy

install -d -m 755 "$APP_DIR"
install -m 755 "$REPO_DIR/deploy/scripts/deploy.sh" \
    "$REPO_DIR/deploy/scripts/render-env.sh" "$APP_DIR/"
install -m 644 "$REPO_DIR/compose.prod.yaml" "$APP_DIR/"

# 매 배포마다 재시작하면 수집이 불필요하게 끊기므로 달라진 경우에만 반영한다.
# config.alloy에 환경변수를 추가했다면 setup-monitoring.sh를 따로 실행해야 한다.
if [ -f "$ALLOY_CONF" ] && ! cmp -s "$REPO_DIR/deploy/alloy/config.alloy" "$ALLOY_CONF"; then
    install -m 644 "$REPO_DIR/deploy/alloy/config.alloy" "$ALLOY_CONF"
    systemctl restart alloy
    echo "alloy config updated"
fi

# nginx 설정은 ORIGIN_DOMAIN 치환이 필요해 bootstrap.sh가 계속 담당한다.