public class Game {

	private int[] ballIndices;
	private int[] ballCounts = { 0, 0 };
	private int[] ballTypes = { 0, 0 };

	private Ball toRespot = null;

	private int calledPocket = 0;

	private boolean pocketed = false;
	private boolean scratch = true;
	private boolean theBreak = true;

	private int turn = 1;
	private int winner = 0;

	public int[] getBallIndices() {
		return ballIndices;
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

	public void setBallTypes(Ball ball) {
	}

	public int getCalledPocket() {
		return calledPocket;
	}

	public void setCalledPocket(int pocket) {
		calledPocket = pocket;
	}

	public boolean toCall(int player) {
		return false;
	}

	public boolean pocketed() {
		return pocketed;
	}

	public void setPocketed(boolean pocketed) {
		this.pocketed = pocketed;
	}

	public boolean scratch() {
		return scratch;
	}

	public void setScratch(boolean scratch) {
		this.scratch = scratch;
	}

	public boolean isBreaking() {
		return theBreak;
	}

	public void endBreak() {
		theBreak = false;
	}

	public int getTurn() {
		return turn;
	}

	public void nextTurn() {
		turn++;
		if (turn > 2)
			turn = 1;
	}

	public int getWinner() {
		return winner;
	}

	public void setWinner(int winner) {
		this.winner = winner;
	}

	public boolean processPocket(Ball ball) {
		return true;
	}

	public int getPocketNumber(int x, int y) {
		int output = 1;
		if (y > 100)
			output = 2;
		if (y > 860)
			output = 3;
		if (x > 260)
			output += 3;
		return output;
	}

	public boolean validateFirstHit(Ball firstHit) {
		return true;
	}

	public Ball toRespot() {
		return toRespot;
	}

	public void setToRespot(Ball ball) {
		toRespot = ball;
	}

	public boolean playerWon(int player) {
		return false;
	}
}
