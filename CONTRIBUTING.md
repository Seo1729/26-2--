# 팀 협업 가이드 (CONTRIBUTING)

전북대학교 소프트웨어공학과 소스코드분석 1조 — Tetris 개선 프로젝트

이 문서는 팀원 3명이 같은 코드를 안전하게 함께 수정하기 위한 Git/GitHub 사용 규칙입니다. 작업을 시작하기 전에 한 번 끝까지 읽어 주세요.

---

## 1. 핵심 개념

| 용어 | 뜻 | 명령어 |
|---|---|---|
| **commit** | 변경 내용을 **내 PC(로컬 저장소)** 에 스냅샷으로 저장합니다. 아직 GitHub에는 올라가지 않습니다. | `git add .` → `git commit -m "메시지"` |
| **push** | 내 PC의 커밋을 **GitHub(원격 저장소)** 에 업로드합니다. | `git push` |
| **pull** | GitHub의 최신 커밋을 내 PC로 가져옵니다. | `git pull` |
| **branch** | 독립된 개발 라인입니다. 내 브랜치에서 작업해도 다른 브랜치는 영향을 받지 않습니다. | `git checkout -b 이름` |
| **Pull Request (PR)** | 내 브랜치의 변경 내용을 다른 브랜치에 병합해 달라는 요청입니다. 코드 리뷰를 거쳐 병합합니다. | GitHub 웹에서 생성 |

---

## 2. 브랜치 구조

```
main        ← 제출용 안정 버전 (직접 수정 금지)
 └─ develop ← 통합/테스트 (직접 수정 금지)
     ├─ feature/기능명-A  ← 팀원 A 작업
     ├─ feature/기능명-B  ← 팀원 B 작업
     └─ feature/기능명-C  ← 팀원 C 작업
```

| 브랜치 | 용도 | 직접 커밋 |
|---|---|---|
| `main` | 교수님께 제출하는 안정 버전. 실행 테스트를 통과한 코드만 들어갑니다. | ❌ 금지 |
| `develop` | 팀원들의 작업을 합쳐서 테스트하는 통합 브랜치입니다. | ❌ 금지 |
| `feature/*` | 각자 기능 단위로 작업하는 브랜치입니다. | ✅ 여기서만 작업 |

병합 방향은 항상 `feature/*` → `develop` → `main` 입니다.

---

## 3. 최초 1회 세팅

### 3-1. 사전 준비
1. [Git for Windows](https://git-scm.com/download/win) 설치
2. GitHub 계정 준비, **레포 협업자(Collaborator) 초대 메일 수락**
3. JDK 설치 (팀원 모두 **같은 버전** 사용, 정해진 버전이 없다면 JDK 17 권장)

### 3-2. Git 사용자 정보 등록
```powershell
git config --global user.name "이름"
git config --global user.email "GitHub에 등록한 이메일"
git config --global core.autocrlf true
git config --global core.quotepath false
git config --global init.defaultBranch main
```

### 3-3. 레포 내려받기 (clone)
작업할 폴더를 만든 뒤, 그 폴더의 터미널에서 실행합니다. 폴더가 완전히 비어 있다면 끝의 `.`을 붙여 현재 폴더에 바로 받습니다.

```powershell
git clone -b develop https://github.com/Seo1729/26-2-- .
```

> 처음 `git push` 할 때 브라우저 로그인 창이 뜨면 GitHub에 로그인하고 **Authorize**를 누르세요.

---

## 4. 매번 하는 작업 순서

### STEP 1. 최신 코드 받기
```powershell
git checkout develop
git pull
```

### STEP 2. 내 작업 브랜치 만들기
브랜치 이름은 `feature/기능명` 형식으로 짓습니다. 영문 소문자와 하이픈을 사용하세요.
```powershell
git checkout -b feature/hold-block
```

### STEP 3. 코딩 후 커밋
```powershell
git add .
git commit -m "feat: 홀드 블록 기능 추가"
```
커밋은 **작게, 자주** 합니다. 기능 하나가 끝날 때마다 하는 것이 좋습니다.

### STEP 4. GitHub에 올리기 (push)
```powershell
git push -u origin feature/hold-block
```
두 번째 push부터는 `git push`만 입력해도 됩니다.

### STEP 5. Pull Request 생성
1. GitHub 레포 페이지 → **Compare & pull request** 클릭
2. **base: `develop`  ←  compare: `feature/내브랜치`** 인지 반드시 확인
3. 제목과 설명(무엇을 바꿨는지)을 작성하고 **Create pull request**

### STEP 6. 리뷰 후 병합
- 작성자가 아닌 **다른 팀원 1명**이 변경 내용(Files changed)을 확인합니다.
- 문제가 없으면 **Approve → Merge pull request** 를 누릅니다.
- 병합이 끝나면 작업 브랜치는 **Delete branch** 로 지웁니다.

### STEP 7. 다시 develop으로 돌아와 최신화
```powershell
git checkout develop
git pull
```

---

## 5. develop → main 반영 (제출 버전 만들기)

`develop`에서 프로그램이 정상 실행되는지 확인한 뒤에만 진행합니다.

1. GitHub에서 **base: `main` ← compare: `develop`** 으로 PR 생성
2. 팀원 1명 이상 승인 후 Merge
3. 제출 시점에는 태그를 달아 버전을 고정합니다.
   ```powershell
   git checkout main
   git pull
   git tag v1.0
   git push origin v1.0
   ```

---

## 6. 커밋 메시지 규칙

`접두어: 내용` 형식으로 통일합니다.

| 접두어 | 용도 | 예시 |
|---|---|---|
| `feat` | 새 기능 | `feat: 홀드 블록 기능 추가` |
| `fix` | 버그 수정 | `fix: 줄 삭제 시 점수 중복 계산 수정` |
| `refactor` | 동작 변경 없는 구조 개선 | `refactor: Board 클래스 메서드 분리` |
| `docs` | 문서 수정 | `docs: README 실행 방법 추가` |
| `chore` | 설정/기타 | `chore: .gitignore 추가` |

---

## 7. 반드시 지킬 규칙

1. `main`과 `develop`에서 **직접 코딩하거나 커밋하지 않습니다.**
2. 작업을 시작하기 전에 항상 `git checkout develop` → `git pull` 을 먼저 합니다.
3. **같은 파일을 여러 명이 동시에 수정하지 않도록** 작업 분담을 먼저 정합니다.
4. 브랜치는 오래 두지 말고 **1~2일 단위로 PR**을 올립니다. 오래 살아 있는 브랜치일수록 충돌이 커집니다.
5. `git push --force` 는 **절대 사용하지 않습니다.**
6. 컴파일 오류가 나는 코드는 PR을 올리지 않습니다. **PR 전에 직접 실행해서 확인**합니다.
7. 빌드 산출물(`.class`, `out/`)과 IDE 설정(`.idea/`, `.vscode/`)은 커밋하지 않습니다. (`.gitignore`로 제외)

---

## 8. 충돌(conflict)이 났을 때

두 사람이 같은 부분을 수정하면 병합 시 충돌이 납니다. 당황하지 말고 아래 순서로 해결합니다.

```powershell
git checkout feature/내브랜치
git pull origin develop       # 최신 develop을 내 브랜치로 가져오기
```

충돌 파일을 열면 아래와 같은 표시가 있습니다.

```
<<<<<<< HEAD
내가 수정한 내용
=======
상대가 수정한 내용
>>>>>>> origin/develop
```

1. 남길 내용만 남기고 `<<<<<<<`, `=======`, `>>>>>>>` 줄을 모두 지웁니다.
2. 프로그램이 정상 실행되는지 확인합니다.
3. 아래 명령으로 마무리합니다.
   ```powershell
   git add .
   git commit -m "fix: develop 병합 충돌 해결"
   git push
   ```

> 해결이 어렵다면 혼자 고치지 말고 **상대 팀원과 화면을 같이 보며** 해결하세요.

---

## 9. 자주 나는 오류

| 증상 | 해결 |
|---|---|
| `fatal: detected dubious ownership` | USB/외장 드라이브에서 발생합니다. `git config --global --add safe.directory '폴더경로'` (경로는 `/` 로 입력) |
| `git: 'chechout' is not a git command` | 오타입니다. `checkout` 으로 입력하세요. |
| `remote: Permission denied` / `403` | 협업자 초대를 수락했는지 확인하세요. 다른 계정으로 로그인되어 있다면 Windows 자격 증명 관리자에서 `github.com` 항목을 삭제한 뒤 다시 로그인합니다. |
| `Your local changes would be overwritten by merge` | 커밋하지 않은 변경이 있습니다. 먼저 `git add .` → `git commit` 을 하고 `git pull` 하세요. |
| 지금 어느 브랜치인지 모르겠음 | `git branch` (현재 브랜치에 `*` 표시), 또는 `git status` |
| 한글 파일명이 `\354\206...` 처럼 보임 | `git config --global core.quotepath false` |

작업 도중 상태를 확인하고 싶다면 아래 두 명령이 가장 유용합니다.
```powershell
git status          # 현재 브랜치와 변경된 파일 확인
git log --oneline   # 최근 커밋 목록 확인
```

---

## 10. 작업 전 체크리스트

- [ ] `git branch`로 내가 `feature/*` 브랜치에 있는지 확인했다
- [ ] 작업 시작 전에 `develop`을 `pull` 했다
- [ ] 다른 팀원과 수정할 파일이 겹치지 않는다
- [ ] PR 전에 프로그램을 직접 실행해 확인했다
- [ ] PR의 base가 `develop`이다
