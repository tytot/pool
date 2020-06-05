public class NineBall extends Game {

	private int[] ballIndices = { 0, 6, 5, 1, 3, 12, 11, 2, 7, 15 };
	private int[] ballTypes = { 1, 2 };
	private boolean[] pocketed = { false, false, false, false, false, false, false, false, false };

	public int[] getBallIndices() {
		return ballIndices;
	}

	public boolean toCall(int player) {
		return false;
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
			setPocketed(true);

			pocketed[ball.getNumber() - 1] = true;

			if (ball.getNumber() == 9) {
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
