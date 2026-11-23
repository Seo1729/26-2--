package threed;

// 다음에 나올 블록 종류를 정해 주는 쪽 (게임은 "어떻게 고르는지" 모르고 이 인터페이스만 쓴다)
// 약속: next()는 null이 아니고, 3x3 보드에 들어가는 종류만 돌려준다. (길이 4인 I는 안 됨)
// 이 약속만 지키면 무작위, 7개 묶음, 테스트용 고정 순서 등 어떤 방식으로 바꿔 끼워도 게임이 그대로 동작한다.
public interface BlockTypeSource
{
	Block3D.Type next();
}
