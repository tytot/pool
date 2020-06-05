import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Ball {
	private double x, y;
	private int type;
	private final int number;
	public static final int SOLID = 1;
	public static final int STRIPES = 2;
	public static final int CUE = 3;
	public static final int EIGHT = 4;
	public static final int RADIUS = 12;
	public static final int DIAMETER = RADIUS * 2;
	private static final double FRICTION = 0.995;

	private Image img;
	private boolean inHole;
	private Vector2 velocity = new Vector2(0, 0);
	private Vector2 realVelocity;

	public Ball(int x, int y, int type, int number, String imgPath) {
		this.x = x;
		this.y = y;
		this.type = type;
		this.number = number;
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
	
	public void setType(int type) {
		this.type = type;
	}
	
	public int getNumber() {
		return number;
	}

	public Image getImg() {
		return img;
	}

	public boolean isInHole() {
		return inHole;
	}

	public void setInHole(boolean inHole) {
		this.inHole = inHole;
	}

	public Vector2 velocity() {
		return velocity;
	}

	public void setVel(Vector2 vel) {
		velocity = vel;
	}
	
	public Vector2 realVelocity() {
		return realVelocity;
	}
	
	public boolean hasRealVelocity() {
		return realVelocity != null;
	}
	
	public void resetRealVelocity() {
		realVelocity = null;
	}
	
	public void setRealVel(Vector2 realVel) {
		realVelocity = realVel;
	}

	public void updatePos() {
		double i = velocity.I();
		double j = velocity.J();
		x += i;
		y += j;
		velocity = velocity.multiply(FRICTION);
	}
}
