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
