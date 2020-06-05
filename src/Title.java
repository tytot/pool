import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Title extends JPanel implements ActionListener, MouseListener {

	private BufferedImage titleScreen;
	private BufferedImage[] selected = new BufferedImage[3];

	private int gamemode = 0;

	public Title() {
		try {
			titleScreen = ImageIO.read(new File("images/title/titlescreen.jpg"));
			selected[0] = ImageIO.read(new File("images/title/select1.png"));
			selected[1] = ImageIO.read(new File("images/title/select2.png"));
			selected[2] = ImageIO.read(new File("images/title/select3.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				showMenu();
			}
		});
	}

	private static void showMenu() {
		Title title = new Title();
		title.addMouseListener(title);

		JFrame frame = new JFrame("Pool");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(title);
		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public Dimension getPreferredSize() {
		int width = titleScreen.getWidth();
		int height = titleScreen.getHeight();
		return new Dimension(width, height);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(titleScreen, 0, 0, null);
		if (gamemode != 0) {
			g.drawImage(selected[gamemode - 1], 0, 0, null);
		}
	}

	private void startGame() {
		Pool.createTable(gamemode);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		if (y >= 390 && y <= 650) {
			int initialGamemode = gamemode;
			if (x >= 45 && x <= 305) {
				if (initialGamemode != 1)
					gamemode = 1;
				else
					gamemode = 0;
			}
			if (x >= 350 && x <= 610) {
				if (initialGamemode != 2)
					gamemode = 2;
				else
					gamemode = 0;
			}
			if (x >= 655 && x <= 915) {
				if (initialGamemode != 3)
					gamemode = 3;
				else
					gamemode = 0;
			}
			if (gamemode != initialGamemode)
				repaint();
		} else if (gamemode != 0) {
			if (y >= 800 && y <= 910) {
				if (x >= 305 && x <= 655)
					startGame();
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

}
