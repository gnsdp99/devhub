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

get_param() {
    aws ssm get-parameter --name "$HOST_SSM_PREFIX/$1" \
        --with-decryption --region "$REGION" \
        --query Parameter.Value --output text
}

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
EOF
umask 022

install -m 644 "$DEPLOY_DIR/alloy/config.alloy" /etc/alloy/config.alloy

systemctl enable alloy
systemctl restart alloy

echo "monitoring setup done. check: systemctl status alloy"