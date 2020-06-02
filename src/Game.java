public class Game {
	private Ball[] balls = new Ball[16];
	
	private int[] ballCounts = {0, 0};
	private int[] ballTypes = {0, 0};
	
	public static final int LEGAL = 0;
	public static final int SCRATCH = 1;
	
	private int turn = 1;
	private int winner = 0;
	
	public Game(Ball[] balls) {
		this.balls = balls;
	}
	
	public int getBallCount(int player) {
		return ballCounts[player - 1];
	}
	public void addBall(int player) {
		ballCounts[player - 1]++;
	}
	public int getBallType(int player) {
		return ballTypes[player - 1];
	}
	
	public int getTurn() {
		return turn;
	}
	
	public void nextTurn() {
		turn++;
		if (turn > 2)
			turn = 1;
		System.out.println("Player " + turn + "'s turn");
	}
	
	public int getWinner() {
		return winner;
	}
	
	public void setWinner(int winner) {
		this.winner = winner;
	}
	
	public void setBallTypes(Ball ball) {
		int ballType = ball.getType();
		if (ballTypes[0] == 0) {
			int other = turn + 1;
			if (other == 3)
				other = 1;
			ballTypes[turn - 1] = ballType;
			ballTypes[other - 1] = ballType == Ball.SOLID ? Ball.STRIPES : Ball.SOLID;
		}
		System.out.println("p1 type = " + ballTypes[0]);
		System.out.println("p2 type = " + ballTypes[1]);
	}
	
	public int pocket(Ball ball) {
		if (ballCounts[0] == 0) {
			setBallTypes(ball);
		}
		return LEGAL;
	}
	
	public boolean playerWon(int player) {
		return false;
	}
	
	public boolean ballsAtRest() {
		for (Ball b : balls) {
			if (!b.isInHole() && (Math.abs(b.velocity().I()) >= 0.05 || Math.abs(b.velocity().J()) >= 0.05))
				return false;
		}
		for (Ball b : balls) {
			b.setVel(new Vector2(0, 0));
		}
		return true;
	}
}
