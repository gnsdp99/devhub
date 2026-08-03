#!/usr/bin/env bash
set -euo pipefail

# SSM Parameter Store의 파라미터 이름을 그대로 환경변수 이름으로 쓴다.
# 예: /devhub/prod/SPRING_DATASOURCE_URL -> SPRING_DATASOURCE_URL
SSM_PREFIX="${SSM_PREFIX:-/devhub/prod}"
REGION="${AWS_REGION:-ap-northeast-2}"

aws ssm get-parameters-by-path \
    --path "$SSM_PREFIX" \
    --recursive \
    --with-decryption \
    --region "$REGION" \
    --query 'Parameters[].[Name,Value]' \
    --output text |
    while IFS=$'\t' read -r name value; do
        printf '%s=%s\n' "${name##*/}" "$value"
    done
