# GitHub PR 리뷰 가이드

2026-10-03 · 김민서

팀원이 올린 PR(Pull Request)은 GitHub 웹에서 바뀐 코드를 읽고, 궁금한 줄에 코멘트를 남긴 뒤, Approve(승인) 또는 Request changes(수정 요청)로 리뷰를 제출하면 됩니다. 실제로 돌아가는지는 내 컴퓨터에서 그 브랜치를 받아 실행해 확인합니다.

## 1. 시작 전 준비

리뷰어로 지정받으려면 저장소의 협업자(Collaborator)여야 합니다. 처음 한 번만 하면 됩니다.

1. [github.com](https://github.com)에서 계정을 만들고, 내 아이디를 저장소 주인에게 알려 줍니다.
2. 저장소 주인이 [저장소](https://github.com/Seo1729/26-2--)의 **Settings → Collaborators → Add people**에서 그 아이디를 초대합니다.
3. 초대받은 사람은 GitHub 가입 메일로 온 초대 메일의 **Accept invitation**을 누릅니다. 메일이 안 보이면 [github.com/notifications](https://github.com/notifications)에서도 수락할 수 있습니다.

내 컴퓨터에서 실행까지 해 보려면 [Git](https://git-scm.com/download/win)과 JDK(java, javac 명령이 되는 상태)도 설치되어 있어야 합니다.

## 2. PR 찾고 둘러보기

저장소 상단의 **Pull requests** 탭을 누르면 열린 PR 목록이 나오고, 제목을 누르면 아래 탭 세 개가 보입니다. 리뷰어로 지정되면 GitHub 알림과 메일로도 알려 줍니다.

| 탭 | 보여 주는 것 | 이럴 때 본다 |
| --- | --- | --- |
| Conversation | PR 설명, 남긴 코멘트, 리뷰 기록 | 무엇을 만들었는지, 실행 방법이 뭔지 먼저 읽을 때 |
| Commits | 커밋 목록(작업 단계) | 어떤 순서로 만들었는지 볼 때 |
| Files changed | 바뀐 코드 (초록색 = 추가, 빨간색 = 삭제) | 실제 리뷰를 할 때 |

순서는 Conversation에서 설명을 읽고 → Files changed에서 코드를 보는 것을 권합니다.

## 3. 코드 줄에 코멘트 남기기

Files changed에서 줄 번호에 마우스를 올리면 나오는 파란 **+** 버튼으로 그 줄에 바로 코멘트를 달 수 있습니다.

1. 코멘트를 달 줄의 줄 번호 옆 **+**를 누릅니다. 여러 줄이면 **+**를 누른 채 아래로 끌어서 범위를 고릅니다.
2. 칸에 내용을 씁니다.
3. **Start a review**를 누릅니다. 다른 줄에도 같은 방식으로 남기면 **Add review comment**로 모여서, 제출할 때 한 번에 전달됩니다.
4. **Add single comment**는 그 코멘트 하나만 바로 올립니다. 간단한 질문 하나일 때만 쓰세요.

코멘트는 이렇게 쓰면 작성자가 바로 고칠 수 있습니다.

- 무엇이 문제인지 + 어떻게 했을 때 그랬는지: "A를 누른 채 W를 누르면 총알이 안 나가요"
- 질문은 질문이라고 밝히기: "질문: 여기서 24는 한 칸 크기인가요?"
- 취향 문제는 가볍게: "제안: 이름을 diveTick으로 바꾸면 어떨까요? (안 바꿔도 됨)"

## 4. 리뷰 제출하기

Files changed 오른쪽 위의 초록색 **Review changes**(또는 **Finish your review**) 버튼을 누르고, 전체 의견을 한두 줄 쓴 뒤 아래 세 가지 중 하나를 골라 **Submit review**를 누릅니다.

| 선택 | 뜻 | 이럴 때 고른다 |
| --- | --- | --- |
| Approve | 승인. 머지해도 좋다 | 실행해 보니 동작하고, 꼭 고칠 게 없을 때 |
| Request changes | 수정 요청. 고친 뒤 머지하자 | 컴파일이 안 되거나, 기존 기능이 깨졌거나, 버그가 있을 때 |
| Comment | 의견만 남김 | 질문이나 가벼운 제안만 있을 때 |

자기가 올린 PR에는 Approve를 누를 수 없습니다. 리뷰는 항상 다른 팀원이 합니다.

## 5. 내 컴퓨터에서 직접 실행해 보기

코드만 읽어서는 동작을 알 수 없으니, PR의 브랜치를 받아 실행해 본 뒤 리뷰를 제출합니다. 명령어는 VS Code 터미널(단축키 Ctrl + `)에 한 줄씩 붙여 넣습니다.

**처음 한 번만**: 저장소를 내 컴퓨터로 복사합니다. 끝나면 VS Code의 파일 → 폴더 열기로 생긴 26-2-- 폴더를 엽니다.

```
git clone https://github.com/Seo1729/26-2--.git
```

**리뷰할 때마다**: PR 제목 아래 `wants to merge ... from feature/어쩌고` 부분에 있는 브랜치 이름을 확인하고, 아래 순서대로 실행합니다. 예시는 갤러그 기능의 브랜치 `feature/galag`입니다.

1. GitHub에 올라온 최신 브랜치 목록을 받습니다: `git fetch`
2. 그 브랜치로 옮깁니다: `git switch feature/galag`
3. 프로젝트 폴더로 들어갑니다: `cd Tetris-master\Tetris-master`
4. 컴파일합니다. 아무 메시지 없이 끝나면 성공입니다: `javac -encoding UTF-8 -d out src/*.java src/dodge/*.java`
5. 실행합니다. 이미지가 들어 있는 `images` 폴더를 클래스패스에 꼭 넣어야 하고(`bin`이 아닙니다), 3D만 따로 보려면 마지막을 `Game3D`로 바꿉니다: `java -cp "out;images;src" GameFrame`
6. 확인이 끝나면 내 작업 브랜치나 develop으로 돌아갑니다: `cd ..\..` 후 `git switch develop`

2번에서 오류가 나면 대부분 내가 수정하던 파일을 아직 커밋하지 않아서입니다. 내 작업을 먼저 커밋한 뒤 다시 하세요. `out` 폴더는 빌드 결과물이니 커밋하지 않습니다.

실행해 보면서 확인할 것:

- [ ] PR 설명에 적힌 기능이 설명대로 동작한다
- [ ] 일반 모드 등 기존 기능이 여전히 잘 된다
- [ ] 컴파일 오류가 없다

## 6. 리뷰 다음에 일어나는 일

Request changes가 오면 작성자가 고치고, Approve가 나오면 머지합니다.

1. 리뷰어가 리뷰를 제출하면 작성자에게 알림이 갑니다.
2. 수정 요청이면 작성자가 같은 브랜치에서 고쳐 다시 커밋하고 푸시합니다. 새 PR을 만들 필요 없이 같은 PR에 자동으로 붙습니다.
3. 코멘트를 반영했으면 작성자가 그 코멘트의 **Resolve conversation**을 눌러 닫습니다.
4. 리뷰어는 다시 확인하고 Approve를 누릅니다. 아직 문제가 있으면 2번으로 돌아갑니다.
5. Approve를 받으면 작성자가 **Merge pull request**로 develop에 합칩니다.

팀 약속:

- PR은 항상 develop으로 보내고, main에는 직접 올리지 않습니다.
- 다른 사람 PR의 Merge 버튼은 누르지 않습니다. 머지는 작성자가 합니다.
- 다른 사람의 PR이 머지되면 내 컴퓨터에서도 `git switch develop` 후 `git pull`로 최신 상태를 받고, 새 기능은 그 develop에서 `git switch -c feature/새기능` 으로 시작합니다.

참고: 갤러그 협동 모드 [PR #2](https://github.com/Seo1729/26-2--/pull/2)는 이미 머지됐지만, Files changed에서 코멘트는 지금도 남길 수 있습니다. 연습 삼아 3번과 4번을 해 보세요.
