import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Ball {
	private double x, y;
	private final int type;
	public static final int SOLID = 1;
	public static final int STRIPES = 2;
	public static final int CUE = 3;
	public static final int EIGHT = 4;
	public static final double RADIUS = 12.0;
	private static final double FRICTION = 0.997;
	private static final double MAX_SPEED = 20.0;

	private Image img;
	private boolean inHole;
	private Vector2 velocity = new Vector2(0, 0);

	public Ball(int x, int y, int type, String imgPath) {
		this.x = x;
		this.y = y;
		this.type = type;
		this.inHole = false;
		try {
			img = ImageIO.read(new File(imgPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public double X() {
		return x;
	}

	public double Y() {
		return y;
	}

	public void setPos(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public int getType() {
		return type;
	}

	public Image getImg() {
		return img;
	}

	public boolean isInHole() {
		return inHole;
	}

	public void setInHole(boolean inHole) {
		if (inHole) {
			x = 0.0;
			y = 0.0;
		}
		this.inHole = inHole;
	}

	public Vector2 velocity() {
		return velocity;
	}

	public void setVel(Vector2 vel) {
		velocity = vel;
	}

	public void updatePos() {
		double i = velocity.I();
		double j = velocity.J();
		if (i > MAX_SPEED)
			i = MAX_SPEED;
		if (j > MAX_SPEED)
			j = MAX_SPEED;
		x += i;
		y += j;
		velocity.multiply(FRICTION);
	}
}
