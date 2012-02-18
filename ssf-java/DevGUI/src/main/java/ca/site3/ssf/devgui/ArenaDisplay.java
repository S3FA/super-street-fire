package ca.site3.ssf.devgui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import ca.site3.ssf.gamemodel.FireEmitterConfig;

class ArenaDisplay extends Container {

	final private class EmitterData {
		final Color colour;
		
		EmitterData() {
			this.colour = Color.darkGray;
		}
		
		EmitterData(float[] intensities, Color[] colours) {
			assert(intensities.length == colours.length);
			
			int totalRed = 0;
			int totalGreen = 0;
			int totalBlue = 0;
			
			for (int i = 0; i < intensities.length; i++) {
				totalRed   = Math.min(255, (int)(totalRed + intensities[i] * colours[i].getRed()));
				totalGreen = Math.min(255, (int)(totalRed + intensities[i] * colours[i].getGreen()));
				totalBlue  = Math.min(255, (int)(totalRed + intensities[i] * colours[i].getBlue()));
			}
			
			if (totalRed == 0 && totalGreen == 0 && totalBlue == 0) {
				this.colour = Color.darkGray;
			}
			else {
				this.colour = new Color(totalRed, totalGreen, totalBlue);
			}
		}

	}
	
	private static final long serialVersionUID = 7000442714767712317L;

	final static float DASH_1[] = {10.0f};
	final static private BasicStroke railStroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	final static private BasicStroke dashedStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, DASH_1, 0.0f);
	final static private BasicStroke emitterOutlineStroke = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	
	// Colours used when drawing the fire emitters whose flame belongs to a particular entity in the game...
	final static private Color PLAYER_1_COLOUR   = new Color(1.0f, 0.0f, 0.0f);
	final static private Color PLAYER_2_COLOUR   = new Color(0.0f, 0.0f, 1.0f);
	final static private Color RINGMASTER_COLOUR = Color.yellow;
	
	private FireEmitterConfig fireEmitterConfig;
	
	// Emitter data is stored in the same orderings as the FireEmitterModel in the gamemodel
	private EmitterData[] leftRingColours  = null;
	private EmitterData[] rightRingColours = null;
	private EmitterData[] outerRingColours = null;
	
	
	public ArenaDisplay(FireEmitterConfig fireEmitterConfig) {
		super();
		this.setMinimumSize(new Dimension(800, 600));
		
		this.fireEmitterConfig = fireEmitterConfig;
		assert(fireEmitterConfig != null);
		
		this.leftRingColours  = new EmitterData[fireEmitterConfig.getNumEmittersPerRail()];
		this.rightRingColours = new EmitterData[fireEmitterConfig.getNumEmittersPerRail()];
		for (int i = 0; i < fireEmitterConfig.getNumEmittersPerRail(); i++) {
			this.leftRingColours[i]  = new EmitterData();
			this.rightRingColours[i] = new EmitterData();
		}
		
		this.outerRingColours = new EmitterData[fireEmitterConfig.getNumOuterRingEmitters()];
		for (int i = 0; i < fireEmitterConfig.getNumOuterRingEmitters(); i++) {
			this.outerRingColours[i] = new EmitterData();
		}
	}

	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		Dimension size = this.getSize();
		
		final float CENTER_X = size.width/2.0f;
		final float CENTER_Y = size.height/2.0f;
		
		final float WIDTH_BETWEEN_RAILS       = size.width * 0.2f;
		final float HALF_WIDTH_BETWEEN_RAILS  = WIDTH_BETWEEN_RAILS / 2.0f;
		final float FULL_RAIL_LENGTH          = WIDTH_BETWEEN_RAILS * 2.0f; // Number comes from the schematic
		final float HALF_RAIL_LENGTH          = FULL_RAIL_LENGTH / 2.0f;
		
		final float LENGTH_PER_EMITTER_AND_SPACE_ON_RAIL = FULL_RAIL_LENGTH / ((float)this.fireEmitterConfig.getNumEmittersPerRail()-1);
		final float EMITTER_DIAMETER                     = 0.66f * LENGTH_PER_EMITTER_AND_SPACE_ON_RAIL;
		final float EMITTER_RADIUS                       = EMITTER_DIAMETER / 2.0f;
		final float DISTANCE_BETWEEN_RAIL_EMITTERS       = LENGTH_PER_EMITTER_AND_SPACE_ON_RAIL - EMITTER_DIAMETER;
		
		final float RAIL_TOP_Y    = CENTER_Y - HALF_RAIL_LENGTH;
		final float RAIL_BOTTOM_Y = CENTER_Y + HALF_RAIL_LENGTH;
		
		// Start by drawing the two central rails between the player podiums
		final float LEFT_RAIL_CENTER_X = CENTER_X - HALF_WIDTH_BETWEEN_RAILS - EMITTER_RADIUS;
		final float RIGHT_RAIL_CENTER_X = CENTER_X + HALF_WIDTH_BETWEEN_RAILS + EMITTER_RADIUS;
		
		g2.setPaint(Color.black);
		g2.setStroke(this.railStroke);
		g2.draw(new Line2D.Float(LEFT_RAIL_CENTER_X, RAIL_TOP_Y, LEFT_RAIL_CENTER_X, RAIL_BOTTOM_Y));
		g2.draw(new Line2D.Float(RIGHT_RAIL_CENTER_X, RAIL_TOP_Y, RIGHT_RAIL_CENTER_X, RAIL_BOTTOM_Y));		
		
		// Now draw all of the rail emitters as filled-in shapes
		g2.setStroke(this.emitterOutlineStroke);
		
		float currPosition = RAIL_BOTTOM_Y;
		for (int i = 0; i < this.fireEmitterConfig.getNumEmittersPerRail(); i++) {
			Ellipse2D.Float leftRailEmitterShape  = new Ellipse2D.Float(LEFT_RAIL_CENTER_X - EMITTER_RADIUS,
					currPosition - EMITTER_RADIUS, EMITTER_DIAMETER, EMITTER_DIAMETER);
			Ellipse2D.Float rightRailEmitterShape = new Ellipse2D.Float(RIGHT_RAIL_CENTER_X - EMITTER_RADIUS,
					currPosition - EMITTER_RADIUS, EMITTER_DIAMETER, EMITTER_DIAMETER);
			
			g2.setPaint(this.leftRingColours[i].colour);
			g2.fill(leftRailEmitterShape);
			g2.setPaint(this.rightRingColours[i].colour);
			g2.fill(rightRailEmitterShape);
			
			g2.setPaint(Color.black);
			g2.draw(leftRailEmitterShape);
			g2.draw(rightRailEmitterShape);
			
			currPosition -= (EMITTER_DIAMETER + DISTANCE_BETWEEN_RAIL_EMITTERS);
		}
		
		// Draw the outer ring of emitters
		final float OUTER_RING_RADIUS   = WIDTH_BETWEEN_RAILS * 1.642857f; // This number comes from the schematic (23' / 14')
		final float OUTER_RING_DIAMETER = 2.0f * OUTER_RING_RADIUS;
		
		final int HALF_NUM_OUTER_RING_EMITTERS = this.fireEmitterConfig.getNumOuterRingEmitters() / 2;
		final float INCREMENT_ANGLE = (float)Math.PI / (float)(HALF_NUM_OUTER_RING_EMITTERS+1);
		final float HALF_PI = (float)Math.PI/2.0f;
		
		final float OUTER_RING_X = CENTER_X - OUTER_RING_RADIUS;
		final float OUTER_RING_Y = CENTER_Y - OUTER_RING_RADIUS;
		
		g2.setStroke(dashedStroke);
		g2.setPaint(Color.black);
		g2.draw(new Ellipse2D.Float(OUTER_RING_X, OUTER_RING_Y, OUTER_RING_DIAMETER, OUTER_RING_DIAMETER));
		
		// Start by drawing the right-hand side of the outer ring, staring with the bottom-right emitter and moving
		// up and around the outer ring to the top-right emitter...
		g2.setStroke(this.emitterOutlineStroke);
		float currAngle = -HALF_PI + INCREMENT_ANGLE;
		for (int i = 0; i < HALF_NUM_OUTER_RING_EMITTERS; i++) {
			Ellipse2D.Float outerRingEmitterShape  = new Ellipse2D.Float(CENTER_X + OUTER_RING_RADIUS * (float)Math.cos(currAngle) - EMITTER_RADIUS,
					CENTER_Y + OUTER_RING_RADIUS * (float)Math.sin(currAngle) - EMITTER_RADIUS, EMITTER_DIAMETER, EMITTER_DIAMETER);
			
			g2.setPaint(this.outerRingColours[i].colour);
			g2.fill(outerRingEmitterShape);
			g2.setPaint(Color.black);
			g2.draw(outerRingEmitterShape);
			
			currAngle += INCREMENT_ANGLE;
		}
		
		// Now draw the left-hand side of the outer ring...
		currAngle = HALF_PI + INCREMENT_ANGLE;
		for (int i = HALF_NUM_OUTER_RING_EMITTERS; i < this.fireEmitterConfig.getNumOuterRingEmitters(); i++) {
			Ellipse2D.Float outerRingEmitterShape  = new Ellipse2D.Float(CENTER_X + OUTER_RING_RADIUS * (float)Math.cos(currAngle) - EMITTER_RADIUS,
					CENTER_Y + OUTER_RING_RADIUS * (float)Math.sin(currAngle) - EMITTER_RADIUS, EMITTER_DIAMETER, EMITTER_DIAMETER);
		
			g2.setPaint(this.outerRingColours[i].colour);
			g2.fill(outerRingEmitterShape);
			g2.setPaint(Color.black);
			g2.draw(outerRingEmitterShape);
			
			currAngle += INCREMENT_ANGLE;
		}
	}

}
