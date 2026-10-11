# 3D Tetris review

검토 범위: src의 전체 Java 파일. 목표 사양의 실행 경로는 Game3D → Game3DFrame → Game3DPanel이며, 기존 2D GameFrame은 별도 실행 경로이다.

## 확인 결과

| 항목 | 결과 및 조치 |
| --- | --- |
| 컴파일 | OpenJDK 25.0.2, UTF-8로 전체 src와 tests 컴파일 성공. 실제 out/production/26-2--도 갱신. |
| 런타임 | 3D 상태 전이, spawn 실패, pause/restart, timer를 회귀 검증. 실제 출력 폴더의 이미지 48개 로딩 성공. |
| 배열 범위 | 3D canPlace는 long 좌표 계산 후 범위를 검사하므로 배열 접근 전에 경계를 차단한다. 기존 2D 회전 후보 범위 검사 누락 수정. |
| 충돌 | 이동·회전·자동 낙하·Ghost는 Board3D.canPlace를 공유한다. Hard Drop도 Ghost의 착지 계산을 사용하도록 통합했다. |
| 회전 | X/Y/Z 회전 행렬, 네 번 회전의 원상 복구, 혼합 회전, 기준점 유지, 충돌 시 원상 유지 검증 통과. |
| 층 삭제 | 3×3 전부 채워졌을 때만 삭제. 14층의 완성/미완성 조합 16,384개에서 점유 상태와 종류 보존 검증 통과. 삭제 후 다음 spawn 순서 유지. |
| 키 입력 | press 후 Ctrl/Alt/Meta 등을 누르고 release하면 해제가 누락될 수 있었다. 모든 수정 키 조합의 release를 같은 해제 동작에 연결했다. 사이드바 스크롤바가 Space 포커스를 가로채지 않도록 했다. |
| 카메라/좌표 | 마우스 조작은 투영 변수만 변경. 세계 좌표와 게임 진행은 변경하지 않는다. TOP/NEXT는 고정된 XY 방향이며 Ghost도 세계 좌표에서 계산된다. |
| 중복 | Hard Drop/Ghost 착지 계산 통합. 기존 2D useItem/attackItem 중복 제거. |
| 책임 | UI의 셀 수 기반 점수 추정 제거. Game3D가 실제 clearCompletedLayers 반환값을 누적하고 SCORE를 제공한다. 공통 색상을 BlockColors로 분리해 TOP/NEXT/UI가 메인 렌더러의 색상 함수에 의존하지 않게 했다. |

## 수정한 오류

- UI를 늦게 생성하거나 재생성하면 점수·삭제 층 수가 0으로 초기화되던 문제: 게임에서 기록을 유지하고 UI는 조회한다.
- UI 밖에서 restart를 호출할 때 UI 자체 추정과 어긋날 수 있던 문제: 게임의 카운터를 restart에서 초기화한다.
- 수정 키가 달라진 key release 누락: 모든 수정 키 조합을 등록한다.
- 기존 2D 빈 아이템 목록 접근 및 대상 없는 아이템 사용: 먼저 검사한다.
- 기존 2D 바닥/벽 근처 회전의 배열 범위 초과: 후보 사각형 범위를 먼저 검사한다.
- 기존 2D 과도한 fast 아이템으로 낙하 간격이 0 이하가 되던 문제: 적용 전 간격을 확인한다.
- 기존 2D 아이콘 URL의 고정 길이 substring으로 쿨다운 효과를 잘못 복구하던 문제: 실제 아이콘 객체를 비교한다.
- 기존 2D 창 생성은 Swing EDT에서 수행하도록 변경했다.

점수 규칙은 기존 표시와 동일하게 삭제 층당 100점이다.

## 검증

NextBlockCheck, GameUICheck, GhostPieceCheck, TopViewCheck, RenderingCheck, RotationCheck, LayerClearCheck, HardDropCheck, GameOverCheck, BlockLockCheck 모두 통과했다. GameUICheck에는 UI 재생성, 외부 restart, Ctrl+Alt 상태에서 Space 해제 후 재입력 검증을 추가했다. 빌드 출력 폴더의 48개 이미지도 별도로 확인했다.

## 범위와 제한

- I 블록 타입과 회전은 구현되어 있지만 초기 XY 폭이 4여서 3×3 보드의 생성 목록에서는 제외되어 있다. 기존 동작을 유지했다. I까지 출현시키려면 세로 초기 방향 등 별도의 생성 규칙이 필요하다.
- 기존 2D GameFrame에는 게임·아이템·시간 스레드에서 Swing 컴포넌트를 직접 변경하는 구조가 남아 있다. 3D 실행 경로에서는 이 클래스를 사용하지 않으며, 3D 자동 낙하와 UI 갱신은 Swing Timer로 EDT에서 동작한다. 기존 2D의 스레드 구조 전체를 이번 3D 검토에서 재작성하지는 않았다.
- 회귀 테스트는 headless 렌더링 및 이벤트 검증이다. 실제 데스크톱에서 장시간 플레이하는 수동 검증을 대신하지 않는다.
