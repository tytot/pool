public class TenBall extends Game {

	private int[] ballIndices = { 0, 9, 10, 1, 3, 5, 6, 12, 11, 7, 15 };
	private int[] ballTypes = { 1, 2 };
	private boolean[] pocketed = { false, false, false, false, false, false, false, false, false, false };

	public int[] getBallIndices() {
		return ballIndices;
	}

	public boolean toCall(int player) {
		if (isBreaking() || getCalledPocket() != 0)
			return false;
		return true;
	}

	public int getBallType(int player) {
		return ballTypes[player - 1];
	}

	public boolean processPocket(Ball ball) {
		int ballType = ball.getType();
		if (ballType == Ball.SOLID) {
			int turn = getTurn();
			ball.setType(turn);
			addBall(turn);
			if (getPocketNumber((int) ball.X(), (int) ball.Y()) == getCalledPocket())
				setPocketed(true);

			pocketed[ball.getNumber() - 1] = true;

			if (ball.getNumber() == 10) {
				if (!scratch())
					setWinner(turn);
				else {
					setToRespot(ball);
					return false;
				}
			}
		} else {
			setScratch(true);
			return false;
		}
		return true;
	}

	public boolean validateFirstHit(Ball firstHit) {
		int legal = 1;
		while (pocketed[legal - 1] == true)
			legal++;

		if (firstHit.getNumber() == legal)
			return true;
		return false;
	}
}
