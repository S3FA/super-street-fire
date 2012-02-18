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

	private static final long serialVersionUID = 7000442714767712317L;

	private FireEmitterConfig fireEmitterConfig;
	
	final static float DASH_1[] = {10.0f};
	final static private BasicStroke railStroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	final static private BasicStroke dashedStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, DASH_1, 0.0f);
	final static private BasicStroke emitterOutlineStroke = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	
	
	public ArenaDisplay(FireEmitterConfig fireEmitterConfig) {
		super();
		this.setMinimumSize(new Dimension(800, 600));
		
		this.fireEmitterConfig = fireEmitterConfig;
		assert(fireEmitterConfig != null);
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
		
		final float LENGTH_PER_EMITTER_AND_SPACE_ON_RAIL = FULL_RAIL_LENGTH / (float)this.fireEmitterConfig.getNumEmittersPerRail();
		final float EMITTER_DIAMETER                     = 0.66f * LENGTH_PER_EMITTER_AND_SPACE_ON_RAIL;
		final float EMITTER_RADIUS                       = EMITTER_DIAMETER / 2.0f;
		final float DISTANCE_BETWEEN_RAIL_EMITTERS       = LENGTH_PER_EMITTER_AND_SPACE_ON_RAIL - EMITTER_DIAMETER;
		
		final float RAIL_TOP_Y    = CENTER_Y + HALF_RAIL_LENGTH;
		final float RAIL_BOTTOM_Y = CENTER_Y - HALF_RAIL_LENGTH;
		
		// Start by drawing the two central rails between the player podiums
		final float LEFT_RAIL_CENTER_X = CENTER_X - HALF_WIDTH_BETWEEN_RAILS - EMITTER_RADIUS;
		final float RIGHT_RAIL_CENTER_X = CENTER_X + HALF_WIDTH_BETWEEN_RAILS + EMITTER_RADIUS;
		
		g2.setPaint(Color.black);
		g2.setStroke(this.railStroke);
		g2.draw(new Line2D.Float(LEFT_RAIL_CENTER_X, RAIL_TOP_Y, LEFT_RAIL_CENTER_X, RAIL_BOTTOM_Y));
		g2.draw(new Line2D.Float(RIGHT_RAIL_CENTER_X, RAIL_TOP_Y, RIGHT_RAIL_CENTER_X, RAIL_BOTTOM_Y));		
		
		// Now draw all of the rail emitters as filled-in shapes
		g2.setStroke(this.emitterOutlineStroke);
		
		// NOTE: we subtract a small epsilon from the for loop condition to ensure that all the emitters get drawn
		for (float i = RAIL_TOP_Y; i >= (RAIL_BOTTOM_Y - 0.001f); i -= (EMITTER_DIAMETER + DISTANCE_BETWEEN_RAIL_EMITTERS)) {
			Ellipse2D.Float leftRailEmitterShape  = new Ellipse2D.Float(LEFT_RAIL_CENTER_X - EMITTER_RADIUS,
					i - EMITTER_RADIUS, EMITTER_DIAMETER, EMITTER_DIAMETER);
			Ellipse2D.Float rightRailEmitterShape = new Ellipse2D.Float(RIGHT_RAIL_CENTER_X - EMITTER_RADIUS,
					i - EMITTER_RADIUS, EMITTER_DIAMETER, EMITTER_DIAMETER);
			
			g2.setPaint(Color.white); // TODO: mix colours of the emitter owners... also indicate if it's a block/attack/non-game flame?
			g2.fill(leftRailEmitterShape);
			g2.fill(rightRailEmitterShape);
			g2.setPaint(Color.black);
			g2.draw(leftRailEmitterShape);
			g2.draw(rightRailEmitterShape);
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
		for (float i = -HALF_PI + INCREMENT_ANGLE; i < (HALF_PI - INCREMENT_ANGLE + 0.001f); i += INCREMENT_ANGLE) {
			Ellipse2D.Float outerRingEmitterShape  = new Ellipse2D.Float(CENTER_X + OUTER_RING_RADIUS * (float)Math.cos(i) - EMITTER_RADIUS,
					CENTER_Y + OUTER_RING_RADIUS * (float)Math.sin(i) - EMITTER_RADIUS, EMITTER_DIAMETER, EMITTER_DIAMETER);
			
			g2.setPaint(Color.white); // TODO: mix colours of the emitter owners... also indicate if it's a block/attack/non-game flame?
			g2.fill(outerRingEmitterShape);
			g2.setPaint(Color.black);
			g2.draw(outerRingEmitterShape);
		}
		
		// Now draw the left-hand side of the outer ring...
		final float THREE_HALVES_PI = 3.0f * (float)Math.PI / 2.0f;
		for (float i = HALF_PI + INCREMENT_ANGLE; i < (THREE_HALVES_PI - INCREMENT_ANGLE + 0.001f); i += INCREMENT_ANGLE) {
			Ellipse2D.Float outerRingEmitterShape  = new Ellipse2D.Float(CENTER_X + OUTER_RING_RADIUS * (float)Math.cos(i) - EMITTER_RADIUS,
					CENTER_Y + OUTER_RING_RADIUS * (float)Math.sin(i) - EMITTER_RADIUS, EMITTER_DIAMETER, EMITTER_DIAMETER);
		
			g2.setPaint(Color.white); // TODO: mix colours of the emitter owners... also indicate if it's a block/attack/non-game flame?
			g2.fill(outerRingEmitterShape);
			g2.setPaint(Color.black);
			g2.draw(outerRingEmitterShape);
		}
	}

}
