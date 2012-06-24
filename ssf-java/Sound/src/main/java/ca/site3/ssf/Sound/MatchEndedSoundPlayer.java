package ca.site3.ssf.Sound;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.MatchEndedEvent;

class MatchEndedSoundPlayer extends SoundPlayer {
	
	private PlaybackHandler p1VictoryPlayback;
	private PlaybackHandler p2VictoryPlayback;
		
	MatchEndedSoundPlayer(SoundPlayerController controller) {
		super(controller);
		
		String tempPath = "";
		AudioSettings globalSettings = controller.getAudioSettings();
		
		tempPath = controller.getResourcePath() +
				controller.getConfigProperties().getProperty("MatchResult.PlayerOneVictory");
		this.p1VictoryPlayback = new PlaybackHandler(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1));
		
		tempPath = controller.getResourcePath() +
				controller.getConfigProperties().getProperty("MatchResult.PlayerTwoVictory");
		this.p2VictoryPlayback = new PlaybackHandler(controller, tempPath,
				new PlaybackSettings(globalSettings.getVolume(), PlaybackSettings.BALANCED_PAN, 1));
	}
	
	public PlaybackHandler getAudioPlaybackHandler(IGameModelEvent gameModelEvent) {
		if (gameModelEvent == null || gameModelEvent.getType() != IGameModelEvent.Type.MATCH_ENDED) {
			return null;
		}
		
		MatchEndedEvent event = (MatchEndedEvent)gameModelEvent;
		
		switch (event.getMatchResult()) {
		case PLAYER1_VICTORY: {
			return this.p1VictoryPlayback;
		}
		case PLAYER2_VICTORY: {
			return this.p2VictoryPlayback;
		}
		default:
			assert(false);
			break;
		}
		
		return null;
	}
	
	public boolean isBackgroundSoundPlayer(IGameModelEvent gameModelEvent) {
		return false;
	}
}
