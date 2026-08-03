#!/usr/bin/env bash
# 현재 브랜치를 푸시하고 draft PR을 만든다. 본문은 PR 템플릿을 쓰되,
# 백엔드 빌드 입력이 바뀐 경우에만 테스트를 돌려 결과를 채운다.
# 사용법: scripts/pr-draft.sh [-y] [제목]
#   커밋이 하나면 그 커밋 제목을 쓴다. 여러 개면 제목을 인자로 줘야 한다.
#   -y 옵션을 주면 확인 없이 진행한다.
set -euo pipefail

root="$(git rev-parse --show-toplevel)"
cd "$root"
template=".github/pull_request_template.md"

assume_yes=false
if [ "${1:-}" = "-y" ]; then
    assume_yes=true
    shift
fi

if [ "$(git rev-parse --abbrev-ref HEAD)" = "main" ]; then
    echo "main에서는 PR을 만들 수 없습니다." >&2
    exit 1
fi

existing="$(gh pr view --json url --jq .url 2>/dev/null || true)"
if [ -n "$existing" ]; then
    echo "이 브랜치에는 이미 PR이 있습니다: $existing" >&2
    exit 1
fi

count="$(git rev-list --count main..HEAD)"
if [ "$count" -eq 0 ]; then
    echo "main과 차이가 없습니다." >&2
    exit 1
elif [ "$count" -eq 1 ]; then
    title="$(git log -1 --format=%s)"
elif [ $# -ge 1 ]; then
    title="$1"
else
    echo "커밋이 ${count}개입니다. 핵심 작업을 제목으로 넘겨주세요." >&2
    git log --oneline main..HEAD >&2
    exit 1
fi

body="$(mktemp)"
trap 'rm -f "$body"' EXIT

# 테스트 결과가 달라질 수 있는 변경일 때만 테스트를 돌린다.
# 인프라 설정·워크플로·문서만 바뀐 PR은 템플릿을 그대로 쓴다.
if git diff --name-only main...HEAD |
    grep -qE '^backend/(src/|gradle|build\.gradle\.kts|settings\.gradle\.kts)'; then
    scripts/pr-body.sh > "$body"

    # 템플릿과 다른 곳이 테스트 결과 3줄뿐인지 확인한다. 빌드 로그가 섞이는 사고를 막는다.
    if [ "$(diff "$template" "$body" | grep -c '^[<>]' || true)" -ne 6 ]; then
        echo "본문이 템플릿의 테스트 결과 3줄 외에 달라졌습니다." >&2
        diff "$template" "$body" >&2
        exit 1
    fi
else
    echo "백엔드 빌드 입력에 변경이 없어 테스트를 건너뜁니다." >&2
    cp "$template" "$body"
fi

printf '제목: %s\n\n' "$title"
cat "$body"

if [ "$assume_yes" = false ]; then
    if [ ! -t 0 ]; then
        echo "확인을 받을 수 없습니다. 비대화형으로 쓰려면 -y 를 주세요." >&2
        exit 1
    fi
    read -r -p $'\ndraft PR을 만들까요? [y/N] ' reply
    if [ "$reply" != "y" ]; then
        echo "취소했습니다."
        exit 1
    fi
fi

git push -u origin HEAD
gh pr create --draft --base main --title "$title" --body-file "$body"