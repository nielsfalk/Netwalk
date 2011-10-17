package de.falkniel.netwalk;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class Netwalk extends Canvas {
    private static final long serialVersionUID = -6769569864616203473L;
	BufferStrategy strategy;
	private boolean waitingForEvent = false;
	private boolean netwalkRunning = true;
	private boolean lock = false; 
	private boolean spinLeft = false;
	private boolean spinRight = false;
	private int mouseH = 0;
	private int mouseW = 0;
	private boolean doMove = false;
	private boolean levelInited = false;
	private Level level;
	private int levelH;
	private int levelW;
	private JFrame frame;
	private int spinCount;
	private int targetSpinCount;
	// Draw var2
	private int windowH;
	private int windowW;
	int lineWStart;
	int lineWEnd;
	int lineHStart;
	int lineHEnd;
	int footerH;
	int newBtnHstart;
	int newBtnHeight;
	int newBtnWstart;
	int newBtnWidth;

	public static final int TRAILOR_H = 14;
	public static final int TRAILOR_W = 4;
	public static final int FOOTER_H = 70;
	public static final int FOOTER_W = 3;
	public static final int IMG_H = 49;
	public static final int IMG_W = 49;

	public static void main(String argv[]) {
		// game size
		Netwalk netwalk = new Netwalk(15, 25);
		netwalk.netwalkLoop();
	}

	private Netwalk(int h, int w) {
		levelH = h;
		levelW = w;
		windowH = h * IMG_H + TRAILOR_H + FOOTER_H;
		windowW = w * IMG_W + TRAILOR_W + FOOTER_W;
		lineWStart = TRAILOR_W - 1;
		lineWEnd = TRAILOR_W - 1 + levelW * IMG_W;
		lineHStart = TRAILOR_H - 1;
		lineHEnd = TRAILOR_H - 1 + levelH * IMG_H;
		footerH = levelH * IMG_H + TRAILOR_H + 30;
		newBtnHstart = footerH - 20;
		newBtnHeight = 55;
		newBtnWstart = TRAILOR_W + IMG_W * (levelW - 2);
		newBtnWidth = 2 * IMG_W - 1;

		frame = new JFrame("Netwalk");
		JPanel panel = (JPanel) frame.getContentPane();
		panel.setPreferredSize(new Dimension(windowW, windowH));
		panel.setLayout(null);

		setBounds(0, 0, windowW, windowH);
		panel.add(this);
		setIgnoreRepaint(true);

		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);

		WindowHandler windowHandler = new WindowHandler();
		frame.addWindowListener(windowHandler);
		frame.addWindowFocusListener(windowHandler);

		MouseInputHandler mouseInputHandler = new MouseInputHandler();
		addKeyListener(new KeyInputHandler());
		addMouseMotionListener(mouseInputHandler);
		addMouseListener(mouseInputHandler);
		addMouseWheelListener(mouseInputHandler);

		requestFocus();

		createBufferStrategy(2);
		strategy = getBufferStrategy();
	}

	private void initLevel() {
		System.out.println("level init");
		level = new Level(levelH, levelW);
		level.generate();
		targetSpinCount = level.spin();
		level.glow();
		spinCount = 0;

		// draw
		drawLevel(0);
	}

	public void drawLevel(long delta) {
		Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, windowW, windowH);

		for (int h = 0; h < level.getFields().length; h++) {
			for (int w = 0; w < level.getFields()[h].length; w++) {
				Field field = level.getFields()[h][w];
				ImageWrapper image = ImageStore.get().getImage(field.getImg());
				image.draw(g, wToPx(w), hToPx(h), field.isLocked(), field.getNextImgRotation(delta));
			}
		}

		g.setColor(Color.GRAY);

		for (int h = 0; h <= levelH; h++) {
			int lineH = TRAILOR_H - 1 + (h * IMG_H);
			g.drawLine(lineWStart, lineH, lineWEnd, lineH);// w h w h
		}

		for (int w = 0; w <= levelW; w++) {
			int lineW = TRAILOR_W - 1 + (w * IMG_H);
			g.drawLine(lineW, lineHStart, lineW, lineHEnd);// w h w h
		}

		g.setColor(Color.BLACK);
		g.drawRect(newBtnWstart, newBtnHstart, newBtnWidth, newBtnHeight);

		g.setFont(new Font("SansSerif", 0, 20));
		g.drawString("New", newBtnWstart + 30, newBtnHstart + 35);

		g.drawString("spinCount " + spinCount, TRAILOR_W, footerH);
		g.drawString("targetSpinCount " + targetSpinCount, TRAILOR_W, footerH + 30);

		g.dispose();
		strategy.show();
	}

	private void netwalkLoop() {
		long lastLoopTime = System.currentTimeMillis();

		while (netwalkRunning) {
			long delta = System.currentTimeMillis() - lastLoopTime;
			lastLoopTime = System.currentTimeMillis();

			if (levelInited) {
				if (doMove) {
					// String msg = "";
					Integer h = pxToH(mouseH);
					Integer w = pxToW(mouseW);

					if (h != null && w != null) {
						Field field = level.getFields()[h][w];
						if (lock) {
							// msg += "lock";
							field.toggleLock();
							lock = false;
						}
						if (spinLeft) {
							// msg += "spinLeft";
							if (!field.isLocked()) {
								field.leftSpin();
								spinCount++;
							}
							spinLeft = false;
						}
						if (spinRight) {
							// msg += "spinRight";
							if (!field.isLocked()) {
								field.rightSpin();
								spinCount++;
							}
							spinRight = false;
						}
					}
					else {
						// new btn
						if (spinLeft && mouseH > newBtnHstart && mouseW > newBtnWstart) {
							levelInited = false;
						}
					}
					// msg = "spinCount="+spinCount+" targetSpinCount "+targetSpinCount+" "+ msg +" mouseH:"+h+"
					// mouseW:"+w;
					// System.out.println(msg);
					doMove = false;
					level.glow();
					
					if (level.isSolved()) {
						if (h != null && w != null) {
							level.getFields()[h][w].stopRotation();
							level.glow();
							drawLevel(delta);
							if (JOptionPane.showConfirmDialog(frame, "You Win!\n\nPlay again?", "You Win!",
							    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
							{
								levelInited = false;
							}
							else {
								System.exit(0);
							}
						}

					}
				}
				drawLevel(delta);
			}
			else {
				initLevel();
				levelInited = true;
			}

			try {
				Thread.sleep(10);
			}
			catch (Exception e) {}
		}
	}

	private int hToPx(int h) {
		return TRAILOR_H + (h * IMG_H);
	}

	private int wToPx(int w) {
		return TRAILOR_W + (w * IMG_W);
	}

	private Integer pxToW(int px) {
		int retVal = (px - TRAILOR_W) / IMG_W;
		if (retVal < 0) return null;
		if (retVal >= levelW) return null;
		return retVal;
	}

	private Integer pxToH(int px) {
		int retVal = (px - TRAILOR_H) / IMG_H;
		if (retVal < 0) return null;
		if (retVal >= levelH) return null;
		return retVal;
	}

	private class KeyInputHandler extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			if (waitingForEvent) {
				return;
			}
			doMove = true;
			switch (e.getKeyCode()) {
				case KeyEvent.VK_Z:
					level.unLockAll();
					break;
				case KeyEvent.VK_SPACE:
				case KeyEvent.VK_UP:
				case KeyEvent.VK_DOWN:
				case KeyEvent.VK_ALT:
					lock = true;
					break;
				case KeyEvent.VK_A:
				case KeyEvent.VK_LEFT:
					spinLeft = true;
					break;
				case KeyEvent.VK_S:
				case KeyEvent.VK_RIGHT:
					spinRight = true;
					break;
				default:
					doMove = false;
					break;
			}
		}
	}

	private class MouseInputHandler extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (waitingForEvent) {
				return;
			}
			doMove = true;
			if (e.getButton() == MouseEvent.BUTTON1)
				spinLeft = true;
			else
				spinRight = true;
		}
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (waitingForEvent) {
				return;
			}
			doMove = true;
			if (e.getWheelRotation() > 0)
				spinRight = true;
			else
				spinLeft = true;
		}
		public void mouseMoved(MouseEvent e) {
			if (waitingForEvent) {
				return;
			}
			mouseW = e.getX();
			mouseH = e.getY();
		}
	}
	private class WindowHandler extends WindowAdapter {

		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}

		public void windowGainedFocus(WindowEvent e) {
			//System.out.println("focus");
			requestFocus();
			doMove = true;
		}
	}

}
