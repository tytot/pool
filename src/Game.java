public class Game {
	private Ball[] balls = new Ball[16];
	
	private int p1Balls = 0;
	private int p1Type = 0;
	private int p2Balls = 0;
	private int p2Type = 0;
	
	public static final int LEGAL = 0;
	public static final int SCRATCH = 1;
	public static final int LOSE = 2;
	public static final int WIN = 3;
	
	private int turn = 1;
	
	public Game(Ball[] balls) {
		this.balls = balls;
	}
	
	public int getP1Balls() {
		return p1Balls;
	}
	public void addP1Ball() {
		p1Balls++;
	}
	public int getP1Type() {
		return p1Type;
	}
	public int getP2Balls() {
		return p2Balls;
	}
	public void addP2Ball() {
		p2Balls++;
	}
	public int getP2Type() {
		return p2Type;
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
	
	public void setBallTypes(Ball ball) {
		int ballType = ball.getType();
		if (turn == 1) {
			p1Type = ballType;
			p2Type = ballType == Ball.SOLID ? Ball.STRIPES : Ball.SOLID;
		} else {
			p2Type = ballType;
			p1Type = ballType == Ball.SOLID ? Ball.STRIPES : Ball.SOLID;
		}
		System.out.println("p1 type = " + p1Type);
		System.out.println("p2 type = " + p2Type);
	}
	
	public int pocket(Ball ball) {
		if (p1Balls == 0) {
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
