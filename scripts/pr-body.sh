#!/usr/bin/env bash
# 테스트를 실행해 단위/통합 개수를 세고, PR 템플릿의 테스트 결과 섹션만 채워 stdout으로 내보낸다.
# 분류는 클래스명 규약을 따른다. 통합은 *IntegrationTest, 단위는 *UnitTest.
set -euo pipefail

root="$(git rev-parse --show-toplevel)"
template="$root/.github/pull_request_template.md"
results="$root/backend/build/test-results/test"
sources="$root/backend/src/test/java"

unclassified="$(grep -rlE '@Test\b' "$sources" | grep -vE '(UnitTest|IntegrationTest)\.java$' || true)"
if [ -n "$unclassified" ]; then
    echo "클래스명이 규약에 맞지 않습니다. *UnitTest 또는 *IntegrationTest로 끝나야 합니다." >&2
    echo "$unclassified" >&2
    exit 1
fi

# 애플리케이션 로그가 본문에 섞이지 않도록 Gradle 출력은 전부 stderr로 보낸다.
(cd "$root/backend" && ./gradlew cleanTest test --quiet >&2)

attr() {
    grep -o " $2=\"[0-9]*\"" "$1" | head -1 | grep -o '[0-9]*'
}

unit=0
integration=0
broken=0

for xml in "$results"/*.xml; do
    count="$(attr "$xml" tests)"
    broken=$((broken + $(attr "$xml" failures) + $(attr "$xml" errors)))

    case "$(basename "$xml" .xml)" in
        *UnitTest | *UnitTest\$*) unit=$((unit + count)) ;;
        *) integration=$((integration + count)) ;;
    esac
done

if [ "$broken" -gt 0 ]; then
    echo "테스트 $broken건이 실패했습니다. $results 를 확인하세요." >&2
    exit 1
fi

sed -e "s|^- \[ \] 단위 / 통합 테스트 통과 .*|- [x] 단위 / 통합 테스트 통과 ($((unit + integration)) tests passed)|" \
    -e "s|^  - 단위 테스트: .*|  - 단위 테스트: $unit tests passed|" \
    -e "s|^  - 통합 테스트: .*|  - 통합 테스트: $integration tests passed|" \
    "$template"
