## 명령어

백엔드는 `backend/`에서 실행한다. 통합 테스트는 Testcontainers를 쓰므로 Docker가 떠 있어야 한다.

```bash
./gradlew unitTest          # *UnitTest만
./gradlew integrationTest   # *IntegrationTest만
./gradlew bootRun           # SPRING_DATASOURCE_URL/USERNAME/PASSWORD 필요
```

프론트엔드는 테스트가 없다. `npm run lint`(biome)와 `npm run typecheck`가 CI 게이트다.

PR은 `scripts/pr-draft.sh [-y] [제목]`으로 만든다. 커밋이 하나면 그 제목을 쓰고, 여러 개면 제목을 인자로 줘야 한다.

## 개발 방식

기능 추가와 동작 변경은 TDD로 한다. 테스트를 먼저 쓰고 assertion이 실패하는 것을 확인한 뒤에 구현한다. 컴파일 에러는 테스트 실패가 아니다. 최소 시그니처로 컴파일만 통과시킨 뒤 실패를 확인한다.

설정 파일과 마이그레이션 SQL, 로그 문구와 이름 변경은 테스트를 새로 쓰지 않는다. 동작이 그대로인 리팩터링도 새로 쓰지는 않되, 기존 테스트가 통과하는 것을 전후로 확인한다.

## 테스트

테스트 클래스 이름은 `*UnitTest` 또는 `*IntegrationTest`로 끝나야 한다.

통합 테스트는 `support`의 `AbstractIntegrationTest` 또는 `AbstractApiIntegrationTest`를 상속한다. 수집 테스트는 시드 데이터를 갈아엎으므로 `CollectionContainerConfig`와 `@Sql` 정리 스크립트를 따로 쓴다.

`PublicAddressOnlyPolicy`가 사설·루프백 주소를 막으므로, 스텁 서버를 띄우는 통합 테스트에는 `TestAddressPolicyConfig`를 `@Import`해야 한다.

단위 테스트는 Mockito와 AssertJ에 `@Nested`, 한국어 `@DisplayName`을 쓴다. 메서드 이름은 영어다.

## 코드 규약

`app`은 `app/port/out` 인터페이스에만 의존하고 구현은 `infra`에 둔다. 새 외부 연동은 `port/out`에 인터페이스부터 만든다. `domain`은 Spring·HTTP·JDBC를 모른다. 응답 DTO는 `api`에 두고 `from(도메인)` 정적 팩터리로 변환한다.

도메인 예외는 sealed `DomainException` 계층이고 `ApiExceptionHandler`가 갈래별로 상태 코드를 매핑한다. 새 예외는 기존 갈래를 상속시키고, 새 갈래가 필요하면 permits와 핸들러의 switch를 함께 넓힌다.

시각은 항상 주입받은 `Clock`에서 얻는다. `Instant.now()`를 직접 부르지 않는다.

커밋 메시지와 PR, 주석, `@DisplayName`은 한국어로 쓴다. 커밋 제목은 `feat:`, `fix:`, `refactor:`, `test:`, `chore:`, `ci:` 접두사를 쓴다.

`updated_at`은 트리거가 갱신하므로 SQL에서 직접 넣지 않는다.
