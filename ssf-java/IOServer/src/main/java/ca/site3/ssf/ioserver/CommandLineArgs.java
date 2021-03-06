package ca.site3.ssf.ioserver;

import java.io.File;
import java.net.InetAddress;

import com.beust.jcommander.Parameter;

/**
 * Stores command line arguments and defines their defaults.
 * 
 * @author greg
 */
public class CommandLineArgs {

	@Parameter(names = { "-log", "-verbose" }, description = "Level of verbosity -- 0 (OFF) to 5 (ERROR)")
	public Integer verbosity = 3;
	
	@Parameter(names = "-chipDamage", description = "Chip damage enabled")
	public Boolean isChipDamage = true;
	
	@Parameter(names = "-chipDamagePercent", description = "Chip damage percentage (percentage of an attack's damage that transfers to a blocking player)")
	public Float chipDamagePercentage = 0.1f;
	
	@Parameter(names = "-actionGap", description = "Minimum time (in seconds) between player actions")
	public Double minTimeBetweenPlayerActionsInSecs = 0.75;
	
	@Parameter(names = "-actionPtRegenRate", description = "Regeneration rate of player action points (in action points per second)")
	public Float actionPointRegenRate = 8.0f;
	
	@Parameter(names="-roundLength", description="Length of rounds (in seconds)")
	public Integer roundTimeInSecs = 45;
	
	@Parameter(names="-numRounds", description="Number of rounds per match")
	public Integer numRoundsPerMatch = 3;
	
	
	@Parameter(names="-tickFreq", description="Tick frequency (Hz)")
	public Integer tickFrequency = 50;
	
	@Parameter(names={"-devicePort","-p"}, description="Port to listen on for peripherals")
	public Integer devicePort = 3000;
	
	@Parameter(names={"-heartbeatPort","-h"}, description="Port to listen on for heartbeats")
	public Integer heartbeatPort = 55555;
	
	@Parameter(names={"-guiPort","-g"}, description="Port to listen on for GUI connections")
	public Integer guiPort = 31337;
	
	@Parameter(names={"-serialDevice","-s"}, description="Serial device used to send data to flamethrowers, timer, etc.")
	public String serialDevice = System.getProperty("os.name").toLowerCase().contains("win") ? "COM11" : "/dev/tty.xbee";

	@Parameter(names={"-gloveInterfaceIP"}, description="The IP Network Interface of the IOServer for listening to glove data")
	public String gloveInterfaceIP = "192.168.100.2";
	
	@Parameter(names={"-gestureEngine", "-ge"}, description="File path to the gesture recognition engine used for recongnizing game gestures")
	public String gestureEngineFilepath = new File(System.getProperty("user.dir"), "../../data/gesture/gesture_recognizer_engine.eng").toString();
	
	@Parameter(names={"-encrypt", "-e"}, description="Use SSL to encrypt wifi communications")
	public Boolean useSSL = false;
	
	
	public CommandLineArgs() {
		try {
			gloveInterfaceIP = InetAddress.getLocalHost().toString().substring(InetAddress.getLocalHost().toString().indexOf("/")+1);
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("Log level: "+verbosity+ "");
		buf.append("\nChip damage: "+isChipDamage);
		if (isChipDamage) {
			buf.append(" (" + chipDamagePercentage +"%)");
		}
		buf.append("\nAction gap: "+ minTimeBetweenPlayerActionsInSecs +"s");
		buf.append("\nRound length: "+ roundTimeInSecs+ "s");
		buf.append("\nNumber of rounds: "+ numRoundsPerMatch +"s");
		buf.append("\nTick frequency: "+ tickFrequency + " Hz");
		buf.append("\nPeripheral port: "+ devicePort);
		buf.append("\nHeartbeat port: "+ heartbeatPort);
		buf.append("\nGUI port: "+ guiPort);
		buf.append("\nUsing SSL: "+ useSSL);
		buf.append("\nSerial device: "+ serialDevice);
		buf.append("\nI/O Server IP: "+ gloveInterfaceIP);
		buf.append("\nLoading Gesture file: "+ gestureEngineFilepath);
		
		return buf.toString();
	}
}
