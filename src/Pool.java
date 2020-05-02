import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Pool extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
	private Timer timer = new Timer(5, this);
	private int mouseX = 261;
	private int mouseY = 698;
	private int lastX = 261;
	private int lastY = 698;

	private int barPos = 23;
	private int clickedY = 0;
	private boolean movingBar = false;
	private int retractSpeed = 0;
	private double cueAngle = Math.toRadians(-90);
	private int[] cuePos = {261, 697};

	private boolean aiming = true;
	private boolean placing = false;
	private boolean pocketed = false;
	private boolean scratch = true;

	private BufferedImage table;
	private BufferedImage bar;
	private BufferedImage stick;

	private Game m;
	private CollisionDetector cd;

	private ArrayList<Ball> balls = new ArrayList<Ball>();
	private final Ball cueBall;
	private final int[][] startPos = { { 261, 262 }, { 237, 216 }, { 261, 170 }, { 285, 216 }, { 211, 170 }, { 273, 239 },
			{ 249, 193 }, { 261, 216 }, { 286, 170 }, { 225, 193 }, { 297, 193 }, { 273, 193 }, { 249, 239 },
			{ 311, 170 }, { 236, 170 }, { 261, 697 } };

	public Pool() {
		try {
			table = ImageIO.read(new File("images/pooltable.png"));
			bar = ImageIO.read(new File("images/cuebar.png"));
			stick = ImageIO.read(new File("images/cuestick.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int i = 1; i <= 16; i++) {
			String path = "images/balls/";
			if (i < 16)
				path += i + ".png";
			else
				path += "cue.png";

			int type;
			if (i < 8) {
				type = Ball.SOLID;
			} else if (i == 8) {
				type = Ball.EIGHT;
			} else if (i < 16) {
				type = Ball.STRIPES;
			} else
				type = Ball.CUE;

			balls.add(new Ball(startPos[i - 1][0], startPos[i - 1][1], type, path));
		}
		cueBall = balls.get(15);
		m = new EightBall(balls.toArray(new Ball[balls.size()]));
		cd = new CollisionDetector();
		//cueBall.setVel(new Vector2(0, -10));
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createTable();
			}
		});
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(table, 0, 0, null);

		if (placing) {
			cueBall.setPos(mouseX, mouseY);
			cuePos[0] = (int) mouseX;
			cuePos[1] = (int) mouseY;
		}
		for (Ball b : balls) {
			if (!b.isInHole())
				drawBall(b, g);
		}
		if (aiming)
			drawCue(g);
		else {
			if (cuePos[1] - 30 - retractSpeed <= 960 * 2)
				retractCue(g);
			update();
		}
		
		drawBar(g);

		timer.start();
		lastX = mouseX;
		lastY = mouseY;
	}

	private void update() {
		updatePositions();
		cd.manageCollisions(balls.toArray(new Ball[balls.size()]));
		for (int i = 0; i < balls.size(); i++) {
			Ball b = balls.get(i);
			if (b.isInHole()) {
				int val = m.pocket(b);
				if (val == Game.LEGAL) {
					pocketed = true;
				} else if (val == Game.SCRATCH) {
					scratch = true;
					pocketed = true;
				} else if (val == Game.LOSE)
					loss();
				else
					win();
				balls.remove(i);
			}
		}
		if (m.ballsAtRest()) {
			aiming = true;
			if (!pocketed || scratch) {
				m.nextTurn();
			}
			if (cueBall.isInHole()) {
				cueBall.setInHole(false);
				balls.add(cueBall);
				cueBall.setPos(261, 697);
			}
			for (int i = 0; i < balls.size(); i++) {
				Ball b = balls.get(i);
				if (b.isInHole())
					balls.remove(b);
			}
		}
	}

	private void updatePositions() {
		for (Ball ball : balls) {
			ball.updatePos();
		}
	}

	private void win() {
		System.out.println("Player " + m.getTurn() + " won!");
	}

	private void loss() {
		int player = m.getTurn() + 1;
		if (player == 3)
			player = 1;
		System.out.println("Player " + player + " won!");
	}

	private void drawBall(Ball ball, Graphics g) {
		g.drawImage(ball.getImg(), (int) (ball.X() - Ball.RADIUS), (int) (ball.Y() - Ball.RADIUS), 25, 25, null);
	}
	
	private void hit() {
		cuePos[0] = (int) cueBall.X();
		cuePos[1] = (int) cueBall.Y();
		double speed = Math.pow(retractSpeed / Ball.RADIUS, 2);
		if (speed > Ball.RADIUS)
			speed = Ball.RADIUS;
		double Vx = -speed * Math.cos(cueAngle);
		double Vy = speed * Math.sin(cueAngle);
		cueBall.setVel(new Vector2(Vx, Vy));
		pocketed = false;
		scratch = false;
	}

	private void drawBar(Graphics g) {
		if (movingBar) {
			barPos -= retractSpeed;
			g.drawImage(bar, 551, barPos, 62, 511, null);
			
			if (barPos < 23) {
				aiming = false;
				hit();
				if (barPos < (23 - 4 * retractSpeed)) {
					barPos = 23;
					movingBar = false;
				}
			}
		} else {
			g.drawImage(bar, 551, 23, 62, 511, null);
		}
	}

	private void drawCue(Graphics g) {
		if (!movingBar && !placing) {
			double diff = calculateAngle(mouseX, mouseY) - calculateAngle(lastX, lastY);
			cueAngle += diff;
		}
		Graphics2D g2d = (Graphics2D) g;
		drawLine(g2d);
		AffineTransform og = g2d.getTransform();
		AffineTransform trans = new AffineTransform();
		trans.rotate(-(cueAngle + Math.PI / 2), cueBall.X(), cueBall.Y());
		g2d.transform(trans);
		int offset = 0;
		if (movingBar)
			offset = (barPos - 23) / 4;
		g2d.drawImage(stick, (int) (cueBall.X() - 23), (int) (cueBall.Y() - 30 + offset), 45, 547, null);
		g2d.setTransform(og);
	}
	
	private void drawLine(Graphics2D g2d) {
		g2d.setStroke(new BasicStroke(2));
		g2d.setColor(Color.WHITE);
		double angle = Math.PI - cueAngle;
		double startX = cueBall.X() + ((Ball.RADIUS + 4) * Math.cos(angle));
		double startY = cueBall.Y() + ((Ball.RADIUS + 4) * Math.sin(angle));
		//getting equation of the line ax+by+c=0
		double a = -Math.tan(cueAngle);
		double b = 1.0;
		double c = Math.tan(cueAngle) * cueBall.X() - cueBall.Y();
		Ball willHit = null;
		double minDist = -1;
		for (Ball ball : balls) {
			double bDistX = Math.abs(ball.X() - cueBall.X());
			if (ball.getType() != Ball.CUE && Math.signum(cueAngle) != Math.signum(bDistX)) {
				//formula for distance between a point and a line squared
				double dist = Math.pow(a * ball.X() + b * ball.Y() + c, 2) / (a * a + b * b);
				if (dist < 4 * Ball.RADIUS * Ball.RADIUS) {
					if (minDist == -1 || dist < minDist) {
						minDist = dist;
					}
					if (willHit == null)
						willHit = ball;
					else {
						double dist1 = distSquared(ball.X(), ball.Y(), cueBall.X(), cueBall.Y());
						double dist2 = distSquared(willHit.X(), willHit.Y(), cueBall.X(), cueBall.Y());
						if (dist1 < dist2)
							willHit = ball;
					}
				}
			}
		}
		double length;
		if (willHit == null)
			length = 1;
		else {
			//determine where to put the white circle
			double z1 = distSquared(cueBall.X(), cueBall.Y(), willHit.X(), willHit.Y());
			double y = minDist;
			double x1 = Math.sqrt(z1 - y);
			
			double z2 = 4 * Ball.RADIUS * Ball.RADIUS;
			double x2 = Math.sqrt(z2 - y);
			
			length = x1 - x2;
		}
		int circleX = (int) (cueBall.X() + (length * Math.cos(angle)));
		int circleY = (int) (cueBall.Y() + (length * Math.sin(angle)));
		g2d.drawOval(circleX - 11, circleY - 11, 22, 22);
		g2d.drawLine((int) startX, (int) startY, circleX, (int) circleY); 
	}
	
	private void retractCue(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		AffineTransform og = g2d.getTransform();
		AffineTransform trans = new AffineTransform();
		trans.rotate(-(cueAngle + Math.PI / 2), cuePos[0], cuePos[1]);
		g2d.transform(trans);
		if (barPos != 23) {
			int offset = (barPos - 23) / 4;
			g2d.drawImage(stick, cuePos[0] - 23, cuePos[1] - 30 + offset, 45, 547, null);
		} else {
			g2d.drawImage(stick, cuePos[0] - 23, cuePos[1] - 30 - retractSpeed, 45, 547, null);
			retractSpeed -= 2;
		}
		g2d.setTransform(og);
	}
	
	private double calculateAngle(int x, int y) {
		double angle;
		if (x == cueBall.X()) {
			if (y > cueBall.Y())
				angle = Math.toRadians(270);
			else
				angle = Math.toRadians(90);
		} else {
			angle = Math.atan((cueBall.Y() - y) / (x - cueBall.X()));
			if (x < cueBall.X())
				angle += Math.PI;
		}
		return angle;
	}
	
	private double distSquared(double x1, double y1, double x2, double y2) {
		return (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
	}

	public void actionPerformed(ActionEvent e) {
		repaint();
	}

	@Override
	public Dimension getPreferredSize() {
		int width = table.getWidth();
		int height = table.getHeight();
		return new Dimension(width, height);
	}

	private static void createTable() {
		Pool pool = new Pool();
		pool.addMouseListener(pool);
		pool.addMouseMotionListener(pool);

		JFrame frame = new JFrame("Pool");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(pool);
		frame.pack();
		frame.setVisible(true);
	}

	public void mousePressed(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
		lastX = mouseX;
		lastY = mouseY;
		if (!placing && scratch) {
			double dX = mouseX - cueBall.X();
			double dY = mouseY - cueBall.Y();
			if ((dX * dX + dY * dY) <= Ball.RADIUS * Ball.RADIUS)
				placing = true;
		} 
		if (aiming && !movingBar) {
			if (mouseX >= 550 && mouseX <= 612 && mouseY >= barPos && mouseY <= barPos + 511) {
				clickedY = e.getY();
				movingBar = true;
				retractSpeed = 0;
			}
		}
	}

	public void mouseDragged(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
		if (movingBar) {
			int newPos = barPos + mouseY - clickedY;
			if (newPos >= 23 && newPos <= 425) {
				barPos += (mouseY - clickedY);
				clickedY = mouseY;
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (placing)
			placing = false;
		if (movingBar) {
			if (barPos - 23 > 10) {
				retractSpeed = (barPos - 23) / 8;
			} else {
				barPos = 23;
				movingBar = false;
			}
			clickedY = 0;
		}
		lastX = mouseX;
		lastY = mouseY;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}
}
