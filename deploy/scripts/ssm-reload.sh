#!/usr/bin/env bash
set -euo pipefail

# 수동 실행 전용. 호스트 설정 파일은 그대로 두고 SSM Parameter Store 값만 다시 읽어
# 재기동한다. 저장소 파일을 보내지 않으므로 배포된 이미지와 다른 커밋의 설정이 섞이지 않는다.

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

remote="$(mktemp)"
cat >"$remote" <<'SCRIPT'
set -euo pipefail
image=$(grep -m1 '^DEVHUB_IMAGE=' /opt/devhub/.env | cut -d= -f2-)
/opt/devhub/deploy.sh "$image"
SCRIPT

exec "$DIR/ssm-run.sh" "$remote" "reload ssm parameters"