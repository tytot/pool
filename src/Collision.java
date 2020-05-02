public class Collision {
	public static final int N = 1;
	public static final int E = 2;
	public static final int S = 3;
	public static final int W = 4;
	public static final int POCKET = 5;

	private static final double RESTITUTION = 0.94;
	private static final double SHIFT = 0.02;

	public static Vector2[] processCollision(Ball b1, Ball b2) {
		double b1i = b1.velocity().I();
		double b1j = b1.velocity().J();
		double b2i = b2.velocity().I();
		double b2j = b2.velocity().J();
		Vector2 normal = new Vector2(b1.X() - b2.X(), b1.Y() - b2.Y());
		while (normal.magSquared() <= 576.0) {
			b1.setPos(b1.X() - SHIFT * b1i, b1.Y() - SHIFT * b1j);
			b2.setPos(b2.X() - SHIFT * b2i, b2.Y() - SHIFT * b2j);
			normal = new Vector2(b1.X() - b2.X(), b1.Y() - b2.Y());
		}
		//System.out.println("normal: " + normal);
		Vector2 dv = new Vector2(b1i - b2i, b1j - b2j);
		double vn = normal.dot(dv) / normal.magSquared();

		Vector2 n = normal.clone();
		n.multiply(vn);

		Vector2[] output = new Vector2[2];
		Vector2 b1Vel = b1.velocity().clone();
		b1Vel.subtract(n);
		b1Vel.multiply(RESTITUTION);
		output[0] = b1Vel;
		Vector2 b2Vel = b2.velocity().clone();
		b2Vel.add(n);
		b2Vel.multiply(RESTITUTION);
		output[1] = b2Vel;
		// System.out.println("ball " + i + ": " + b2.velocity());
		// System.out.println("cue: " + b.velocity() + "\n-----------------");
		
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
