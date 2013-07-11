package ca.site3.ssf.Sound;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.MatchEndedEvent;
import ca.site3.ssf.gamemodel.MatchEndedEvent.MatchResult;

class MatchEndedSoundPlayer extends SoundPlayer {

	private Map<MatchResult, PlaybackHandler> actionAudioMap = new HashMap<MatchResult, PlaybackHandler>(MatchResult.values().length);
		
	MatchEndedSoundPlayer(SoundPlayerController controller) {
		super(controller);
		
		Properties configProperties = controller.getConfigProperties();
		PlaybackSettings playbackSettings = getDefaultPlaybackSettings();

		actionAudioMap.put(MatchResult.PLAYER1_VICTORY, PlaybackHandler.build(controller, configProperties.getProperty("MatchResult.PlayerOneVictory"), playbackSettings));
		actionAudioMap.put(MatchResult.PLAYER2_VICTORY, PlaybackHandler.build(controller, configProperties.getProperty("MatchResult.PlayerTwoVictory"), playbackSettings));
	}
	
	// Get the default playback settings for this sound player
	private PlaybackSettings getDefaultPlaybackSettings()
	{
		return new PlaybackSettings(controller.getAudioSettings().getVolume(), false, false);
	}
	
	public PlaybackHandler getAudioPlaybackHandler(IGameModelEvent gameModelEvent) {
		if (gameModelEvent == null || gameModelEvent.getType() != IGameModelEvent.Type.MATCH_ENDED) {
			return null;
		}
		
		MatchEndedEvent event = (MatchEndedEvent)gameModelEvent;
		return this.actionAudioMap.get(event.getMatchResult());
	}
	
	public boolean isBackgroundSoundPlayer(IGameModelEvent gameModelEvent) {
		return false;
	}
}
