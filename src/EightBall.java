public class EightBall extends Game {

	private int[] ballIndices = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
	private int[] ballTypes = { 0, 0 };

	public int[] getBallIndices() {
		return ballIndices;
	}

	public boolean toCall(int player) {
		if (getBallCount(player) == 7 && getCalledPocket() == 0)
			return true;
		return false;
	}

	public int getBallType(int player) {
		return ballTypes[player - 1];
	}

	public void setBallTypes(Ball ball) {
		int ballType = ball.getType();
		if (ballTypes[0] == 0) {
			int other = getTurn() + 1;
			if (other == 3)
				other = 1;
			ballTypes[getTurn() - 1] = ballType;
			ballTypes[other - 1] = ballType == Ball.SOLID ? Ball.STRIPES : Ball.SOLID;
		}
	}

	public boolean processPocket(Ball ball) {
		int ballType = ball.getType();
		if (getBallType(1) == 0 && (ballType == Ball.SOLID || ballType == Ball.STRIPES)) {
			setBallTypes(ball);
		}
		if (ballType == getBallType(1)) {
			addBall(1);
			if (getTurn() == 1) {
				setPocketed(true);
			}
			return true;
		} else if (ballType == getBallType(2)) {
			addBall(2);
			if (getTurn() == 2) {
				setPocketed(true);
			}
			return true;
		} else if (ballType == Ball.CUE) {
			setScratch(true);
			return false;
		} else {
			int turn = getTurn();
			addBall(turn);
			boolean valid = false;
			if (getPocketNumber((int) ball.X(), (int) ball.Y()) == getCalledPocket())
				valid = true;
			if (getBallCount(turn) == 8 && valid)
				setWinner(turn);
			else {
				turn++;
				if (turn == 3)
					turn = 1;
				setWinner(turn);
			}
			return true;
		}
	}

	public boolean validateFirstHit(Ball firstHit) {
		int ballType = getBallType(getTurn());
		int firstHitType = firstHit.getType();
		if (ballType == 0) {
			if (firstHitType == Ball.EIGHT)
				return false;
		} else {
			if (firstHitType == Ball.EIGHT && getBallCount(getTurn()) == 7)
				return true;
			if (firstHitType != ballType)
				return false;
		}
		return true;
	}
}