public class EightBall extends Game {
	
	public EightBall(Ball[] balls) {
		super(balls);
	}
	
	public int pocket(Ball ball) {
		if (getP1Balls() == 0) {
			setBallTypes(ball);
		}
		if (ball.getType() == getP1Type()) {
			addP1Ball();
			return LEGAL;
		} else if (ball.getType() == getP2Type()) {
			addP2Ball();
			return LEGAL;
		} else if (ball.getType() == Ball.CUE) {
			return SCRATCH;
		} else {
			if (playerWon(getTurn()))
				return WIN;
			return LOSE;
		}
	}
	
	public boolean playerWon(int player) {
		if (player == 1 && getP1Balls() == 7)
			return true;
		if (player == 2 && getP2Balls() == 7)
			return true;
		return false;
	}
}
