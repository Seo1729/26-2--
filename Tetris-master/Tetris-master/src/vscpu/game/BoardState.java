package vscpu.game;

/**
 * 한쪽 판을 "보기만" 할 때 쓰는 읽기 전용 창구.
 * 화면(BoardView)은 이것만 받아서 그리므로 판을 실수로 바꿀 수 없다.
 */
public interface BoardState {

	String getName();

	/** (x, y) 칸에 쌓인 블록. 비어 있으면 null */
	Tetromino cellAt(int x, int y);

	Tetromino getCurrent();

	Tetromino getNext();

	int getRotation();

	int getX();

	int getY();

	/** 지금 블록을 떨어뜨리면 멈출 y (그림자 표시용) */
	int ghostY();

	int getScore();

	int getLines();

	int level();

	boolean isGameOver();
}
