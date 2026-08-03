#!/usr/bin/env bash
set -euo pipefail

# EC2 최초 1회 실행. 저장소의 deploy/ 디렉터리를 인스턴스로 복사한 뒤 root로 실행한다.

ORIGIN_DOMAIN="${ORIGIN_DOMAIN:?Origin domain is required}"
CERTBOT_EMAIL="${CERTBOT_EMAIL:?Certbot contact email is required}"
REGION="${AWS_REGION:-ap-northeast-2}"
HOST_SSM_PREFIX="${HOST_SSM_PREFIX:-/devhub/host}"
APP_DIR="${APP_DIR:-/opt/devhub}"
SRC_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_VERSION="${COMPOSE_VERSION:-v5.3.1}"

dnf -y update
dnf -y install docker nginx python3 augeas-libs

install -d /usr/libexec/docker/cli-plugins
curl -fsSL \
    "https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-linux-$(uname -m)" \
    -o /usr/libexec/docker/cli-plugins/docker-compose
chmod +x /usr/libexec/docker/cli-plugins/docker-compose

systemctl enable --now docker

install -d -m 755 "$APP_DIR"
install -m 755 "$SRC_DIR/deploy.sh" "$SRC_DIR/render-env.sh" "$APP_DIR/"
install -m 644 "$SRC_DIR/../compose.prod.yaml" "$APP_DIR/"

# 80번 포트를 열지 않으므로 HTTP-01 대신 Cloudflare DNS-01 챌린지를 쓴다.
python3 -m venv /opt/certbot
/opt/certbot/bin/pip install --quiet --upgrade pip
/opt/certbot/bin/pip install --quiet certbot certbot-dns-cloudflare
ln -sf /opt/certbot/bin/certbot /usr/local/bin/certbot

install -d -m 700 /etc/letsencrypt/cloudflare
umask 077
printf 'dns_cloudflare_api_token = %s\n' \
    "$(aws ssm get-parameter --name "$HOST_SSM_PREFIX/CLOUDFLARE_API_TOKEN" \
        --with-decryption --region "$REGION" --query Parameter.Value --output text)" \
    >/etc/letsencrypt/cloudflare/credentials.ini
umask 022

certbot certonly \
    --non-interactive --agree-tos --email "$CERTBOT_EMAIL" \
    --dns-cloudflare \
    --dns-cloudflare-credentials /etc/letsencrypt/cloudflare/credentials.ini \
    --dns-cloudflare-propagation-seconds 30 \
    -d "$ORIGIN_DOMAIN"

sed "s/ORIGIN_DOMAIN/$ORIGIN_DOMAIN/g" "$SRC_DIR/nginx/devhub-origin.conf" \
    >/etc/nginx/conf.d/devhub-origin.conf
nginx -t
systemctl enable --now nginx

cat >/etc/systemd/system/certbot-renew.service <<'EOF'
[Unit]
Description=Renew Let's Encrypt certificates

[Service]
Type=oneshot
ExecStart=/usr/local/bin/certbot renew --quiet --deploy-hook "systemctl reload nginx"
EOF

cat >/etc/systemd/system/certbot-renew.timer <<'EOF'
[Unit]
Description=Run certbot renew twice daily

[Timer]
OnCalendar=*-*-* 03,15:00:00
RandomizedDelaySec=1h
Persistent=true

[Install]
WantedBy=timers.target
EOF

systemctl daemon-reload
systemctl enable --now certbot-renew.timer

echo "bootstrap done. deploy with: $APP_DIR/deploy.sh <ecr-image-ref>"
