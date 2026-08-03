# 버전 조회 출처와 명령

기억으로 버전을 단정하지 않기 위한 조회 방법 모음. 대부분 인증 없이 동작한다.

## 목차

- [GitHub 릴리스](#github-릴리스)
- [컨테이너 이미지](#컨테이너-이미지)
- [배포 자산 존재 확인](#배포-자산-존재-확인)
- [리눅스 배포판 패키지](#리눅스-배포판-패키지)
- [클라우드 관리형 버전](#클라우드-관리형-버전)
- [깨지는 변경 확인](#깨지는-변경-확인)
- [레이트 리밋](#레이트-리밋)

## GitHub 릴리스

GitHub Actions, docker/compose, 각종 CLI 도구가 여기에 해당한다.

```bash
curl -s https://api.github.com/repos/<owner>/<repo>/releases/latest \
  | grep -m1 '"tag_name"' | sed 's/.*: "//;s/".*//'
```

여러 개를 한 번에 확인할 때는 묶는다. 조회 자체는 빠르므로 개별 호출로 나눌 이유가 없다.

```bash
for repo in actions/checkout docker/build-push-action aws-actions/configure-aws-credentials; do
  tag=$(curl -s "https://api.github.com/repos/$repo/releases/latest" \
        | grep -m1 '"tag_name"' | sed 's/.*: "//;s/".*//')
  printf '%-45s %s\n' "$repo" "${tag:-조회실패}"
done
```

`releases/latest`는 프리릴리스를 건너뛴다. 프리릴리스까지 봐야 하면 `/releases`를 쓰고 첫 항목을 본다.

일부 저장소는 릴리스를 만들지 않고 태그만 단다. 그럴 때는 `/tags`를 쓴다.

```bash
curl -s https://api.github.com/repos/<owner>/<repo>/tags | grep -m1 '"name"'
```

## 컨테이너 이미지

Docker Hub 공식 이미지(`library/`)와 사용자 이미지의 경로가 다르다.

```bash
# 공식 이미지 — postgres, node, eclipse-temurin 등
curl -s "https://hub.docker.com/v2/repositories/library/postgres/tags?page_size=100&name=18" \
  | python3 -c "import sys,json;print('\n'.join(t['name'] for t in json.load(sys.stdin)['results']))"

# 사용자/조직 이미지
curl -s "https://hub.docker.com/v2/repositories/<org>/<image>/tags?page_size=100"
```

레지스트리를 가리지 않고 특정 태그의 존재와 아키텍처를 확인하려면 매니페스트를 본다. arm64 전환 시 필수 확인이다.

```bash
docker manifest inspect <이미지>:<태그> | grep -A2 platform
```

이미지 안의 실제 내용물(셸 종류, curl 유무)은 조회로 알 수 없다. 직접 실행한다.

```bash
docker run --rm <이미지>:<태그> sh -c 'readlink -f /bin/sh; command -v bash curl wget'
```

## 배포 자산 존재 확인

**핀한 버전이 낡았는지보다 존재하는지가 더 급하다.** 없는 태그는 실행 시점에 바로 실패한다.

```bash
# 릴리스 태그 페이지
curl -s -o /dev/null -w '%{http_code}\n' https://github.com/<owner>/<repo>/releases/tag/<태그>

# 다운로드 자산 (리다이렉트를 따라가야 하므로 -L)
curl -sIL -o /dev/null -w '%{http_code}\n' \
  https://github.com/<owner>/<repo>/releases/download/<태그>/<자산이름>
```

아키텍처별 자산 이름 규칙이 프로젝트마다 다르다(`x86_64`/`amd64`, `aarch64`/`arm64`). 스크립트가 `$(uname -m)`으로 이름을 조립한다면 두 아키텍처 모두 200이 나오는지 확인한다.

## 리눅스 배포판 패키지

패키지가 존재하는지, 시스템 파이썬이 외부 관리 상태인지는 해당 배포판 이미지에서 직접 확인하는 게 가장 확실하다.

```bash
docker run --rm amazonlinux:2023 sh -c 'dnf -y -q info <패키지> 2>&1 | head -20'
docker run --rm amazonlinux:2023 sh -c 'ls /usr/lib/python3*/EXTERNALLY-MANAGED 2>/dev/null && echo "PEP 668 적용됨"'
```

## 클라우드 관리형 버전

RDS 엔진 버전, EKS 버전처럼 클라우드가 정하는 목록은 CLI로 조회한다. 자격증명이 있을 때만 가능하다.

```bash
aws rds describe-db-engine-versions --engine postgres \
  --query 'DBEngineVersions[].EngineVersion' --output text
aws ec2 describe-images --owners amazon \
  --filters 'Name=name,Values=al2023-ami-*-arm64' \
  --query 'sort_by(Images,&CreationDate)[-1].[Name,ImageId]' --output text
```

자격증명이 없으면 조회 불가로 보고하고 사용자에게 확인을 요청한다.

## 깨지는 변경 확인

메이저를 올리기 전에 릴리스 노트를 읽는다. 입력 이름 변경, 기본값 변경, 런타임 요구사항 상승이 주로 문제가 된다.

```bash
curl -s https://api.github.com/repos/<owner>/<repo>/releases/tags/<새태그> \
  | python3 -c "import sys,json;print(json.load(sys.stdin)['body'])"
```

메이저를 여러 단계 건너뛸 때는 중간 메이저의 노트도 본다. v5에서 v8로 갈 때 깨지는 변경은 v6이나 v7에 적혀 있을 수 있다.

## 레이트 리밋

GitHub API는 미인증 시 IP당 시간당 60회다. 남은 횟수를 확인할 수 있다.

```bash
curl -s https://api.github.com/rate_limit | grep -m1 remaining
```

`gh` CLI가 인증돼 있으면 훨씬 넉넉하다.

```bash
gh api repos/<owner>/<repo>/releases/latest --jq .tag_name
```

리밋에 걸리면 추측으로 채우지 말고 확인 불가로 보고한다.