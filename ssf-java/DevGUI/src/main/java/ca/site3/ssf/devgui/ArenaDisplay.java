package ca.site3.ssf.devgui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumSet;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.FireEmitter;
import ca.site3.ssf.gamemodel.FireEmitter.Location;
import ca.site3.ssf.gamemodel.FireEmitterConfig;
import ca.site3.ssf.gamemodel.IGameModel;
import ca.site3.ssf.gamemodel.IGameModel.Entity;
import ca.site3.ssf.gamemodel.RoundEndedEvent.RoundResult;
import ca.site3.ssf.gamemodel.SystemInfoRefreshEvent;
import ca.site3.ssf.gamemodel.SystemInfoRefreshEvent.OutputDeviceStatus;
import ca.site3.ssf.guiprotocol.StreetFireGuiClient;

class ArenaDisplay extends JPanel implements MouseListener, MouseMotionListener {

	private static final long serialVersionUID = 7000442714767712317L;

	private Logger log = LoggerFactory.getLogger(getClass());
	
	final static float DASH_1[] = {10.0f};
	final static private BasicStroke RAIL_STROKE            = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	final static private BasicStroke DASHED_STROKE          = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, DASH_1, 0.0f);
	final static private BasicStroke EMITTER_OUTLINE_STROKE = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	final static private BasicStroke PODIUM_OUTLINE_STROKE  = new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	final static private BasicStroke ROUND_OUTLINE_STROKE   = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	
	final static private Font INTENSITY_FONT  = new Font("SansSerif", Font.PLAIN, 12);
	final static private Font PLAYER_FONT     = new Font("SansSerif", Font.BOLD, 16);
	final static private Font RINGMASTER_FONT = new Font("SansSerif", Font.BOLD, 12);
	final static private Font COUNTDOWN_FONT  = new Font("SansSerif", Font.ITALIC, 20);
	final static private Font ROUND_FONT      = new Font("SansSerif", Font.BOLD, 24);
	
	// Colours used when drawing the fire emitters whose flame belongs to a particular entity in the game...
	final static Color PLAYER_1_COLOUR   = new Color(0, 128, 255);
	final static Color PLAYER_2_COLOUR   = new Color(40, 205, 0);
	final static Color RINGMASTER_COLOUR = Color.orange;
	
	// Emitter status indicators
	BufferedImage DEVICE_UNKNOWN_STATUS_IMAGE;
	BufferedImage DEVICE_NOT_RESPONDING_IMAGE;
	BufferedImage DEVICE_RESPONDING_IMAGE;
	BufferedImage DEVICE_ARMED_IMAGE;
	BufferedImage DEVICE_FLAME_IMAGE;
	
	
	final private FireEmitterConfig fireEmitterConfig;
	
	private Image ssfImage;
	
	// Emitter data is stored in the same orderings as the FireEmitterModel in the gamemodel
	private EmitterData[] leftRailEmitterData  = null;
	private EmitterData[] rightRailEmitterData = null;
	private EmitterData[] outerRingEmitterData = null;
	
	private String infoText = "";
	
	private RoundResult[] roundResults = null;
	private StreetFireGuiClient client = null;
	private SystemInfoRefreshEvent latestSystemStatus = null;
	
	public ArenaDisplay(int roundsPerMatch, FireEmitterConfig fireEmitterConfig,
						StreetFireGuiClient client) {
		super();

		this.setBorder(BorderFactory.createLineBorder(Color.black, 2));
		this.setBackground(Color.white);
		
		this.fireEmitterConfig = fireEmitterConfig;
		assert(fireEmitterConfig != null);
		
		this.roundResults = new RoundResult[roundsPerMatch + 1]; // +1 for the tie breaker round...
		for (int i = 0; i < this.roundResults.length; i++) {
			this.roundResults[i] = null;
		}
		
		this.leftRailEmitterData  = new EmitterData[fireEmitterConfig.getNumEmittersPerRail()];
		this.rightRailEmitterData = new EmitterData[fireEmitterConfig.getNumEmittersPerRail()];
		for (int i = 0; i < fireEmitterConfig.getNumEmittersPerRail(); i++) {
			this.leftRailEmitterData[i]  = new EmitterData();
			this.rightRailEmitterData[i] = new EmitterData();
		}
		
		this.outerRingEmitterData = new EmitterData[fireEmitterConfig.getNumOuterRingEmitters()];
		for (int i = 0; i < fireEmitterConfig.getNumOuterRingEmitters(); i++) {
			this.outerRingEmitterData[i] = new EmitterData();
		}
		
		
		try {
			this.ssfImage = ImageIO.read(getClass().getResource("ssfsmall.jpg"));
			
			DEVICE_UNKNOWN_STATUS_IMAGE = ImageIO.read(getClass().getResource("flame_question_mark_12px.png"));
			DEVICE_NOT_RESPONDING_IMAGE = ImageIO.read(getClass().getResource("flame_greyscale_12px.png"));
			DEVICE_RESPONDING_IMAGE = ImageIO.read(getClass().getResource("flame_sepia_12px.png"));
			DEVICE_ARMED_IMAGE = ImageIO.read(getClass().getResource("flame_12px.png"));
			DEVICE_FLAME_IMAGE = ImageIO.read(getClass().getResource("flame_on_12px.png"));
			
		} catch (IOException e) {
			log.error("Couldn't load image", e);
		} catch (IllegalArgumentException e) {
			log.error("Couldn't load image", e);
		}
		
		assert(client != null);
		this.client = client;
		
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	public void setRoundResult(int roundNum, RoundResult roundResult) {
		assert(roundNum > 0 && roundNum <= this.roundResults.length);
		this.roundResults[roundNum-1] = roundResult; 
	}
	public void clearRoundResults() {
		for (int i = 0; i < this.roundResults.length; i++) {
			this.roundResults[i] = null;
		}
		this.repaint();
	}

	public void setInfoText(String text) {
		this.infoText = text;
		this.repaint();
	}
	
	public void setSystemStatus(SystemInfoRefreshEvent statusEvent) {
		this.latestSystemStatus = statusEvent;
	}
	
	public void setLeftRailEmitter(int index, EmitterData data) {
		this.leftRailEmitterData[index] = data;
		this.repaint();
	}
	public void setRightRailEmitter(int index, EmitterData data) {
		this.rightRailEmitterData[index] = data;
		this.repaint();
	}
	public void setOuterRingEmitter(int index, EmitterData data) {
		this.outerRingEmitterData[index] = data;
		this.repaint();
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		
		Graphics2D g2 = (Graphics2D)g;

		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		
		final FontMetrics INTENSITY_FONT_METRICS  = g2.getFontMetrics(INTENSITY_FONT);
		final FontMetrics PLAYER_FONT_METRICS     = g2.getFontMetrics(PLAYER_FONT);
		final FontMetrics RINGMASTER_FONT_METRICS = g2.getFontMetrics(RINGMASTER_FONT);
		
		Dimension size = this.getSize();
		
		final float CENTER_X = size.width/2.0f;
		final float CENTER_Y = size.height/2.0f;
		
		float sizeX = Math.min(750, size.width);
		float sizeY = Math.min(750, size.height);
		size.setSize(sizeX, sizeY);

		final float WIDTH_BETWEEN_RAILS       = size.width * 0.2f;
		final float HALF_WIDTH_BETWEEN_RAILS  = WIDTH_BETWEEN_RAILS / 2.0f;
		final float FULL_RAIL_LENGTH          = WIDTH_BETWEEN_RAILS * 2.0f; // Number comes from the schematic
		final float HALF_RAIL_LENGTH          = FULL_RAIL_LENGTH / 2.0f;
		
		final float LENGTH_PER_EMITTER_AND_SPACE_ON_RAIL = FULL_RAIL_LENGTH / ((float)this.fireEmitterConfig.getNumEmittersPerRail()-1);
		final float EMITTER_DIAMETER                     = 0.66f * LENGTH_PER_EMITTER_AND_SPACE_ON_RAIL;
		final float EMITTER_RADIUS                       = EMITTER_DIAMETER / 2.0f;
		final float DISTANCE_BETWEEN_RAIL_EMITTERS       = LENGTH_PER_EMITTER_AND_SPACE_ON_RAIL - EMITTER_DIAMETER;
		
		final float RAIL_TOP   = CENTER_X - HALF_RAIL_LENGTH;
		final float RAIL_BOTTOM = CENTER_X + HALF_RAIL_LENGTH;
		
		// Start by drawing the two central rails between the player podiums
		final float LEFT_RAIL_CENTER  = CENTER_Y + HALF_WIDTH_BETWEEN_RAILS + EMITTER_RADIUS;
		final float RIGHT_RAIL_CENTER = CENTER_Y - HALF_WIDTH_BETWEEN_RAILS - EMITTER_RADIUS;
		
		g2.setPaint(Color.black);
		g2.setStroke(ArenaDisplay.RAIL_STROKE);
		g2.draw(new Line2D.Float(RAIL_TOP, LEFT_RAIL_CENTER, RAIL_BOTTOM, LEFT_RAIL_CENTER));
		g2.draw(new Line2D.Float(RAIL_TOP, RIGHT_RAIL_CENTER, RAIL_BOTTOM, RIGHT_RAIL_CENTER));		
		
		// Now draw all of the rail emitters as filled-in shapes
		g2.setStroke(ArenaDisplay.EMITTER_OUTLINE_STROKE);
		g2.setFont(ArenaDisplay.INTENSITY_FONT);
		float currPosition = RAIL_BOTTOM;
		for (int i = 0; i < this.fireEmitterConfig.getNumEmittersPerRail(); i++) {
			Point2D.Float leftRailEmitterPos = new Point2D.Float(currPosition - EMITTER_RADIUS, LEFT_RAIL_CENTER - EMITTER_RADIUS);
			Point2D.Float rightRailEmitterPos = new Point2D.Float(currPosition - EMITTER_RADIUS, RIGHT_RAIL_CENTER - EMITTER_RADIUS);
			
			Ellipse2D.Float leftRailEmitterShape  = new Ellipse2D.Float(leftRailEmitterPos.x,
					leftRailEmitterPos.y, EMITTER_DIAMETER, EMITTER_DIAMETER);
			Ellipse2D.Float rightRailEmitterShape = new Ellipse2D.Float(rightRailEmitterPos.x,
					rightRailEmitterPos.y, EMITTER_DIAMETER, EMITTER_DIAMETER);
			
			this.leftRailEmitterData[i].setShape(leftRailEmitterShape);
			this.rightRailEmitterData[i].setShape(rightRailEmitterShape);
			
			g2.setPaint(this.leftRailEmitterData[i].colour);
			g2.fill(leftRailEmitterShape);
			g2.setPaint(this.rightRailEmitterData[i].colour);
			g2.fill(rightRailEmitterShape);
			
			g2.setPaint(Color.black);
			g2.draw(leftRailEmitterShape);
			g2.draw(rightRailEmitterShape);
			
			String leftEmitterPercentStr = "" + (int)(this.leftRailEmitterData[i].maxIntensity * 100) + "%";
			String rightEmitterPercentStr = "" + (int)(this.rightRailEmitterData[i].maxIntensity * 100) + "%";
			g2.drawString(leftEmitterPercentStr, leftRailEmitterPos.x + INTENSITY_FONT_METRICS.stringWidth(leftEmitterPercentStr) / 2.0f,
					leftRailEmitterPos.y + 3*EMITTER_RADIUS);
			g2.drawString(rightEmitterPercentStr, rightRailEmitterPos.x + INTENSITY_FONT_METRICS.stringWidth(rightEmitterPercentStr) / 2.0f, 
					rightRailEmitterPos.y - 5);
			
			g2.drawImage(getIconForEmitter(Location.LEFT_RAIL, i), 
					(int)(leftRailEmitterPos.x + 3 + DEVICE_FLAME_IMAGE.getWidth()/2.0f), 
					(int)(leftRailEmitterPos.y - DEVICE_FLAME_IMAGE.getHeight()),
					null);
			g2.drawImage(getIconForEmitter(Location.RIGHT_RAIL, i), 
					(int)(rightRailEmitterPos.x + 3 + DEVICE_FLAME_IMAGE.getWidth()/2.0f ), 
					(int)(rightRailEmitterPos.y + EMITTER_RADIUS + DEVICE_FLAME_IMAGE.getHeight()), 
					null);
			
			
			g2.setPaint(Color.black);
			currPosition -= (EMITTER_DIAMETER + DISTANCE_BETWEEN_RAIL_EMITTERS);
		}		
		
		// Draw the outer ring of emitters
		final float OUTER_RING_RADIUS   = WIDTH_BETWEEN_RAILS * 1.642857f; // This number comes from the schematic (23' / 14')
		final float OUTER_RING_DIAMETER = 2.0f * OUTER_RING_RADIUS;
		
		final int HALF_NUM_OUTER_RING_EMITTERS = this.fireEmitterConfig.getNumOuterRingEmitters() / 2;
		final float INCREMENT_ANGLE = (float)Math.PI / (float)(HALF_NUM_OUTER_RING_EMITTERS+1);
		final float HALF_PI = (float)Math.PI/2.0f;
		final float QUARTER_PI = (float)Math.PI/4.0f;
		
		final float OUTER_RING_X = CENTER_X - OUTER_RING_RADIUS;
		final float OUTER_RING_Y = CENTER_Y - OUTER_RING_RADIUS;
		
		g2.setFont(ArenaDisplay.INTENSITY_FONT);
		g2.setStroke(ArenaDisplay.DASHED_STROKE);
		g2.setPaint(Color.black);
		g2.draw(new Ellipse2D.Float(OUTER_RING_X, OUTER_RING_Y, OUTER_RING_DIAMETER, OUTER_RING_DIAMETER));
		
		// Start by drawing the right-hand side of the outer ring, staring with the bottom-right emitter and moving
		// up and around the outer ring to the top-right emitter...
		g2.setStroke(ArenaDisplay.EMITTER_OUTLINE_STROKE);
		float currAngle = -(float)Math.PI - HALF_PI - INCREMENT_ANGLE;

		for (int i = 0; i < HALF_NUM_OUTER_RING_EMITTERS; i++) {
			Point2D.Float outerRingEmitterPos =
					new Point2D.Float(CENTER_X + OUTER_RING_RADIUS * (float)Math.sin(currAngle) - EMITTER_RADIUS,
					CENTER_Y - OUTER_RING_RADIUS * (float)Math.cos(currAngle) - EMITTER_RADIUS);
			
			Ellipse2D.Float outerRingEmitterShape  = 
					new Ellipse2D.Float(outerRingEmitterPos.x, outerRingEmitterPos.y, EMITTER_DIAMETER, EMITTER_DIAMETER);
			
			this.outerRingEmitterData[i].setShape(outerRingEmitterShape);
			
			g2.setPaint(this.outerRingEmitterData[i].colour);
			g2.fill(outerRingEmitterShape);
			g2.setPaint(Color.black);
			g2.draw(outerRingEmitterShape);
			
			g2.drawString("" + (int)(this.outerRingEmitterData[i].maxIntensity * 100) + "%",
					outerRingEmitterPos.x + EMITTER_DIAMETER + 2, outerRingEmitterPos.y + EMITTER_RADIUS);
			
			g2.drawImage(getIconForEmitter(Location.OUTER_RING, i), 
					(int)(outerRingEmitterPos.x + EMITTER_DIAMETER) + 2, 
					(int)outerRingEmitterPos.y + 2 + DEVICE_FLAME_IMAGE.getHeight(), 
					null);
			
			currAngle -= INCREMENT_ANGLE;
		}

		// Now draw the left-hand side of the outer ring...
		currAngle = -(float)Math.PI + HALF_PI - INCREMENT_ANGLE;
		for (int i = HALF_NUM_OUTER_RING_EMITTERS; i < this.fireEmitterConfig.getNumOuterRingEmitters(); i++) {
			Point2D.Float outerRingEmitterPos =
					new Point2D.Float(CENTER_X + OUTER_RING_RADIUS * (float)Math.sin(currAngle) - EMITTER_RADIUS,
					CENTER_Y - OUTER_RING_RADIUS * (float)Math.cos(currAngle) - EMITTER_RADIUS);			
			
			Ellipse2D.Float outerRingEmitterShape  =
					new Ellipse2D.Float(outerRingEmitterPos.x, outerRingEmitterPos.y, EMITTER_DIAMETER, EMITTER_DIAMETER);
		
			this.outerRingEmitterData[i].setShape(outerRingEmitterShape);
			
			g2.setPaint(this.outerRingEmitterData[i].colour);
			g2.fill(outerRingEmitterShape);
			g2.setPaint(Color.black);
			g2.draw(outerRingEmitterShape);
			
			String emitterPercentStr = "" + (int)(this.outerRingEmitterData[i].maxIntensity * 100) + "%";
			g2.drawString(emitterPercentStr, outerRingEmitterPos.x - 2 - INTENSITY_FONT_METRICS.stringWidth(emitterPercentStr),
					outerRingEmitterPos.y + EMITTER_RADIUS);
			
			g2.drawImage(getIconForEmitter(Location.OUTER_RING, i), 
					(int)outerRingEmitterPos.x - 2 - DEVICE_FLAME_IMAGE.getWidth(), 
					(int)outerRingEmitterPos.y + 2 + DEVICE_FLAME_IMAGE.getHeight(), 
					null);
			
			currAngle -= INCREMENT_ANGLE;
		}
		
		// Draw the player podiums
		final float PODIUM_WIDTH      = WIDTH_BETWEEN_RAILS * 0.5f;
		final float HALF_PODIUM_WIDTH = PODIUM_WIDTH / 2.0f;
				
		/*
		final float PLAYER_TOP_Y  = CENTER_Y - PODIUM_WIDTH / 2.0f;
		final float PLAYER_1_TOP_Y = RAIL_BOTTOM;
		final float PLAYER_2_TOP_Y = RAIL_TOP - PODIUM_WIDTH;
		final float RINGMASTER_PODIUM_WIDTH = PODIUM_WIDTH/1.5f;
		final float RINGMASTER_LEFT_X = CENTER_X - WIDTH_BETWEEN_RAILS - RINGMASTER_PODIUM_WIDTH;
		final float RINGMASTER_TOP_Y  = CENTER_Y - (PODIUM_WIDTH/2.0f);
		*/
		
		final float RINGMASTER_PODIUM_HEIGHT = PODIUM_WIDTH/1.5f;
		final float RINGMASTER_LEFT_X  = CENTER_X - PODIUM_WIDTH / 2.0f;
		final float RINGMASTER_TOP_Y   = CENTER_Y + HALF_WIDTH_BETWEEN_RAILS + 1.5f*RINGMASTER_PODIUM_HEIGHT;
		final float PLAYER_TOP_Y  = CENTER_Y - PODIUM_WIDTH / 2.0f;
		final float PLAYER_1_LEFT_X = CENTER_X + HALF_RAIL_LENGTH;
		final float PLAYER_2_LEFT_X = CENTER_X - HALF_RAIL_LENGTH - PODIUM_WIDTH;
		
		RoundRectangle2D.Float player1PodiumShape = 
				new RoundRectangle2D.Float(PLAYER_1_LEFT_X, PLAYER_TOP_Y, PODIUM_WIDTH, PODIUM_WIDTH, 10, 10);
		g2.setStroke(ArenaDisplay.PODIUM_OUTLINE_STROKE);
		g2.setPaint(ArenaDisplay.PLAYER_1_COLOUR);
		g2.fill(player1PodiumShape);
		g2.setPaint(Color.black);
		g2.draw(player1PodiumShape);
		
		RoundRectangle2D.Float player2PodiumShape = 
				new RoundRectangle2D.Float(PLAYER_2_LEFT_X, PLAYER_TOP_Y, PODIUM_WIDTH, PODIUM_WIDTH, 10, 10);
		g2.setStroke(ArenaDisplay.PODIUM_OUTLINE_STROKE);
		g2.setPaint(ArenaDisplay.PLAYER_2_COLOUR);
		g2.fill(player2PodiumShape);
		g2.setPaint(Color.black);
		g2.draw(player2PodiumShape);
		
		RoundRectangle2D.Float ringmasterPodiumShape = 
				new RoundRectangle2D.Float(RINGMASTER_LEFT_X, RINGMASTER_TOP_Y, PODIUM_WIDTH, RINGMASTER_PODIUM_HEIGHT, 10, 10);
		g2.setStroke(ArenaDisplay.PODIUM_OUTLINE_STROKE);
		g2.setPaint(ArenaDisplay.RINGMASTER_COLOUR);
		g2.fill(ringmasterPodiumShape);
		g2.setPaint(Color.black);
		g2.draw(ringmasterPodiumShape);
		
		
		
		String player1Str = "Player 1";
		String player2Str = "Player 2";
		String ringmasterStr = "MG";
		g2.setFont(PLAYER_FONT);
		g2.drawString(player1Str, player1PodiumShape.x + (PODIUM_WIDTH - PLAYER_FONT_METRICS.stringWidth(player1Str)) / 2.0f, player1PodiumShape.y + HALF_PODIUM_WIDTH);
		g2.drawString(player2Str, player2PodiumShape.x + (PODIUM_WIDTH - PLAYER_FONT_METRICS.stringWidth(player2Str)) / 2.0f, player2PodiumShape.y + HALF_PODIUM_WIDTH);
		g2.setFont(RINGMASTER_FONT);
		g2.drawString(ringmasterStr, ringmasterPodiumShape.x + (PODIUM_WIDTH - RINGMASTER_FONT_METRICS.stringWidth(ringmasterStr)) / 2.0f,
				ringmasterPodiumShape.y + RINGMASTER_PODIUM_HEIGHT/2.0f + RINGMASTER_FONT_METRICS.getHeight()/2.0f);
		
		// Draw the countdown text...
		FontMetrics COUNTDOWN_FONT_METRICS = g2.getFontMetrics(ArenaDisplay.COUNTDOWN_FONT);
		g2.setFont(ArenaDisplay.COUNTDOWN_FONT);
		
		float tempY = 0;
		for (String line : this.infoText.split("\n")) {
            g2.drawString(line, CENTER_X - COUNTDOWN_FONT_METRICS.stringWidth(line) / 2.0f, CENTER_Y + tempY);
            tempY += COUNTDOWN_FONT_METRICS.getHeight();
		}
		
		// Draw the round display text and fill shapes...
		FontMetrics ROUND_FONT_METRICS = g2.getFontMetrics(ArenaDisplay.ROUND_FONT);
		g2.setFont(ArenaDisplay.ROUND_FONT);
		g2.setPaint(Color.darkGray);
		String roundsStr = "Rounds: ";
		g2.drawString(roundsStr, 10, ROUND_FONT_METRICS.getHeight());
		
		final float DISTANCE_BETWEEN_ROUND_SHAPES = 8;
		final float ROUND_SHAPE_SIZE              = 25;
		
		float xPos = 10 + ROUND_FONT_METRICS.stringWidth(roundsStr) + 5;
		float yPos = 8 + (((float)ROUND_FONT_METRICS.getHeight() - ROUND_SHAPE_SIZE) / 2.0f);
		
		g2.setStroke(ArenaDisplay.ROUND_OUTLINE_STROKE);
		for (int i = 0; i < this.roundResults.length; i++) {
			Rectangle2D.Float roundShape = new Rectangle2D.Float(xPos, yPos, ROUND_SHAPE_SIZE, ROUND_SHAPE_SIZE);
			
			if (this.roundResults[i] == null) {
				g2.setPaint(Color.lightGray);	
			}
			else {
				switch (this.roundResults[i]) {
				case PLAYER1_VICTORY:
					g2.setPaint(ArenaDisplay.PLAYER_1_COLOUR);
					break;
				case PLAYER2_VICTORY:
					g2.setPaint(ArenaDisplay.PLAYER_2_COLOUR);
					break;
				case TIE:
					Paint tiePaint = new GradientPaint(xPos, yPos, ArenaDisplay.PLAYER_1_COLOUR,
							xPos+ROUND_SHAPE_SIZE, yPos+ROUND_SHAPE_SIZE, ArenaDisplay.PLAYER_2_COLOUR);
					g2.setPaint(tiePaint);
					break;
				default:
					assert(false);
					break;
				}
			}
			
			g2.fill(roundShape);
			g2.setPaint(Color.black);
			g2.draw(roundShape);
			
			if (i == this.roundResults.length-2) {
				xPos += ROUND_SHAPE_SIZE + DISTANCE_BETWEEN_ROUND_SHAPES;
				g2.draw(new Line2D.Float(xPos, yPos, xPos, yPos + ROUND_SHAPE_SIZE));
				xPos += DISTANCE_BETWEEN_ROUND_SHAPES;
			}
			else {
				xPos += ROUND_SHAPE_SIZE + DISTANCE_BETWEEN_ROUND_SHAPES;
			}
		}
		
		// Draw the SSF image in the lower right
		final float SCALE_AMT = 0.5f;
		AffineTransform imgTransform = new AffineTransform();
		imgTransform.translate(this.getWidth() - SCALE_AMT * this.ssfImage.getWidth(null) - 10,
				this.getHeight() - SCALE_AMT * this.ssfImage.getHeight(null) - 10);		
		imgTransform.scale(SCALE_AMT, SCALE_AMT);

		g2.drawImage(this.ssfImage, imgTransform, null);
	}

	public void mouseClicked(MouseEvent event) {
		this.mouseTouchEmitters(event);
	}

	public void mouseEntered(MouseEvent event) {
	}

	public void mouseExited(MouseEvent event) {
	}

	public void mousePressed(MouseEvent event) {
		this.mouseTouchEmitters(event);
	}

	public void mouseReleased(MouseEvent event) {
	}

	public void mouseDragged(MouseEvent event) {
		this.mouseTouchEmitters(event);
	}

	public void mouseMoved(MouseEvent event) {
		this.mouseTouchEmitters(event);
	}
	
	/**
	 * Private helper that will take a mouse event and then update the emitters so that it simulates
	 * what the AndroidGUI will eventually be doing by having the Ringmaster 'touch' fire emitters in order
	 * to turn them on and create crowd-pleasing displays of fire.
	 * @param event
	 */
	private void mouseTouchEmitters(MouseEvent event) {
		EnumSet<IGameModel.Entity> contributors = EnumSet.noneOf(IGameModel.Entity.class);
		
		if ((event.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
			
			if ((event.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK) {
				contributors.add(Entity.PLAYER1_ENTITY);
			}
			if ((event.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {
				contributors.add(Entity.PLAYER2_ENTITY);
			}
			
			contributors.add(Entity.RINGMASTER_ENTITY);
		}
		
		if (contributors.isEmpty()) {
			return;
		}
		
		for (int i = 0; i < this.leftRailEmitterData.length; i++) {
			if (this.leftRailEmitterData[i].contains(event.getX(), event.getY())) {
				try {
					client.activateEmitter(Location.LEFT_RAIL, i, FireEmitter.MAX_INTENSITY, contributors);
				} catch (IOException ex) {
					log.warn("Could not activate left rail emitter",ex);
				}
				return;
			}
		}
		for (int i = 0; i < this.rightRailEmitterData.length; i++) {
			if (this.rightRailEmitterData[i].contains(event.getX(), event.getY())) {
				try {
					client.activateEmitter(Location.RIGHT_RAIL, i, FireEmitter.MAX_INTENSITY, contributors);
				} catch (IOException ex) {
					log.warn("Could not activate right rail emitter",ex);
				}
				return;
			}
		}
		
		// Outer ring can only have a ringmaster entity contribute to it
		contributors.clear();
		contributors.add(Entity.RINGMASTER_ENTITY);
		for (int i = 0; i < this.outerRingEmitterData.length; i++) {
			if (this.outerRingEmitterData[i].contains(event.getX(), event.getY())) {
				try {
					client.activateEmitter(Location.OUTER_RING, i, FireEmitter.MAX_INTENSITY, contributors);
				} catch (IOException ex) {
					log.warn("Could not activate outter rail emitter",ex);
				}
				return;
			}
		}
	}

	
	private Image getIconForEmitter(Location location, int index) {
		if (latestSystemStatus == null) {
			return DEVICE_UNKNOWN_STATUS_IMAGE;
		}
		try {
			OutputDeviceStatus status = latestSystemStatus.getDeviceStatus(location, index);
			if (status.isFlame) {
				return DEVICE_FLAME_IMAGE;
			} else if (status.isArmed) {
				return DEVICE_ARMED_IMAGE;
			} else if (status.isResponding) {
				return DEVICE_RESPONDING_IMAGE;
			} else {
				return DEVICE_NOT_RESPONDING_IMAGE;
			}
		} catch (IllegalArgumentException ex) {
			log.error("Invalid location / index",ex);
			return DEVICE_UNKNOWN_STATUS_IMAGE;
		}
		
	}
}
