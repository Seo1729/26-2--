# Poop(똥피하기) 모드 리팩토링 계획

> 기준 코드: `Tetris-master/Tetris-master/src/dodge` (2026-10-31 수정본)
> 아직 코드는 수정하지 않았고, 리팩토링(이하 Ref) 전에 구조와 순서를 정리한 문서입니다.

## 1. 폴더 구조 재검토 및 재설계

### 현재 문제점
- 원본·갤러그·3D 파일이 `src` 바로 아래 **기본 패키지**에 섞여 있다. 기본 패키지 클래스는 다른 패키지에서 import할 수 없어서, 폴더를 나누는 순간 서로 부를 수 없다.
- `dodge` 안에 지금 쓰지 않는 예전 파일 6개(`GameBoard`, `TetrisPlayer`, `InputHandler`, `GameRules`, `Theme`, `Tetrominoes`)가 남아 있고, 실제 동작은 `GamePanel` 한 파일(552줄)에 몰려 있다.
- `3D`는 숫자로 시작해서 자바 패키지 이름으로 쓸 수 없다. 폴더 이름은 `threed`처럼 바꿔야 한다.

### 제안 구조 (폴더 이름 = 패키지 이름)
```
src/
 ├ origin/   GameFrame, MainMenuPanel, 일반 GamePanel, HpSystem, LevelManager … (+ common/)
 ├ galag/    GalagaLayer
 ├ threed/   Game3D, Game3DFrame, Board3D …
 ├ vscpu/    컴퓨터와 대전 (vsCPU 문서 참고)
 └ poop/
    ├ PoopMode, PoopModeMain        ← 모드 진입점 (지금 DodgeMode)
    ├ game/       GameBoard, Shape, TetrisPlayer, Dodger, GameRules   ← Swing 없는 순수 로직
    ├ input/      InputHandler
    ├ view/       GamePanel(루프+그리기), InfoPanel, DodgerSprite, Theme
    ├ character/  CharacterManager, CharacterItem, CharacterAppearance, CharacterCustomizationPanel
    └ images/
```

## 2. Ref 전 코드의 RFIV 증상

| 증상 | 근거 (현재 코드) |
|---|---|
| **Rigidity (경직성)** | 보드 크기 `10`, `22`, 제한시간 `120000`, 목표 `10`줄, 골 `22-4`가 `GamePanel`·`InfoPanel`·`Dodger`에 숫자로 15군데 넘게 들어 있다. 하나를 바꾸려면 세 파일을 모두 고쳐야 한다. 승패도 `result` 정수 0~5라서 승리 조건을 하나 추가하면 `refresh()`, `paintComponent()`, 전적 계산, 테스트를 모두 건드려야 한다. |
| **Fragility (취약성)** | `Dodger`는 `GamePanel`의 `isFree()`·`occupies()`를 부르고, `GamePanel`은 `dodger.x`, `dodger.crushed`를 직접 읽는 순환 의존이다. `InfoPanel`도 `game.playElapsed`, `game.nextPiece`, `game.dodger.y`를 필드로 바로 읽어서 `GamePanel` 내부를 바꾸면 정보판과 `DodgeOutcomeCheck` 테스트가 같이 깨진다. 반복 키 목록이 `keyPressed`와 `actionPerformed` 두 곳에 따로 있어 한쪽만 고치면 버그가 난다. |
| **Immobility (부동성)** | 보드 판정(`canPlace`, `removeFullLines`)이 JPanel 안에 있어서 다른 모드나 테스트에서 재사용할 수 없다. `drawSquare`는 `GamePanel`·`InfoPanel`·`Theme`에 3벌 복사되어 있고, 블록 모양도 `dodge.Shape`·원본 `Blocks`·`vsai.Tetromino`로 3벌이다. |
| **Viscosity (점착성)** | `Shape`를 정수 방식으로 바꿀 때 쓰지 않는 `TetrisPlayer`를 지우지 않고 `.ordinal()`로 땜질한 흔적이 있다. 새 기능을 넣을 때 `GamePanel`에 필드를 하나 더 붙이는 것이 가장 쉬운 구조라 클래스가 계속 커진다. 메뉴로 돌아가기(`backToMenu`)도 주석 처리되어 있다. |

## 3. SOLID 원칙에 따른 수정 대상

| 원칙 | 수정 대상 | 바꾸는 방법 |
|---|---|---|
| **SRP** | `GamePanel` (1순위) | 보드, P1 조작, 키 입력, 승패, 전적, 그리기, 효과음, 설정창 8가지를 `GameBoard`, `TetrisPlayer`, `InputHandler`, `GameRules`, `GamePanel`(그리기)로 나눈다. `Dodger`의 효과음도 분리한다. |
| **OCP** | `result` 정수 if문, `act()`의 키 if-else, `DodgerSprite` 상태 숫자 0~4 | `GameRules.Result` enum, `Map<KeyAction, Runnable>`, `Pose` enum으로 바꿔서 조건·키·모습을 추가해도 기존 코드를 고치지 않게 한다. |
| **LSP** | (현재 상속이 거의 없음) | 모드 공통 규약을 만들 때 "`stop()`하면 타이머와 BGM이 반드시 멈춘다" 같은 약속을 모든 모드가 지키게 한다. 갤러그는 패널이 아니라 레이어라 특히 주의한다. |
| **ISP** | `InfoPanel`, `Dodger` | `InfoPanel`은 읽기 전용 `GameStatus` 인터페이스만, `Dodger`는 "이 칸이 막혔나?"만 묻는 `CollisionMap` 인터페이스만 받게 한다. |
| **DIP** | `Dodger`→`GamePanel`, 로직→`SoundManager`, `GameFrame`→`dodge.DodgeMode` | 각각 `CollisionMap`, 게임 이벤트 리스너, `GameMode` 인터페이스에 의존하게 바꾼다. |

## 4. 리팩토링 순서

원칙: **의존받는 쪽(잎)부터, 로직 다음 화면, Poop 안쪽 먼저 하고 Origin은 마지막에 한 번에.** 단계마다 컴파일하고 `DodgeOutcomeCheck`를 실행해 동작이 그대로인지 확인한다.

### Poop 내부 (전부 `dodge` 폴더 안이라 팀원과 충돌 없음)

보드 격자(GameBoard) -> 테트리스 블록 조작(TetrisPlayer) -> 고양이 이동·물리(Dodger) -> 승패 판정·전적(GameRules) -> 2인 키 입력(InputHandler) -> 효과음 연결(PoopSounds) -> 게임 화면·정보판(GamePanel, InfoPanel, Theme) -> 캐릭터 그림(DodgerSprite) -> 모드 진입(DodgeMode)

- 예전 파일들은 지우지 않고, 같은 이름에 새 내용을 덮어쓰는 자리로 쓴다.
- `DodgerSprite`와 `Character*`는 다른 팀원이 작업한 코드라 뒤쪽에 두고, 손대기 전에 미리 공유한다.

### 모드 리팩토링 (Origin 폴더 충돌 방지)

모드 공통 규약(GameMode) -> 똥피하기 모드(PoopMode) -> 컴퓨터 대전 모드(VsCpuMode) -> 3D 모드(Game3DFrame) -> 갤러그 모드(GalagaLayer) -> 모드 전환(GameFrame) -> 메인 메뉴(MainMenuPanel) -> 폴더 이동(package 선언)

- `GameMode`는 origin에 **새 파일만 추가**하므로 충돌이 나지 않는다.
- 각 모드가 규약을 구현하는 단계는 각자 자기 폴더에서 하므로 동시에 진행해도 된다.
- `GameFrame`(1974줄, 여러 명이 고친 파일)과 `MainMenuPanel`은 **한 사람이 짧은 브랜치 하나로** 처리한다. `startDodge`/`startVsAi`/`start3D`를 `startMode(GameMode)` 하나로 합치면 이후 모드를 추가해도 Origin을 고치지 않아도 된다.
- 폴더 이동은 **로직 변경 없이 이동만 하는 PR**로 따로 내고, 모두 머지한 뒤 진행한다. `git mv`로 옮겨 기록을 남긴다. 이동 순서는 poop -> threed -> galag -> origin (갤러그가 원본 `GamePanel`을 쓰므로 origin이 마지막). 이미지 경로 `/dodge/images/...`도 함께 바꾼다.
