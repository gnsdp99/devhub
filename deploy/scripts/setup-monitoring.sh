#!/usr/bin/env bash
set -euo pipefail

# Grafana Alloy 설치. Grafana Cloud 파라미터를 SSM에 등록한 뒤 root로 1회 실행한다.

REGION="${AWS_REGION:-ap-northeast-2}"
HOST_SSM_PREFIX="${HOST_SSM_PREFIX:-/devhub/host}"
ALLOY_VERSION="${ALLOY_VERSION:-1.18.0}"
SRC_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_DIR="$(cd "$SRC_DIR/.." && pwd)"

dnf -y install \
    "https://github.com/grafana/alloy/releases/download/v${ALLOY_VERSION}/alloy-${ALLOY_VERSION}-1.$(uname -m | sed 's/aarch64/arm64/').rpm"

# 컨테이너 로그를 Docker API로 읽으므로 docker 그룹이 필요하다.
usermod -aG docker alloy

# 명령 치환 실패는 set -e가 잡지 못하므로 빈 값을 직접 걸러낸다.
# 값 자체는 어떤 경우에도 출력하지 않는다.
get_param() {
    local value
    value="$(aws ssm get-parameter --name "$HOST_SSM_PREFIX/$1" \
        --with-decryption --region "$REGION" \
        --query Parameter.Value --output text 2>/dev/null || true)"
    if [ -z "$value" ] || [ "$value" = "None" ]; then
        echo "$HOST_SSM_PREFIX/$1 is missing or empty in SSM Parameter Store" >&2
        exit 1
    fi
    printf '%s' "$value"
}

# 형식이 깨진 DSN은 Alloy의 마스킹을 통과하지 못해 비밀번호가 메트릭 라벨과
# 로그에 그대로 실린다. 배치 전에 막는다.
assert_dsn() {
    if ! printf '%s' "$1" | grep -qE '^postgres(ql)?://[^:/@]+:[^@/]+@[^:/@]+(:[0-9]+)?/[^?/]+'; then
        echo "POSTGRES_EXPORTER_DSN must look like postgresql://user:password@host:5432/db" >&2
        exit 1
    fi
}

for key in GRAFANA_CLOUD_PROMETHEUS_URL GRAFANA_CLOUD_PROMETHEUS_USERNAME \
    GRAFANA_CLOUD_LOKI_URL GRAFANA_CLOUD_LOKI_USERNAME \
    GRAFANA_CLOUD_API_TOKEN POSTGRES_EXPORTER_DSN; do
    get_param "$key" >/dev/null
done
assert_dsn "$(get_param POSTGRES_EXPORTER_DSN)"

umask 077
cat >/etc/sysconfig/alloy <<EOF
CONFIG_FILE="/etc/alloy/config.alloy"
CUSTOM_ARGS=""
RESTART_ON_UPGRADE=true
GRAFANA_CLOUD_PROMETHEUS_URL=$(get_param GRAFANA_CLOUD_PROMETHEUS_URL)
GRAFANA_CLOUD_PROMETHEUS_USERNAME=$(get_param GRAFANA_CLOUD_PROMETHEUS_USERNAME)
GRAFANA_CLOUD_LOKI_URL=$(get_param GRAFANA_CLOUD_LOKI_URL)
GRAFANA_CLOUD_LOKI_USERNAME=$(get_param GRAFANA_CLOUD_LOKI_USERNAME)
GRAFANA_CLOUD_API_TOKEN=$(get_param GRAFANA_CLOUD_API_TOKEN)
POSTGRES_EXPORTER_DSN=$(get_param POSTGRES_EXPORTER_DSN)
EOF
umask 022

install -m 644 "$DEPLOY_DIR/alloy/config.alloy" /etc/alloy/config.alloy

systemctl enable alloy
systemctl restart alloy

echo "monitoring setup done. check: systemctl status alloy"