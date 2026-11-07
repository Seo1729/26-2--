# vsCPU(컴퓨터와 대전) 모드 리팩토링 계획

> 기준 코드: `Tetris-master/Tetris-master/src/vsai` (2026-10-31 수정본, 12개 파일)
> 아직 코드는 수정하지 않았고, 리팩토링(이하 Ref) 전에 구조와 순서를 정리한 문서입니다.

## 0. 이름 변경: vsAI → vsCPU

상대가 학습된 AI가 아니라 `HeuristicBrain`의 점수 공식(높이 합·지운 줄·구멍·울퉁불퉁함)으로 자리를 계산하는 방식이므로 **CPU**라고 부르는 것이 정확하다. 메인 메뉴 버튼 문구도 이미 "컴퓨터와 대전"이다.

| 대상 | 변경 |
|---|---|
| 폴더·패키지 | `vsai` → `vscpu` (12개 파일의 `package` 줄) |
| 클래스 | `AiBrain`→`CpuBrain`, `AiController`→`CpuController`, `VsAiMode`→`VsCpuMode`, `VsAiMain`→`VsCpuMain` (`HeuristicBrain`은 유지) |
| 필드·상수·주석 | `ai`, `aiView`, `AI_STEP_MS`, `AI_COLOR`, 스레드 이름 `"vsai-brain"`, 주석 속 "AI" |
| 삭제 | `ClaudeBrain` (API 연결 자리만 있는 빈 뼈대) |
| Origin 영향 | `GameFrame`의 `vsai.VsAiMode` 필드와 `startVsAi()` 2군데 |
| 팀 문서·설정 | `PR_REVIEW_GUIDE.md` 컴파일 명령의 `src/vsai/*.java`, Git 훅 허용 폴더 목록, 브랜치 이름 |

## 1. Ref 전 코드의 RFIV 증상

| 증상 | 근거 (현재 코드) |
|---|---|
| **Rigidity (경직성)** | 조작키가 `VsAiMode`에 `VK_LEFT`, `VK_SPACE` 등으로 고정되어 있어, 똥피하기처럼 팀 공통 키 설정(`KeySettings`)에 붙이려면 모드 클래스를 직접 고쳐야 한다. |
| **Fragility (취약성)** | 일시정지·승패 문구를 두 `BoardView`에 각각 `setOverlay()` 한다. 하나를 빠뜨리면 한쪽 판에만 표시된다. `AiController.step()`은 회전이 막히면 바로 하드드롭하므로 `PlayerBoard`의 벽 킥(`KICKS`)을 바꾸면 CPU 행동도 같이 바뀐다. |
| **Immobility (부동성)** | `Tetromino`가 `java.awt.Color`를 가지고 있어 순수 로직이 화면 라이브러리에 묶여 있다. `drawCell`과 색 상수는 똥피하기 `Theme`과 거의 같은 복사본이다. |
| **Viscosity (점착성)** | `src/vsai` 안에 `.idea`, `out`, `vsai.iml`이 들어 있다(팀 규칙 7번 대상). `ClaudeBrain`처럼 쓰지 않는 뼈대도 남아 있다. |
