import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;

public class Pool extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
	private Timer timer = new Timer(5, this);
	private int mouseX = 261, mouseY = 698;
	private int lastX = 261, lastY = 698;

	private int barPos = 23;
	private int clickedY = 0;
	private boolean movingBar = false;
	private int retractSpeed = 0;
	private double cueAngle = Math.toRadians(-90);
	// cuePos stores the point at which the cue ball was hit for proper cue stick
	// retraction.
	private int[] cuePos = { 261, 697 };

	private boolean aiming = true, placing = false, validated = false;

	private BufferedImage table, bar, stick, arrows, calls;
	private Clip cueSFX;
	private boolean[] highlightedPockets = { false, false, false, false, false, false };

	private Dimension size;

	private Game m;
	private CollisionDetector cd;

	private ArrayList<Ball> balls = new ArrayList<Ball>();
	private ArrayList<Ball> sbBalls = new ArrayList<Ball>();
	private final Ball cueBall;
	private final int[][] startPos = { { 261, 262 }, { 237, 216 }, { 261, 170 }, { 285, 216 }, { 211, 170 },
			{ 273, 239 }, { 249, 193 }, { 261, 216 }, { 286, 170 }, { 225, 193 }, { 297, 193 }, { 273, 193 },
			{ 249, 239 }, { 311, 170 }, { 236, 170 }, { 261, 697 } };
	private final int[][] scoreboardPos = { { 677, 300 }, { 730, 300 }, { 677, 360 }, { 730, 360 }, { 677, 420 },
			{ 730, 420 }, { 677, 480 }, { 730, 480 }, { 703, 540 } };

	public Pool(Game game) {
		m = game;
		cd = new CollisionDetector();

		try {
			table = ImageIO.read(getClass().getResource("images/pooltable.png"));
			bar = ImageIO.read(getClass().getResource("images/cuebar.png"));
			stick = ImageIO.read(getClass().getResource("images/cuestick.png"));
			arrows = ImageIO.read(getClass().getResource("images/cuemove.png"));
			calls = ImageIO.read(getClass().getResource("images/calls.png"));

			size = new Dimension(table.getWidth(), table.getHeight());

			AudioInputStream audioIn = AudioSystem.getAudioInputStream(getClass().getResource("sfx/cue.wav"));
			cueSFX = AudioSystem.getClip();
			cueSFX.open(audioIn);
		} catch (Exception e) {
			e.printStackTrace();
		}

		int[] ballIndices = m.getBallIndices();
		int length = ballIndices.length;
		for (int i = 0; i < length; i++) {
			int ball = i + 1;
			String path = "images/balls/";
			if (ball < length)
				path += ball + ".png";
			else
				path += "cue.png";

			int type;
			if (ball == length) {
				type = Ball.CUE;
			} else if (length == 16) {
				if (ball < 8) {
					type = Ball.SOLID;
				} else if (ball == 8) {
					type = Ball.EIGHT;
				} else {
					type = Ball.STRIPES;
				}
			} else {
				type = Ball.SOLID;
			}

			balls.add(new Ball(startPos[ballIndices[i]][0], startPos[ballIndices[i]][1], type, ball, path));
		}
		cueBall = balls.get(length - 1);
	}

	public static void createTable(int gamemode) {
		Game game;
		if (gamemode == 1)
			game = new EightBall();
		else if (gamemode == 2)
			game = new NineBall();
		else
			game = new TenBall();
		Pool pool = new Pool(game);
		pool.addMouseListener(pool);
		pool.addMouseMotionListener(pool);
		Dimension size = pool.getPreferredSize();

		JFrame frame = new JFrame("Pool");
		frame.setResizable(false);
		frame.setPreferredSize(size);
		frame.setMinimumSize(size);
		frame.setMaximumSize(size);
		frame.add(pool);
		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public Dimension getPreferredSize() {
		return size;
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
			if (m.isBreaking() && cuePos[1] <= 697)
				cuePos[1] = 697;

			cueBall.setPos(cuePos[0], cuePos[1]);
		}
		for (Ball b : balls) {
			if (!b.isInHole())
				drawBall(b, g);
		}

		drawScoreboardBalls(g);
		if (aiming) {
			if (m.toCall(m.getTurn()))
				g.drawImage(calls, 0, 0, null);
			else if (m.getCalledPocket() != 0) {
				highlightPocket(m.getCalledPocket(), g);
			}
			if (m.scratch())
				drawArrows(g);
			drawCue(g);
		} else {
			if (cuePos[1] - 30 - retractSpeed <= 960 * 2)
				retractCue(g);
			for (int i = 0; i < 6; i++) {
				if (highlightedPockets[i])
					highlightPocket(i + 1, g);
			}
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
		Ball firstHit = cd.getFirstHit();
		if (!validated && firstHit != null) {
			if (!m.validateFirstHit(firstHit)) {
				m.setScratch(true);
			}
			validated = true;
		}
		for (int i = 0; i < balls.size(); i++) {
			Ball b = balls.get(i);
			if (b.isInHole()) {
				boolean legal = m.processPocket(b);
				int pocketNum = m.getPocketNumber((int) b.X(), (int) b.Y());
				highlightedPockets[pocketNum - 1] = true;
				if (legal) {
					balls.remove(i);
					sbBalls.add(b);
				}
			}
		}
		if (ballsAtRest()) {
			if (m.getWinner() == 0) {
				cd.resetFirstHit();
				if (!validated)
					m.setScratch(true);
				else
					validated = false;
				m.setCalledPocket(0);

				aiming = true;
				if (m.scratch())
					m.setPocketed(false);

				if (!m.pocketed()) {
					m.nextTurn();
					if (m.scratch()) {
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

				Ball toRespot = m.toRespot();
				if (toRespot != null) {
					System.out.println("respot");
					respot(toRespot);
					m.setToRespot(null);
				}
				resetHighlightedPockets();
			} else {
				timer.stop();
			}
		}
	}

	private void respot(Ball toRespot) {
		toRespot.setInHole(false);
		toRespot.setPos(startPos[0][0], startPos[0][1]);
		for (Ball b : balls) {
			if (b != toRespot) {
				if (distSquared(b.X(), b.Y(), toRespot.X(), toRespot.Y()) < Ball.DIAMETER * Ball.DIAMETER) {
					double relAngle = Math.atan2(toRespot.Y() - b.Y(), toRespot.X() - b.X());
					double newX = b.X() + ((Ball.DIAMETER + 1) * Math.cos(relAngle));
					double newY = b.Y() + ((Ball.DIAMETER + 1) * Math.sin(relAngle));
					toRespot.setPos(newX, newY);
				}
			}
		}
	}

	private void updateBalls() {
		for (Ball b : balls) {
			b.resetRealVelocity();
			b.updatePos();
		}
	}

	public boolean ballsAtRest() {
		for (Ball b : balls) {
			if (!b.isInHole() && (Math.abs(b.velocity().I()) >= 0.05 || Math.abs(b.velocity().J()) >= 0.05))
				return false;
		}
		for (Ball b : balls) {
			b.setVel(new Vector2(0, 0));
		}
		return true;
	}

	private void hit() {
		playSound(cueSFX);
		double speed;
		if (m.isBreaking()) {
			speed = retractSpeed / 3.0;
			if (speed > 15.0)
				speed = 15.0;
			m.endBreak();
		} else {
			speed = retractSpeed / 4.0;
		}
		double Vx = -speed * Math.cos(cueAngle);
		double Vy = speed * Math.sin(cueAngle);
		cueBall.setVel(new Vector2(Vx, Vy));
		m.setPocketed(false);
		m.setScratch(false);
	}

	public static void playSound(Clip clip) {
		try {
			clip.start();
			clip.addLineListener(new LineListener() {
				public void update(LineEvent event) {
					if (event.getType() == LineEvent.Type.STOP) {
						clip.stop();
						clip.setFramePosition(0);
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void drawMessage(Graphics g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font("Roboto", Font.PLAIN, 56));
		int turn = m.getTurn();
		if (m.getWinner() == 0) {
			g.drawString("P" + turn + "'s Turn", 670, 675);
		} else
			g.drawString("P" + m.getWinner() + " WON!!!", 660, 675);

		g.setFont(new Font("Roboto", Font.ITALIC, 32));
		if (aiming) {
			if (m.isBreaking())
				g.drawString("BREAK", 730, 730);
			else if (m.toCall(turn))
				g.drawString("CALL YOUR SHOT", 655, 730);
			else if (m.scratch())
				g.drawString("BALL IN HAND", 680, 730);
		} else {
			if (m.scratch())
				g.drawString("SCRATCH", 715, 730);
		}
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
				g.drawImage(ball.getImg(), scoreboardPos[p2Count][0] + 123, scoreboardPos[p2Count][1], size, size,
						null);
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

	private void highlightPocket(int pocket, Graphics g) {
		int startX = 262 * (pocket / 4);
		int endX = startX + 262;
		int startY = 360 * ((pocket - 1) % 3);
		int endY = startY + 360;
		g.drawImage(calls, startX, startY, endX, endY, startX, startY, endX, endY, null);
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
					if (dist <= Ball.DIAMETER * Ball.DIAMETER) {
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
		g2d.drawOval(circleX - Ball.RADIUS, circleY - Ball.RADIUS, Ball.DIAMETER, Ball.DIAMETER);
		if (distSquared(cueBall.X(), cueBall.Y(), circleX, circleY) > Ball.RADIUS * Ball.RADIUS) {
			g2d.drawLine(startX, startY, circleX - (int) ((Ball.RADIUS + 1) * Math.cos(angle)),
					circleY - (int) ((Ball.RADIUS + 1) * Math.sin(angle)));
		}
	}

	private void drawBallGuidelines(Graphics2D g2d, double angle, int startX, int startY, double minDist,
			Ball willHit) {
		// determine where to put the white circle
		double z1 = distSquared(cueBall.X(), cueBall.Y(), willHit.X(), willHit.Y());
		double y = minDist;
		double x1 = Math.sqrt(z1 - y);
		double z2 = Ball.DIAMETER * Ball.DIAMETER;
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
		// System.out.println(angle + " - " + whAngle + " = " + dAngle);
		double whLength = 120 * ((Math.PI / 2 - Math.abs(dAngle)) / (Math.PI / 2));
		double dLength = 120 - whLength;

		dLength += (Ball.RADIUS + 1);
		g2d.drawLine(circleX + (int) ((Ball.RADIUS + 1) * Math.cos(whAngle)),
				circleY + (int) ((Ball.RADIUS + 1) * Math.sin(whAngle)), circleX + (int) (whLength * Math.cos(whAngle)),
				circleY + (int) (whLength * Math.sin(whAngle)));
		double cAngle = whAngle + Math.signum(dAngle) * Math.PI / 2;
		g2d.drawLine(circleX + (int) ((Ball.RADIUS + 1) * Math.cos(cAngle)),
				circleY + (int) ((Ball.RADIUS + 1) * Math.sin(cAngle)), circleX + (int) (dLength * Math.cos(cAngle)),
				circleY + (int) (dLength * Math.sin(cAngle)));
		g2d.drawOval(circleX - Ball.RADIUS, circleY - Ball.RADIUS, Ball.DIAMETER, Ball.DIAMETER);
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

	private void resetHighlightedPockets() {
		for (int i = 0; i < 6; i++) {
			highlightedPockets[i] = false;
		}
	}

	private double distSquared(double x1, double y1, double x2, double y2) {
		return (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
	}

	public void actionPerformed(ActionEvent e) {
		repaint();
	}

	public void mousePressed(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
		lastX = mouseX;
		lastY = mouseY;
		if (!placing && m.scratch()) {
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
		if (aiming && m.toCall(m.getTurn())) {
			int x = e.getX();
			int y = e.getY();
			Color c = new Color(calls.getRGB(x, y));
			if (c.getGreen() == 148) {
				m.setCalledPocket(m.getPocketNumber(x, y));
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (placing) {
			for (Ball b : balls) {
				if (b.getType() != Ball.CUE) {
					if (distSquared(b.X(), b.Y(), cuePos[0], cuePos[1]) < Ball.DIAMETER * Ball.DIAMETER) {
						double relAngle = Math.atan2(cuePos[1] - b.Y(), cuePos[0] - b.X());
						cuePos[0] = (int) (b.X() + ((Ball.DIAMETER + 1) * Math.cos(relAngle)));
						cuePos[1] = (int) (b.Y() + ((Ball.DIAMETER + 1) * Math.sin(relAngle)));
					}
				}
			}
			cueBall.setPos(cuePos[0], cuePos[1]);
			placing = false;
		}

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
