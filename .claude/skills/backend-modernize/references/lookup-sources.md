# 버전·폐기 여부 조회 방법

기억으로 단정하지 않기 위한 확인 수단. 프로젝트 안에서 얻을 수 있는 근거를 먼저 쓰고, 없을 때 바깥을 조회한다.

## 목차

- [프로젝트 내부에서 얻는 근거](#프로젝트-내부에서-얻는-근거)
- [Maven Central](#maven-central)
- [GitHub 릴리스](#github-릴리스)
- [지원 종료(EOL) 확인](#지원-종료eol-확인)
- [마이그레이션 가이드](#마이그레이션-가이드)
- [취약점 확인](#취약점-확인)
- [Node·Python 생태계](#nodepython-생태계)

## 프로젝트 내부에서 얻는 근거

가장 정확하고 가장 빠르다. 바깥을 조회하기 전에 여기부터 본다.

**실제 해석된 의존성 버전** — 선언한 값과 다를 수 있다. BOM이 관리하는 버전, 전이 의존성이 여기서 드러난다.

```bash
./gradlew dependencies --configuration runtimeClasspath
./gradlew dependencyInsight --dependency <artifact> --configuration runtimeClasspath
```

**폐기 API 사용처** — 컴파일러가 직접 알려준다. 검색보다 정확하다.

```bash
./gradlew compileJava --rerun-tasks 2>&1 | grep -i -A2 deprecat
```

경고가 안 보이면 컴파일러 옵션에서 폐기 경고가 꺼져 있을 수 있다. `-Xlint:deprecation`을 켜고 다시 돌린다.

**업그레이드 가능 목록** — 플러그인이 있으면 한 번에 볼 수 있다.

```bash
./gradlew dependencyUpdates   # com.github.ben-manes.versions 플러그인
```

**기동 시 경고** — 설정 키 변경과 자동 설정 문제는 여기서 드러난다. 애플리케이션을 띄우고 로그의 `WARN`을 읽는 것이 확실한 확인이다.

## Maven Central

```bash
# 최신 버전 (정렬은 릴리스 순)
curl -s "https://search.maven.org/solrsearch/select?q=g:<group>+AND+a:<artifact>&core=gav&rows=10&wt=json" \
  | python3 -c "import sys,json;print('\n'.join(d['v'] for d in json.load(sys.stdin)['response']['docs']))"

# 특정 버전이 존재하는지
curl -s -o /dev/null -w '%{http_code}\n' \
  "https://repo1.maven.org/maven2/<group을 슬래시로>/<artifact>/<버전>/"
```

프리릴리스(`-M1`, `-RC1`, `-SNAPSHOT`)가 섞여 나온다. GA만 필요하면 걸러낸다.

## GitHub 릴리스

프레임워크와 빌드 도구는 GitHub 릴리스가 사실상 공지 채널이다.

```bash
curl -s https://api.github.com/repos/spring-projects/spring-boot/releases/latest \
  | grep -m1 '"tag_name"' | sed 's/.*: "//;s/".*//'

curl -s https://api.github.com/repos/gradle/gradle/releases/latest | grep -m1 '"tag_name"'
```

미인증 시 IP당 시간당 60회 제한이 있다. `gh` CLI가 인증돼 있으면 넉넉하다.

```bash
gh api repos/<owner>/<repo>/releases/latest --jq .tag_name
```

## 지원 종료(EOL) 확인

**버전이 최신인지보다 보안 패치를 받고 있는지가 중요하다.** 낡았어도 지원 중이면 급하지 않고, 최신 바로 아래여도 지원이 끝났으면 급하다.

```bash
# 여러 제품의 지원 주기를 한 곳에서
curl -s https://endoflife.date/api/spring-boot.json | head -40
curl -s https://endoflife.date/api/java.json | head -40
```

프레임워크 공식 사이트의 지원 정책 페이지가 더 정확하다. 상용 지원과 OSS 지원의 종료일이 다른 경우가 많으니 어느 쪽 기준인지 확인한다.

## 마이그레이션 가이드

메이저를 올리기 전에 읽는다. 대부분의 프로젝트가 버전별로 별도 문서를 낸다.

```bash
# 릴리스 노트 본문
curl -s https://api.github.com/repos/<owner>/<repo>/releases/tags/<태그> \
  | python3 -c "import sys,json;print(json.load(sys.stdin)['body'])"
```

읽을 때 우선순위는 **제거된 것 > 바뀐 기본값 > 새로 추가된 것** 순이다. 앞의 둘이 코드를 깨뜨린다. 특히 바뀐 기본값은 코드 수정 없이 동작만 달라져 발견이 늦다.

여러 메이저를 건너뛴다면 중간 단계 가이드도 본다. 제거는 보통 한 버전에서 예고되고 다음에서 실행된다.

## 취약점 확인

```bash
# OSV 데이터베이스 조회
curl -s -X POST https://api.osv.dev/v1/query \
  -d '{"package":{"ecosystem":"Maven","name":"<group>:<artifact>"},"version":"<버전>"}'
```

GitHub 저장소라면 Dependabot 알림이 이미 있을 수 있다.

```bash
gh api repos/<owner>/<repo>/dependabot/alerts --jq '.[] | {package:.dependency.package.name, severity:.security_advisory.severity}'
```

## Node·Python 생태계

백엔드가 JVM이 아닐 때.

```bash
# npm
npm view <패키지> version
npm outdated

# PyPI
curl -s https://pypi.org/pypi/<패키지>/json | python3 -c "import sys,json;print(json.load(sys.stdin)['info']['version'])"
pip list --outdated
```