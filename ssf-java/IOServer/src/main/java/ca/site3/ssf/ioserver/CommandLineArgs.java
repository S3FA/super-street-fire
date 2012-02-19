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
	public Boolean isChipDamage = false;
	
	@Parameter(names = "-actionGap", description = "Minimum time (in seconds) between player actions")
	public Double minTimeBetweenPlayerActionsInSecs;
	
	@Parameter(names="-roundLength", description="Length of rounds (in seconds)")
	public Integer roundTimeInSecs = 60;
	
	@Parameter(names="-numRounds", description="Number of rounds per match")
	public Integer numRoundsPerMatch = 3;
	
	
	@Parameter(names="-tickFreq", description="Tick frequency (Hz)")
	public Integer tickFrequency = 50;
}
