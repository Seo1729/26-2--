package vsai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 두 판이 함께 쓰는 블록 순서. 같은 순서로 블록이 나와서 공정하다.
 * 7종을 한 묶음으로 섞어 내보내는 방식(7-bag)이라 같은 블록만 계속 나오지 않는다.
 */
public class PieceSequence {

	private final Random random;
	private final List<Tetromino> pieces = new ArrayList<>();

	public PieceSequence(long seed) {
		random = new Random(seed);
	}

	/** index번째 블록 (0부터) */
	public synchronized Tetromino get(int index) {
		while (pieces.size() <= index) {
			List<Tetromino> bag = new ArrayList<>(List.of(Tetromino.values()));
			Collections.shuffle(bag, random);
			pieces.addAll(bag);
		}
		return pieces.get(index);
	}
}
