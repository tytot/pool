public class EightBall extends Game {
	
	public EightBall(Ball[] balls) {
		super(balls);
	}
	
	public int pocket(Ball ball) {
		int ballType = ball.getType();
		if (getBallType(1) == 0 && (ballType == Ball.SOLID || ballType == Ball.STRIPES)) {
			setBallTypes(ball);
		}
		if (ballType == getBallType(1)) {
			addBall(1);
			if (getTurn() == 1)
				return LEGAL;
			return -1;
		} else if (ballType == getBallType(2)) {
			addBall(2);
			if (getTurn() == 2)
				return LEGAL;
			return -1;
		} else if (ballType == Ball.CUE) {
			return SCRATCH;
		} else {
			int turn = getTurn();
			if (playerWon(turn))
				setWinner(turn);
			else {
				turn++;
				if (turn == 3)
					turn = 1;
				setWinner(turn);
			}
			return -1;
		}
	}
	
	public boolean playerWon(int player) {
		if (getBallCount(player) == 7)
			return true;
		return false;
	}
}
