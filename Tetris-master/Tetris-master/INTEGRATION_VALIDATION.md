# 홀드·환경 설정·캐릭터 관리 통합 검증

검증일: 2026-10-11. Windows / OpenJDK 25.0.2.
모든 검증은 임시 컴파일 디렉터리와 테스트별 임시 `user.home`에서 실행했다.
실제 사용자 설정을 테스트 대상으로 사용하지 않았으며 왼쪽 이동 키는 검증 종료 후에도 37(←)이다.

## 실행 결과

42개 운영 Java 소스와 25개 테스트 소스를 모두 포함한 `javac -encoding UTF-8` 컴파일: 통과.
IntelliJ의 기존 제외 목록에 의존하지 않고 컴파일했다. 테스트의 기존 `sun.misc.Unsafe` 사용 경고는 남아 있다.

| 요구사항 | 실제 실행한 검증 | 결과 |
|---|---|---|
| 기존 실행·메뉴 | GameFrame.main, 환경 설정/캐릭터 관리 열기·닫기, 일반 모드/똥피하기 버튼, 원래 카운트다운 | 통과, 실제 Swing 창 |
| 2D 테트리스 | 좌우 이동, 회전, 한 칸 낙하, 하드 드롭/고정/다음 생성, 기존 점수식, 줄 압축 | 통과, 격리 보드 fixture |
| 테트리스 게임 오버 | 홀드 빈 슬롯/교환 모두의 생성 위치 충돌, GAME OVER 및 진행 중단 | HoldCheck 통과 |
| 홀드·지정 키 | 교환/방향 초기화/다음 블록 보존/재사용 제한/고정 후 재허용, 실제 창 J 키 홀드와 기존 C 키 비활성 | 통과 |
| 환경 설정 | 두 음량 슬라이더, 실시간 값, 저장/복원, 손상 설정, 중복·예약 키, 모달 입력 차단 | 통과 |
| 음량 | Clip 대역으로 gain/PCM 스케일·음소거·재생 재개·오디오 장치 오류 복구 | SoundVolumeCheck 통과 |
| 설정 재실행 | 별도 Java 실행으로 저장된 홀드/똥피하기 키 복원 | KeyBindingCheck restart 통과 |
| 똥피하기 | 좌우 경계, 블록 충돌, 점프/중력/압사, 줄 제거 수, 목표 줄/시간/높이/보드 포화 승패, 승리 수 중복 방지 | 통과 |
| 캐릭터 외형 | 기본 PNG 일치, 스킨 변경, 의상/모자 겹침, 보간 위치/좌우 반전/모든 상태, 적용·취소·저장 실패 | 통과 |
| 외형 재실행 | 새 Java 프로세스에서 스킨·모자·의상 ID 복원 후 게임판 다시 렌더링 | 통과 |
| 포커스 | 설정창 ESC/닫기 후 2D·똥피하기 키 입력, 메뉴 설정·캐릭터 창 종료, 메뉴→게임 | 통과, 실제 창 |
| 이미지 | ImageSource의 48개 ImageIcon 크기, 기본 캐릭터 PNG, 전체 캐릭터 리소스 누락 시 대체 그림과 게임 진행 | 통과 |
| 사운드 데이터 | 모든 Sound/Bgm 값의 합성 PCM 데이터 생성 | 통과, 외부 사운드 파일 방식 아님 |

## 실행한 테스트

총 25개 테스트 프로그램. 캐릭터 및 키 복원은 별도 프로세스에서도 실행했다.

- 2D·메뉴·리소스: HoldCheck, IntegrationGameplayCheck, SettingsWindowCheck, MenuWindowCheck, ResourceLoadingCheck
- 설정·사운드: GameSettingsCheck, KeyBindingCheck, EnvironmentSettingsCheck, SoundVolumeCheck
- 똥피하기·캐릭터: DodgeKeyCheck, DodgeOutcomeCheck, CharacterManagerCheck, CharacterCustomizationCheck, CharacterGameplayCheck, CharacterMissingResourceCheck
- 3D 기존 회귀: RotationCheck, HardDropCheck, BlockLockCheck, LayerClearCheck, GameOverCheck, NextBlockCheck, GhostPieceCheck, GameUICheck, RenderingCheck, TopViewCheck

LayerClearCheck는 16,384개 층 조합을 검사한다. 3D 회귀는 게임 상태와 offscreen 렌더링 검사이며 실제 3D 창 조작 검사는 아니다.
예상된 설정 저장 실패와 리소스 누락은 오류 복구 테스트 입력이다.
최종 SettingsWindowCheck와 MenuWindowCheck는 종료 코드 0이며 EDT 예외가 없었다.

## 발견 및 최소 수정

전체 소스 컴파일은 처음 10개 오류로 실패했다. 기존 IntelliJ 제외 목록에 있던 클래스 4개가 현재 API와 불일치했다.

- src/dodge/GameBoard.java: int 모양 번호를 기존 Tetrominoes 열거형으로 변환.
- src/dodge/TetrisPlayer.java: 비어 있는 모양의 enum/int 비교·전달 및 현재 Dodger 좌표 참조 수정.
- src/dodge/GameRules.java: 현재 Dodger의 압사 상태·좌표 참조 수정.
- src/dodge/InputHandler.java: 현재 GamePanel의 시작/일시정지 상태로 실행 여부 확인.

이 클래스들은 현재 게임 화면의 실행 경로에서 사용되지 않는다. 기존 게임의 이동·점수·승패 로직과 IntelliJ 제외 설정은 변경하지 않았다.

테스트 추가/확장: IntegrationGameplayCheck.java, DodgeOutcomeCheck.java, MenuWindowCheck.java,
ResourceLoadingCheck.java, SettingsWindowCheck.java(실제 사용자 지정 홀드 입력 검증).
새 테스트의 다음 블록 초기화와 4초 카운트다운 대기·낙하 스레드 정리도 보완했다.

이전 캐릭터 검증에서 남아 있던 변경: CharacterManager.java(누락된 추가 아이템만 건너뜀),
CharacterGameplayCheck.java(새 프로세스 외형 복원), CharacterMissingResourceCheck.java,
CHARACTER_CUSTOMIZATION.md. 이번 테스트는 해당 수정도 포함해 수행했다.

## 미검증·남은 제약

- 실제 스피커 청취, 물리 오디오 장치의 음량 체감: 미검증. 합성 데이터와 음량 제어 로직은 검증했다.
- 게임→메인 메뉴 복귀: 현재 backToMenu가 주석 처리돼 있고 복귀 UI가 없다. 기능 추가 없이 현 상태를 기록한다.
- 갤러그·3D 실제 메뉴 진입과 창 전환 포커스: 미검증. 관련 키 매핑과 3D 회귀 테스트는 통과했다.
- 장시간 플레이, 다양한 화면 배율·해상도, 키보드의 물리 동시 입력 제한: 미검증.
- 추가 스킨·모자·의상은 운영 카탈로그에 실제 리소스가 없다. 투명 테스트 PNG로 적용 경로를 검증했으며 실제 의상 디자인 품질은 미검증.
- 기존 테스트는 일부 보드를 Unsafe로 생성한다. 실제 초기화/메뉴 진입은 MenuWindowCheck로 별도 검사했으나 모든 상태를 실제 플레이로 재현한 것은 아니다.

게임 기능 삭제, 새 메뉴 복귀 기능 추가, 사용자 설정 초기화는 수행하지 않았다.
검증 이후 변경 사항은 아직 커밋하지 않았다.
