#!/usr/bin/env bash
set -euo pipefail

# CD 러너에서 실행한다. SSM은 파일이 아니라 명령만 전달하므로 호스트 설정 파일을
# 명령에 실어 보낸다. 이미지를 넘기지 않으면 인스턴스에 배포돼 있는 것을 재사용한다.

IMAGE="${1:-}"
SHA="${SHA:?commit sha is required}"
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

payload=$(tar -cz compose.prod.yaml deploy | base64 -w0)

if [ -n "$IMAGE" ]; then
    resolve="image=$IMAGE"
else
    resolve="image=\$(grep -m1 '^DEVHUB_IMAGE=' /opt/devhub/.env | cut -d= -f2-)"
fi

remote="$(mktemp)"
cat >"$remote" <<SCRIPT
set -euo pipefail
rm -rf /tmp/devhub-sync
mkdir -p /tmp/devhub-sync
echo "$payload" | base64 -d | tar -xz -C /tmp/devhub-sync
/tmp/devhub-sync/deploy/scripts/sync-host.sh
$resolve
/opt/devhub/deploy.sh "\$image"
rm -rf /tmp/devhub-sync
SCRIPT

exec "$DIR/ssm-run.sh" "$remote" "deploy ${SHA:0:7}"