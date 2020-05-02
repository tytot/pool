
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

	public void multiply(double scalar) {
		i *= scalar;
		j *= scalar;
	}

	public double dot(Vector2 other) {
		double out = i * other.I() + j * other.J();
		return out;
	}

	public void add(Vector2 other) {
		i += other.I();
		j += other.J();
	}

	public void subtract(Vector2 other) {
		i -= other.I();
		j -= other.J();
	}

	public Vector2 clone() {
		return new Vector2(i, j);
	}

	public String toString() {
		return "<" + i + ", " + j + ">";
	}
}
