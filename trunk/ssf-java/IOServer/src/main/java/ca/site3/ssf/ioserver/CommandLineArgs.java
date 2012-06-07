package ca.site3.ssf.ioserver;

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
	public Double minTimeBetweenPlayerActionsInSecs = 0.1;
	
	@Parameter(names="-roundLength", description="Length of rounds (in seconds)")
	public Integer roundTimeInSecs = 60;
	
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
	
	@Parameter(names={"-gloveInterfaceIP"}, description="The IP Network Interface of the IOServer for listening to glove data")
	public String gloveInterfaceIP = "192.168.100.2";
	
	
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
		
		return buf.toString();
	}
}
