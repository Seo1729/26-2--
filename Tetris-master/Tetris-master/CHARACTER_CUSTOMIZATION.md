# 똥피하기 외형 데이터와 적용

`Dodger`는 한 칸 단위 위치, 2칸 점프, 중력, 압사와 상태(대기/걷기/점프/낙하/압사)를 관리한다.
고정 블록과 현재 낙하 블록을 기준으로 충돌한다. `drawX/drawY`는 표시 위치만 보간한다.
게임판의 20ms 타이머, 80ms 물리 갱신, 400ms 블록 낙하, 입력 반복 간격은 변경하지 않았다.
걷기 이미지는 실제 이동 시 두 프레임을 교대하며, `DodgerSprite`가 상태별 PNG를 선택한다.

## 초기 제공 항목

- 스킨 `default`: 기존 `/dodge/images/dodger_*.png` 6개.
- 모자/의상 `none`: 미착용. 이미지 아이템이 아니다.
- 별도 스킨, 모자, 의상 이미지는 현재 없으므로 선택 목록에 등록하지 않았다.

`CharacterItem`은 분류, ID, 표시 이름, 상태별 클래스패스 경로를 정의한다.
`CharacterAppearance`는 선택한 스킨/모자/의상 ID만 보관한다.
`CharacterManager`는 목록, 이미지 검증과 캐시, 선택, 저장/불러오기를 맡는다.
게임 능력치나 충돌 크기는 외형 데이터에 포함하지 않는다.

## 선택과 저장 API

```java
CharacterManager manager = CharacterManager.getInstance();
manager.getItems(CharacterItem.Category.SKIN);
manager.selectSkin("default");
manager.selectHat(CharacterAppearance.NONE);
manager.selectOutfit(CharacterAppearance.NONE);
manager.save(); // IOException을 UI에서 처리할 수 있다.
```

선택은 메모리에 즉시 반영되고 다음 그리기부터 적용된다. 저장은 `save()`를 명시적으로 호출한다.
기본 파일은 사용자 홈의 `.jbnu-tetris/character-appearance.properties`이며 키/음량 파일과 별개이다.
생성 시 자동 로드하며, `load()`로 다시 읽을 수 있다. 알려지지 않은 ID는 기본 스킨/미착용으로 복구한다.
저장 실패는 호출자에게 전달하며 기존 파일을 보호하기 위해 임시 파일과 교체를 사용한다.
외형 선택 UI는 이번 단계에 포함하지 않았다.

## 실제 아이템 추가

1. 실제 PNG를 리소스에 추가한다. 모든 그림은 오른쪽을 바라보도록 작성한다.
2. `CharacterItem`에 안정적인 ID와 분류, 상태별 절대 클래스패스 경로를 정의한다.
3. 관리자에 `register(item)`한다. `idle`은 필수이며, 제공한 경로가 누락되거나 이미지로 읽히지 않으면 등록을 거부한다.
4. 재시작 시에도 복원하려면 확장 목록을 `CharacterManager(Path, Collection<CharacterItem>)`에 전달하여 로드 전에 등록한다.
   공유 관리자 사용 시에는 프로그램 시작 때 등록하고 `load()`를 다시 호출한다.

허용 프레임은 `idle`, `walk1`, `walk2`, `jump`, `fall`, `crushed`이다.
모자/의상은 전체 캐릭터 캔버스와 같은 위치에 정렬된 투명 PNG로 작성한다.
레이어 순서는 스킨 → 의상 → 모자이며 동일한 위치·크기·좌우 반전을 사용한다.
빠진 걷기/점프/낙하 프레임은 기존 스프라이트와 같은 대체 규칙을 사용한다.
압사 프레임 없는 모자/의상은 압사 시 숨긴다. 기본 스킨의 이미지 누락 시 기존 코드 그림을 유지한다.

`GamePanel.getCharacterManager()`로 해당 게임의 관리자를 얻을 수 있다.
테스트나 별도 카탈로그 사용 시 `new GamePanel(manager)`로 주입한다.
외형 변경은 `Dodger.reset()` 및 게임 재시작과 독립적이다.

`tests/CharacterManagerCheck.java`는 실제 기존 PNG를 테스트 전용 레이어로 재사용한다.
이 테스트 항목은 운영 카탈로그에 추가되지 않으며 실제 모자/의상이라고 표시하지 않는다.
