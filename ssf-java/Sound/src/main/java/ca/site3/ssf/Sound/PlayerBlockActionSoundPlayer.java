package ca.site3.ssf.Sound;

import java.util.Properties;

import ca.site3.ssf.gamemodel.IGameModelEvent;
import ca.site3.ssf.gamemodel.PlayerBlockActionEvent;

class PlayerBlockActionSoundPlayer extends SoundPlayer {
	
	PlaybackHandler player1BlockSuccess;
	PlaybackHandler player2BlockSuccess;

	PlayerBlockActionSoundPlayer(SoundPlayerController controller) {
		super(controller);
		
		Properties configProperties = controller.getConfigProperties();
		AudioSettings globalSettings = controller.getAudioSettings();
		
		player1BlockSuccess = PlaybackHandler.build(controller, configProperties.getProperty("BlockType.P1Block"), new PlaybackSettings(globalSettings.getVolume(), false, true));
		player2BlockSuccess = PlaybackHandler.build(controller, configProperties.getProperty("BlockType.P2Block"), new PlaybackSettings(globalSettings.getVolume(), false, true));
	}
	
	// Get the default playback settings for this sound player
	public PlaybackHandler getAudioPlaybackHandler(IGameModelEvent gameModelEvent) {
		if (gameModelEvent == null || gameModelEvent.getType() != IGameModelEvent.Type.PLAYER_ATTACK_ACTION) {
			return null;
		}
		
		PlayerBlockActionEvent event = (PlayerBlockActionEvent)gameModelEvent;

		// 10% of the time nyan cat is triggered, play Nyan Waits instead
		if(event.getBlockWasEffective() && event.getPlayerNum() == 1)
		{
			return player1BlockSuccess;
		}
		else if(event.getBlockWasEffective() && event.getPlayerNum() == 2)
		{
			return player2BlockSuccess;
		}
		
		return null;
	}
	
	public boolean isBackgroundSoundPlayer(IGameModelEvent gameModelEvent) {
		return false;
	}
	
	private PlaybackSettings getDefaultPlaybackSettings()
	{
		return new PlaybackSettings(controller.getAudioSettings().getVolume(), false, false);
	}
	
	public PlaybackSettings getPlaybackSettings(AudioSettings globalSettings, IGameModelEvent gameModelEvent) {
		assert(globalSettings != null);
		if (gameModelEvent == null || gameModelEvent.getType() != IGameModelEvent.Type.PLAYER_ATTACK_ACTION) {
			return null;
		}

		return new PlaybackSettings(globalSettings.getVolume(), false, false);
	}
}
