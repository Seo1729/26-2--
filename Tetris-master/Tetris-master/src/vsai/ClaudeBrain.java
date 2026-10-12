package vsai;

/**
 * Claude API에게 둘 자리를 물어보는 AI (아직 뼈대).
 *
 * TODO 다음 단계에서 만들 것:
 * - 판 상태, 지금 블록, 다음 블록을 글로 정리해 API에 보내고
 *   구조화된 출력(JSON)으로 {"rotation": 0~3, "column": 0~9}를 받는다.
 * - API 키는 환경 변수 ANTHROPIC_API_KEY에서만 읽는다. 코드나 저장소에 절대 넣지 않는다.
 * - 키가 없거나, 느리거나, 실패하면 AiController가 HeuristicBrain으로 대신 둔다.
 *
 * 지금은 네트워크를 쓰지 않고 HeuristicBrain에 그대로 맡긴다.
 */
public class ClaudeBrain implements AiBrain {

	private final AiBrain standIn = new HeuristicBrain();

	@Override
	public Move decide(Board board, Tetromino current, Tetromino next) {
		return standIn.decide(board, current, next);
	}
}
