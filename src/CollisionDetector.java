import java.util.ArrayList;

public class CollisionDetector {
	
	public void manageCollisions(Ball[] balls) {
		int numBalls = balls.length;
		ArrayList<Vector2>[] colLists = new ArrayList[numBalls];
		for (int i = 0; i < numBalls; i++)
			colLists[i] = new ArrayList<Vector2>();
		for (int i = 0; i < numBalls; i++) {
			Ball b1 = balls[i];
			for (int j = i + 1; j < numBalls; j++) {
				Ball b2 = balls[j];
				if (areColliding(b1, b2)) {
					Vector2[] velocities = Collision.processCollision(b1, b2);
					colLists[i].add(velocities[0]);
					colLists[j].add(velocities[1]);
				}
			}
			int hit = borderCollision(b1);
			if (hit > 0) {
				if (hit != Collision.POCKET)
					colLists[i].add(Collision.processCollision(b1, hit));
				else
					b1.setInHole(true);
			}
		}
		for (int i = 0; i < numBalls; i++) {
			//System.out.print(i + " (size " + colLists[i].size() + "): ");
			Ball b = balls[i];
			if (colLists[i].size() > 0)
				b.setVel(avgVelocity(colLists[i]));
			//System.out.println();
		}
	}

	private boolean areColliding(Ball b1, Ball b2) {
		if (!b1.velocity().isZero() || !b2.velocity().isZero()) {
			if (distSquared(b1.X(), b1.Y(), b2.X(), b2.Y()) <= 24 * 24) {
				return true;
			}
		}
		return false;
	}
	
	private Vector2 avgVelocity(ArrayList<Vector2> vels) {
		int n = vels.size();
		Vector2 init = vels.get(0).clone();
		//System.out.print(init + " ");
		for (int i = 1; i < n; i++) {
			init.add(vels.get(i));
			//System.out.print(vels.get(i) + " ");
		}
		init.multiply(1.0 / n);
		return init;
	}

	private int borderCollision(Ball b) {
		if (b.X() <= 57) {
			if ((b.Y() >= 70 && b.Y() <= 468) || (b.Y() >= 492 && b.Y() <= 890))
				return Collision.W;
			return Collision.POCKET;
		} else if (b.X() >= 465) {
			if ((b.Y() >= 70 && b.Y() <= 468) || (b.Y() >= 492 && b.Y() <= 890))
				return Collision.E;
			return Collision.POCKET;
		} else if (b.Y() <= 59) {
			if (b.X() >= 70 && b.X() <= 455)
				return Collision.N;
			return Collision.POCKET;
		} else if (b.Y() >= 900) {
			if (b.X() >= 70 && b.X() <= 455)
				return Collision.S;
			return Collision.POCKET;
		}
		return 0;
	}

	private double distSquared(double x1, double y1, double x2, double y2) {
		double xDisp = Math.abs(x2 - x1);
		double yDisp = Math.abs(y2 - y1);
		return xDisp * xDisp + yDisp * yDisp;
	}
}
