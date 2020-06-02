
public class Vector2 {
	private double i = 0;
	private double j = 0;

	public Vector2(double i, double j) {
		this.i = i;
		this.j = j;
	}

	public double I() {
		return i;
	}

	public double J() {
		return j;
	}

	public boolean isZero() {
		if (i == 0.0 && j == 0.0)
			return true;
		return false;
	}

	public double magSquared() {
		return i * i + j * j;
	}

	public Vector2 multiply(double scalar) {
		return new Vector2(i * scalar, j * scalar);
	}

	public double dot(Vector2 other) {
		double out = i * other.I() + j * other.J();
		return out;
	}

	public Vector2 add(Vector2 other) {
		return new Vector2(i + other.I(), j + other.J());
	}

	public Vector2 subtract(Vector2 other) {
		return new Vector2(i - other.I(), j - other.J());
	}
	
	public Vector2 normalize() {
		if (magSquared() == 0.0)
			return new Vector2(0.0, 0.0);
		
		double newI = i / Math.sqrt(magSquared());
		double newJ = j / Math.sqrt(magSquared());
		return new Vector2(newI, newJ);
	}

	public Vector2 clone() {
		return new Vector2(i, j);
	}

	public String toString() {
		return "<" + i + ", " + j + ">";
	}
}
