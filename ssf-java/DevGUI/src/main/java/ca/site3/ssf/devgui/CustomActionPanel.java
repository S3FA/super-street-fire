package ca.site3.ssf.devgui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ca.site3.ssf.guiprotocol.StreetFireGuiClient;

class CustomActionPanel extends JPanel implements ChangeListener, ActionListener {

	private static final long serialVersionUID = 1L;

	enum Hands { 
		LEFT_HAND("Left", true, false),
		RIGHT_HAND("Right", false, true),
		BOTH_HANDS("Both", true, true);
		
		private final String name;
		private boolean left;
		private boolean right;
		
		Hands(String name, boolean left, boolean right) {
			this.name = name;
			this.left = left;
			this.right = right;
		}
		
		public String toString() {
			return this.name;
		}
		boolean left() {
			return this.left;
		}
		boolean right() {
			return this.right;
		}
		
	};
	
	private StreetFireGuiClient client = null;
	
	private JComboBox playerComboBox = null;
	private JComboBox handsComboBox    = null;
	
	private JSlider durationSlider       = null;
	private JSlider damagePerFlameSlider = null;
	private JSlider accelerationSlider   = null;
	private JSlider fireWidthSlider      = null;
	
	private JButton executeButton = null;
	
	CustomActionPanel(StreetFireGuiClient client) {
        assert(client != null);
		this.client = client;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
        JPanel controlPanel = new JPanel();
        controlPanel.setBorder(new EmptyBorder(5,5,5,5));
        controlPanel.setLayout(gridBagLayout);
		
		FormLayoutHelper formLayoutHelper = new FormLayoutHelper();
		
		// Player selection controls -------------------------------------------------
		JLabel playerLabel = new JLabel("Player:");
		this.playerComboBox = new JComboBox();
		this.playerComboBox.addItem(new Integer(1));
		this.playerComboBox.addItem(new Integer(2));
		this.playerComboBox.setSelectedIndex(0);
		this.playerComboBox.addActionListener(this);

		formLayoutHelper.addLabel(playerLabel, controlPanel);
		formLayoutHelper.addMiddleField(this.playerComboBox, controlPanel);
		formLayoutHelper.addLastField(new JLabel(""), controlPanel);
		
		// Hand selection controls ---------------------------------------------------
		JLabel handsLabel = new JLabel("Hand(s):");
		this.handsComboBox = new JComboBox();
		this.handsComboBox.addItem(Hands.LEFT_HAND);
		this.handsComboBox.addItem(Hands.RIGHT_HAND);
		this.handsComboBox.addItem(Hands.BOTH_HANDS);
		this.handsComboBox.setSelectedIndex(0);
		this.handsComboBox.addActionListener(this);
		formLayoutHelper.addLabel(handsLabel, controlPanel);
		formLayoutHelper.addMiddleField(this.handsComboBox, controlPanel);
		formLayoutHelper.addLastField(new JLabel(""), controlPanel);
		
		// Duration controls ----------------------------------------------------------
		final int DURATION_TICK_LENGTH = 100;
		JLabel durationLabel = new JLabel("Duration (s):");
		this.durationSlider = new JSlider(10, DURATION_TICK_LENGTH);
		this.durationSlider.setMajorTickSpacing(10);
		this.durationSlider.setMinorTickSpacing(1);
		this.durationSlider.setPaintTicks(true);
		this.durationSlider.setSnapToTicks(true);
		
		Hashtable<Integer, JLabel> durationLabelTable = new Hashtable<Integer, JLabel>();
		for (int i = 10; i <= DURATION_TICK_LENGTH; i += 10) {
			durationLabelTable.put(new Integer(i), new JLabel(Integer.toString(i/10)));
		}
		this.durationSlider.setLabelTable(durationLabelTable);
		this.durationSlider.setPaintLabels(true);
		this.durationSlider.setSnapToTicks(true);
		
		this.durationSlider.addChangeListener(this);
		this.durationSlider.setValue(30);
		formLayoutHelper.addLabel(durationLabel, controlPanel);
		formLayoutHelper.addLastField(this.durationSlider, controlPanel);
		
		// Damage per emitter controls -----------------------------------------------
		final int DAMAGE_TICK_LENGTH = 20;
		JLabel dmgPerEmitterLabel = new JLabel("Damage Per Flame:");
		this.damagePerFlameSlider = new JSlider(1, DAMAGE_TICK_LENGTH);
		this.damagePerFlameSlider.setMajorTickSpacing(5);
		this.damagePerFlameSlider.setMinorTickSpacing(1);
		this.damagePerFlameSlider.setPaintTicks(true);
		this.damagePerFlameSlider.setSnapToTicks(true);
		
		Hashtable<Integer, JLabel> damageLabelTable = new Hashtable<Integer, JLabel>();
		damageLabelTable.put(new Integer(1), new JLabel(Integer.toString(1)));
		for (int i = 5; i <= DAMAGE_TICK_LENGTH; i += 5) {
			damageLabelTable.put(new Integer(i), new JLabel(Integer.toString(i)));
		}
		this.damagePerFlameSlider.setLabelTable(damageLabelTable);
		this.damagePerFlameSlider.setPaintLabels(true);
		this.damagePerFlameSlider.setSnapToTicks(true);
		
		this.damagePerFlameSlider.addChangeListener(this);
		this.damagePerFlameSlider.setValue(5);
		formLayoutHelper.addLabel(dmgPerEmitterLabel, controlPanel);
		formLayoutHelper.addLastField(this.damagePerFlameSlider, controlPanel);
		
		// Acceleration controls -----------------------------------------------------
		final int ACCEL_TICK_LENGTH = 80;
		JLabel accelLabel = new JLabel("Acceleration (emitter/s^2):");
		this.accelerationSlider = new JSlider(0, ACCEL_TICK_LENGTH);
		this.accelerationSlider.setMajorTickSpacing(10);
		this.accelerationSlider.setMinorTickSpacing(1);
		this.accelerationSlider.setPaintTicks(true);
		this.accelerationSlider.setSnapToTicks(true);
		
		Hashtable<Integer, JLabel> accelLabelTable = new Hashtable<Integer, JLabel>();
		for (int i = 0; i <= ACCEL_TICK_LENGTH; i += 10) {
			accelLabelTable.put(new Integer(i), new JLabel(Integer.toString(i/10)));
		}
		this.accelerationSlider.setLabelTable(accelLabelTable);
		this.accelerationSlider.setPaintLabels(true);
		
		this.accelerationSlider.addChangeListener(this);
		this.accelerationSlider.setValue(0);
		formLayoutHelper.addLabel(accelLabel, controlPanel);
		formLayoutHelper.addLastField(this.accelerationSlider, controlPanel);
		
		// Fire width controls -------------------------------------------------------
		JLabel fireWidthLabel = new JLabel("Flame width:");
		this.fireWidthSlider  = new JSlider(1, 5);
		this.fireWidthSlider.setMajorTickSpacing(1);
		this.fireWidthSlider.setPaintTicks(true);
		this.fireWidthSlider.setPaintLabels(true);
		this.fireWidthSlider.setSnapToTicks(true);
		this.fireWidthSlider.addChangeListener(this);
		this.fireWidthSlider.setValue(1);
		formLayoutHelper.addLabel(fireWidthLabel, controlPanel);
		formLayoutHelper.addLastField(this.fireWidthSlider, controlPanel);
		
		// Button panel...
		JPanel executePanel = new JPanel();
		executePanel.setLayout(new FlowLayout());
		this.executeButton = new JButton("Execute");
		this.executeButton.addActionListener(this);
		executePanel.add(this.executeButton);
		
		this.setLayout(new BorderLayout());
		this.add(controlPanel, BorderLayout.CENTER);
		this.add(executePanel, BorderLayout.SOUTH);
	}

	@Override
	public void stateChanged(ChangeEvent event) {
		if (event.getSource() == this.durationSlider) {
		}
		else if (event.getSource() == this.damagePerFlameSlider) {
		}
		else if (event.getSource() == this.accelerationSlider) {	
		}
		else if (event.getSource() == this.fireWidthSlider) {
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == this.executeButton) {
			// Gather values from all of the controls
			int playerNum = (Integer)this.playerComboBox.getSelectedItem();
			boolean usesLeftHand  = ((Hands)this.handsComboBox.getSelectedItem()).left();
			boolean usesRightHand = ((Hands)this.handsComboBox.getSelectedItem()).right();
			float damagePerFlame  = this.damagePerFlameSlider.getValue();
			int flameWidth        = this.fireWidthSlider.getValue();
			double durationInSecs = (double)this.durationSlider.getValue() / 10.0;
			double acceleration   = (double)this.accelerationSlider.getValue() / 10.0;
			
			try {
				this.client.executeGenericAction(playerNum, usesLeftHand, usesRightHand,
						damagePerFlame, flameWidth, durationInSecs, acceleration);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if (event.getSource() == this.playerComboBox) {
		}
		else if (event.getSource() == this.handsComboBox) {
		}
	}
	
	
	
}
