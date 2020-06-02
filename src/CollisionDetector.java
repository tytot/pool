import java.util.ArrayList;

public class CollisionDetector {
	
	private Ball firstHit = null;
	
	public Ball getFirstHit() {
		return firstHit;
	}
	
	public void resetFirstHit() {
		firstHit = null;
	}
	
	public Vector2[] adjustVelocities(Ball b1, Ball b2) {
		Vector2 moveVec = b1.velocity().subtract(b2.velocity());
		double moveVecMag = Math.sqrt(moveVec.magSquared());
		double dist = Math.sqrt(distSquared(b1.X(), b1.Y(), b2.X(), b2.Y()));
		double diameter = Ball.RADIUS * 2;
		dist -= diameter;
		//escape test 1
		if (moveVecMag < dist)
			return null;
		
		Vector2 normalized = moveVec.normalize();
		Vector2 offset = new Vector2(b2.X() - b1.X(), b2.Y() - b1.Y());
		double dot = normalized.dot(offset);
		//escape test 2
		if (dot <= 0)
			return null;
		
		double offsetLength = offset.magSquared();
		double f = offsetLength - dot * dot;
		//escape test 3
		if (f >= diameter * diameter)
			return null;
		
		double leg = diameter * diameter - f;
		//escape test 4
		if (leg < 0)
			return null;
		
		double moveDist = dot - Math.sqrt(leg);
		//escape test 5
		if (moveVecMag < moveDist)
			return null;
		
		double scalar = moveDist / moveVecMag;
		Vector2 b1VelNew = b1.velocity().multiply(scalar);
		Vector2 b2VelNew = b2.velocity().multiply(scalar);
		if (!b1.hasRealVelocity())
			b1.setRealVel(b1.velocity());
		if (!b2.hasRealVelocity())
			b2.setRealVel(b2.velocity());
		b1.setVel(b1VelNew);
		b2.setVel(b2VelNew);
//		b1.setPos(b1.X() + b1VelNew.I(), b1.Y() + b1VelNew.J());
//		b2.setPos(b2.X() + b2VelNew.I(), b2.Y() + b2VelNew.J());
		//System.out.println(b1.getNumber()+" "+b1.X()+","+b1.Y()+" "+b2.getNumber()+" "+b2.X()+","+b2.Y());
		Vector2[] velocities = Collision.processCollision(b1, b2);
		return velocities;
	}
	
	public void manageCollisions(ArrayList<Ball> balls) {
		int numBalls = balls.size();
		ArrayList<Vector2>[] colLists = new ArrayList[numBalls];
		for (int i = 0; i < numBalls; i++) 
			colLists[i] = new ArrayList<Vector2>();
		for (int i = 0; i < numBalls; i++) {
			Ball b1 = balls.get(i);
			for (int j = i + 1; j < numBalls; j++) {
				Ball b2 = balls.get(j);
				Vector2[] newVels = adjustVelocities(b1, b2);
				if (newVels != null) {
					if (firstHit == null)
						firstHit = b1;
					colLists[i].add(newVels[0]);
					colLists[j].add(newVels[1]);
				}
			}
		}
		for (int i = 0; i < numBalls; i++) {
			Ball b = balls.get(i);
			int hit = borderCollision(b);
			if (hit > 0) {
				if (hit != Collision.POCKET)
					colLists[i].add(Collision.processCollision(b, hit));
				else
					b.setInHole(true);
			}
			if (colLists[i].size() > 0) {
				b.setVel(avgVelocity(colLists[i]));
			}
		}
	}
	
	private Vector2 avgVelocity(ArrayList<Vector2> vels) {
		int n = vels.size();
		Vector2 init = vels.get(0).clone();
		//System.out.print(init + " ");
		for (int i = 1; i < n; i++) {
			init.add(vels.get(i));
			//System.out.print(vels.get(i) + " ");
		}
		return init;
	}

	private int borderCollision(Ball b) {
		if (b.X() <= 57) {
			if ((b.Y() >= 70 && b.Y() <= 468) || (b.Y() >= 492 && b.Y() <= 890)) {
				if (b.X() - b.velocity().I() > 56.9)
					return Collision.W;
				b.setVel(new Vector2(b.velocity().I(), -b.velocity().J()));
			} else if (b.X() < 42)
				return Collision.POCKET;
		} else if (b.X() >= 464) {
			if ((b.Y() >= 70 && b.Y() <= 468) || (b.Y() >= 492 && b.Y() <= 890)) {
				if (b.X() - b.velocity().I() < 464.1)
					return Collision.E;
				b.setVel(new Vector2(b.velocity().I(), -b.velocity().J()));
			} else if (b.X() > 479)
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
