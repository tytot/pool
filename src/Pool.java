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
	//cuePos stores the point at which the cue ball was hit for proper cue stick retraction.
	private int[] cuePos = { 261, 697 };

	private boolean aiming = true;
	private boolean placing = false;
	private boolean pocketed = false;
	private boolean scratch = true;
	private boolean theBreak = true;

	private BufferedImage table;
	private BufferedImage bar;
	private BufferedImage stick;
	private BufferedImage arrows;

	private Game m;
	private CollisionDetector cd;

	private ArrayList<Ball> balls = new ArrayList<Ball>();
	private ArrayList<Ball> sbBalls = new ArrayList<Ball>();
	private final Ball cueBall;
	private final int[][] startPos = { { 261, 262 }, { 237, 216 }, { 261, 170 }, { 285, 216 }, { 211, 170 },
			{ 273, 239 }, { 249, 193 }, { 261, 216 }, { 286, 170 }, { 225, 193 }, { 297, 193 }, { 273, 193 },
			{ 249, 239 }, { 311, 170 }, { 236, 170 }, { 261, 697 } };
	private final int[][] scoreboardPos = { { 677, 300 }, { 730, 300 }, { 677, 360 }, { 730, 360 }, { 677, 420 },
			{ 730, 420 }, { 703, 480 }, { 703, 540 } };

	public Pool() {
		try {
			table = ImageIO.read(new File("images/pooltable.png"));
			bar = ImageIO.read(new File("images/cuebar.png"));
			stick = ImageIO.read(new File("images/cuestick.png"));
			arrows = ImageIO.read(new File("images/cuemove.png"));
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

			balls.add(new Ball(startPos[i - 1][0], startPos[i - 1][1], type, i, path));
		}
		cueBall = balls.get(15);
		m = new EightBall(balls.toArray(new Ball[balls.size()]));
		cd = new CollisionDetector();
		// cueBall.setVel(new Vector2(0, -10));
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
		drawMessage(g);

		if (placing) {
			cuePos[0] = (int) mouseX;
			cuePos[1] = (int) mouseY;
			if (cuePos[0] <= 57)
				cuePos[0] = 57;
			else if (cuePos[0] >= 464)
				cuePos[0] = 464;
			if (cuePos[1] <= 59)
				cuePos[1] = 59;
			else if (cuePos[1] >= 900)
				cuePos[1] = 900;
			if (theBreak && cuePos[1] <= 697)
				cuePos[1] = 697;
			cueBall.setPos(cuePos[0], cuePos[1]);
		}
		for (Ball b : balls) {
			if (!b.isInHole())
				drawBall(b, g);
		}
		drawScoreboardBalls(g);
		if (aiming) {
			if (scratch)
				drawArrows(g);
			drawCue(g);
		} else {
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
		updateBalls();
		cd.manageCollisions(balls);
		for (int i = 0; i < balls.size(); i++) {
			Ball b = balls.get(i);
			if (b.isInHole()) {
				int val = m.pocket(b);
				if (val != -1) {
					if (val == Game.LEGAL) {
						pocketed = true;
					} else if (val == Game.SCRATCH) {
						scratch = true;
					}
				}
				if (b.getType() != Ball.CUE) {
					balls.remove(i);
					sbBalls.add(b);
				}
			}
		}
		if (m.ballsAtRest()) {
			if (m.getWinner() == 0) {
				Ball firstHit = cd.getFirstHit();
				int ballType = m.getBallType(m.getTurn());
				if (ballType == 0) {
					if (firstHit.getType() == Ball.EIGHT)
						scratch = true;
				} else {
					if (firstHit == null || firstHit.getType() != ballType)
						scratch = true;
				}
				cd.resetFirstHit();
				
				aiming = true;
				if (!pocketed) {
					m.nextTurn();
					if (scratch) {
						cuePos[0] = (int) cueBall.X();
						cuePos[1] = (int) cueBall.Y();
					}
				}
				if (cueBall.isInHole()) {
					cueBall.setInHole(false);
					cueBall.setPos(261, 697);
				}
				cuePos[0] = (int) cueBall.X();
				cuePos[1] = (int) cueBall.Y();
			} else {
				timer.stop();
			}
		}
	}

	private void updateBalls() {
		for (Ball b : balls) {
			b.resetRealVelocity();
			b.updatePos();
		}
	}
	
	private void drawMessage(Graphics g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font("Roboto", Font.PLAIN, 56));
		if (m.getWinner() == 0)
			g.drawString("P" + m.getTurn() + "'s Turn", 670, 675);
		else
			g.drawString("P" + m.getWinner() + " WON!!!", 670, 675);
	}

	private void drawBall(Ball ball, Graphics g) {
		g.drawImage(ball.getImg(), (int) (ball.X() - Ball.RADIUS), (int) (ball.Y() - Ball.RADIUS), 25, 25, null);
	}

	private void drawScoreboardBalls(Graphics g) {
		int p1Count = 0, p2Count = 0;
		int size = 40;
		for (Ball ball : sbBalls) {
			if (ball.getType() == m.getBallType(1)) {
				g.drawImage(ball.getImg(), scoreboardPos[p1Count][0], scoreboardPos[p1Count][1], size, size, null);
				p1Count++;
			} else if (ball.getType() == m.getBallType(2)) {
				g.drawImage(ball.getImg(), scoreboardPos[p2Count][0] + 123, scoreboardPos[p2Count][1], size, size, null);
				p2Count++;
			} else {
				int x = scoreboardPos[7][0];
				int y = scoreboardPos[7][1];
				if (m.getTurn() == 2)
					x += 123;
				g.drawImage(ball.getImg(), x, y, size, size, null);
			}
		}
	}

	private void hit() {
		if (theBreak)
			theBreak = false;

		double speed = retractSpeed / 3.0;
		if (speed > 15.0)
			speed = 15.0;
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
	
	private void drawArrows(Graphics g) {
		g.drawImage(arrows, cuePos[0] - 25, cuePos[1] - 25, 50, 50, null);
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
		angle -= Math.floor((angle + Math.PI) / (2 * Math.PI)) * 2 * Math.PI;
		double dx = Math.cos(angle);
		double dy = Math.sin(angle);
		int startX = (int) (cueBall.X() + (Ball.RADIUS * dx));
		int startY = (int) (cueBall.Y() + ((Ball.RADIUS - 1) * dy));
		
		// distance between point and ray: P-(B+tM)
		Vector2 B = new Vector2(startX, startY);
		Vector2 M = new Vector2(dx, dy);
		Ball willHit = null;
		double minDist = -1;

		for (Ball ball : balls) {
			if (ball.getType() != Ball.CUE) {
				Vector2 P = new Vector2(ball.X(), ball.Y());
				// formula for distance between a point and a line squared
				double t = M.dot(P.subtract(B)) / M.dot(M);
				if (t >= 0) {
					double dist = P.subtract(B.add(M.multiply(t))).magSquared();
					if (dist <= 4 * Ball.RADIUS * Ball.RADIUS) {
						if (willHit == null) {
							willHit = ball;
							minDist = dist;
						} else {
							double dist1 = distSquared(ball.X(), ball.Y(), cueBall.X(), cueBall.Y());
							double dist2 = distSquared(willHit.X(), willHit.Y(), cueBall.X(), cueBall.Y());
							if (dist1 < dist2) {
								willHit = ball;
								minDist = dist;
							}
						}
					}
				}
			}
		}
		if (willHit == null) {
			drawWallGuidelines(g2d, angle, startX, startY);
		} else {
			drawBallGuidelines(g2d, angle, startX, startY, minDist, willHit);
		}
	}
	
	private void drawWallGuidelines(Graphics2D g2d, double angle, int startX, int startY) {
		double length;
		double y;
		if (angle <= 0.0 && angle >= -Math.PI)
			y = cueBall.Y() - 59;
		else
			y = cueBall.Y() - 900;
		double x = -y / Math.tan(angle);
		if (cueBall.X() + x > 465) {
			x = 465 - cueBall.X();
			length = x / Math.cos(angle);
		} else if (cueBall.X() + x < 57) {
			x = 57 - cueBall.X();
			length = x / Math.cos(angle);
		} else {
			length = -y / Math.sin(angle);
		}
		int circleX = (int) (cueBall.X() + (length * Math.cos(angle)));
		int circleY = (int) (cueBall.Y() + (length * Math.sin(angle)));
		g2d.drawOval(circleX - Ball.RADIUS, circleY - Ball.RADIUS, 2 * Ball.RADIUS, 2 * Ball.RADIUS);
		if (distSquared(cueBall.X(), cueBall.Y(), circleX, circleY) > Ball.RADIUS * Ball.RADIUS) {
			g2d.drawLine(startX, startY, circleX - (int) ((Ball.RADIUS + 1) * Math.cos(angle)),
				circleY - (int) ((Ball.RADIUS + 1) * Math.sin(angle)));
		}
	}
	
	private void drawBallGuidelines(Graphics2D g2d, double angle, int startX, int startY, double minDist, Ball willHit) {
		// determine where to put the white circle
		double z1 = distSquared(cueBall.X(), cueBall.Y(), willHit.X(), willHit.Y());
		double y = minDist;
		double x1 = Math.sqrt(z1 - y);
		double z2 = 4 * Ball.RADIUS * Ball.RADIUS;
		double x2 = Math.sqrt(z2 - y);

		double length = x1 - x2;
		int circleX = (int) (cueBall.X() + (length * Math.cos(angle)));
		int circleY = (int) (cueBall.Y() + (length * Math.sin(angle)));
		
		Vector2 direction = new Vector2(willHit.X() - circleX, willHit.Y() - circleY);
		double whAngle = Math.atan2(direction.J(), direction.I());
		double dAngle = angle - whAngle;
		if (dAngle <= -Math.PI / 2)
			dAngle += (2 * Math.PI);
		else if (dAngle >= Math.PI / 2)
			dAngle -= (2 * Math.PI);
		//System.out.println(angle + " - " + whAngle + " = " + dAngle);
		double whLength = 120 * ((Math.PI / 2 - Math.abs(dAngle)) / (Math.PI / 2));
		double dLength = 120 - whLength;
		
		dLength += (Ball.RADIUS + 1);
		g2d.drawLine(circleX + (int) ((Ball.RADIUS + 1) * Math.cos(whAngle)),
				circleY + (int) ((Ball.RADIUS + 1) * Math.sin(whAngle)),
				circleX + (int) (whLength * Math.cos(whAngle)), circleY + (int) (whLength * Math.sin(whAngle)));
		double cAngle = whAngle + Math.signum(dAngle) * Math.PI / 2;
		g2d.drawLine(circleX + (int) ((Ball.RADIUS + 1) * Math.cos(cAngle)),
				circleY + (int) ((Ball.RADIUS + 1) * Math.sin(cAngle)),
				circleX + (int) (dLength * Math.cos(cAngle)), circleY + (int) (dLength * Math.sin(cAngle)));
		g2d.drawOval(circleX - Ball.RADIUS, circleY - Ball.RADIUS, 2 * Ball.RADIUS, 2 * Ball.RADIUS);
		if (distSquared(cueBall.X(), cueBall.Y(), circleX, circleY) > Ball.RADIUS * Ball.RADIUS) {
			g2d.drawLine(startX, startY, circleX - (int) ((Ball.RADIUS + 1) * Math.cos(angle)),
				circleY - (int) ((Ball.RADIUS + 1) * Math.sin(angle)));
		}
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
