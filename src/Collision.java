public class Collision {
	public static final int N = 1;
	public static final int E = 2;
	public static final int S = 3;
	public static final int W = 4;
	public static final int POCKET = 5;

	private static final double RESTITUTION = 0.96;

	public static Vector2[] processCollision(Ball b1, Ball b2) {
		Vector2 normal = new Vector2(b1.X() - b2.X(), b1.Y() - b2.Y()).normalize();
		Vector2 b1Vel, b2Vel;
		if (b1.hasRealVelocity())
			b1Vel = b1.realVelocity();
		else
			b1Vel = b1.velocity();
		if (b2.hasRealVelocity())
			b2Vel = b2.realVelocity();
		else
			b2Vel = b2.velocity();
		
		double a1 = b1Vel.dot(normal);
		double a2 = b2Vel.dot(normal);
		double P = a1 - a2;
		Vector2 change = normal.multiply(P);
		Vector2 v1f = b1Vel.subtract(change).multiply(RESTITUTION);
		Vector2 v2f = b2Vel.add(change).multiply(RESTITUTION);
		Vector2[] output = { v1f, v2f };
//		System.out.println("before: " + b1.velocity() + " " + b2.velocity());
//		System.out.println("after: " + v1f + " " + v2f);
		return output;
	}

	public static Vector2 processCollision(Ball b, int wallHit) {
		double bi = b.velocity().I();
		double bj = b.velocity().J();
		Vector2 output;
		if (wallHit == N) {
			b.setPos(b.X(), 60);
			output = new Vector2(bi, -bj);
		} else if (wallHit == S) {
			b.setPos(b.X(), 899);
			output = new Vector2(bi, -bj);
		} else if (wallHit == E) {
			b.setPos(464, b.Y());
			output = new Vector2(-bi, bj);
		} else {
			b.setPos(58, b.Y());
			output = new Vector2(-bi, bj);
		}
		output.multiply(RESTITUTION);
		return output;
	}
}
