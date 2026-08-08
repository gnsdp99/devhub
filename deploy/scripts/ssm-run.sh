#!/usr/bin/env bash
set -euo pipefail

# 원격 스크립트를 SSM Run Command로 인스턴스에서 실행하고 출력을 러너로 옮긴다.

SCRIPT="${1:?usage: ssm-run.sh <script-file> <comment>}"
COMMENT="${2:?usage: ssm-run.sh <script-file> <comment>}"
INSTANCE_ID="${INSTANCE_ID:?instance id is required}"

params="$(mktemp)"
jq -Rn --rawfile s "$SCRIPT" '{commands: ($s | rtrimstr("\n") | split("\n"))}' >"$params"

command_id=$(aws ssm send-command \
    --instance-ids "$INSTANCE_ID" \
    --document-name AWS-RunShellScript \
    --comment "$COMMENT" \
    --parameters "file://$params" \
    --query Command.CommandId --output text)

set +e
aws ssm wait command-executed --command-id "$command_id" --instance-id "$INSTANCE_ID"
status=$?
set -e

aws ssm get-command-invocation \
    --command-id "$command_id" --instance-id "$INSTANCE_ID" \
    --query '[StandardOutputContent, StandardErrorContent]' --output text
exit $status