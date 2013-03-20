package ca.site3.ssf.Sound;

import ca.site3.ssf.gamemodel.PlayerAttackAction;
import ca.site3.ssf.gamemodel.PlayerAttackActionEvent;

import java.io.File;
import java.net.URL;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemException;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

public class SoundPlayerControllerTester implements Runnable{

	static SoundPlayerController soundPlayerController;

    SoundSystem mySoundSystem;

    boolean musicPlayStop = false;
    boolean streamPlayStop = false;
    boolean JavaOpenAL = true; 

    // URLs for all the sound bytes
    private URL hadokenURL = null;
    private URL themeRyuURL = null;
    private URL boingUrl = null;
	
	/** Plays audio from given file names. */
	// A test class that tests a hadouken attack and a game state changed event which causes a looping track (which is then explicitly stopped)
	public static void main(String[] args) 
	{
		new SoundPlayerControllerTester();
	} 
	
	public SoundPlayerControllerTester()
	{
		System.out.println("Started the SoundPlayerControllerTester.");
		
		// Create some sample actions
		PlayerAttackActionEvent player1AttackActionEvent = new PlayerAttackActionEvent(1, PlayerAttackAction.AttackType.HADOUKEN_ATTACK);
		PlayerAttackActionEvent player2AttackActionEvent = new PlayerAttackActionEvent(2, PlayerAttackAction.AttackType.HADOUKEN_ATTACK);
		
		init();
	}
	
	public void init()
	{
		new Thread(this).start();
	}
	
	// Demonstrate OpenAL 3D panning with source at various distances:
    @Override
    public void run()
    {
        // Load some library and codec plugins:
        try
        {
            SoundSystemConfig.addLibrary( LibraryLWJGLOpenAL.class );
            SoundSystemConfig.setCodec( "ogg", CodecJOrbis.class );
            SoundSystemConfig.setCodec( "wav", CodecWav.class );
        }
        catch( SoundSystemException e )
        {
            System.out.println("error linking with the plugins" );
        }

        // Instantiate the SoundSystem:
        try
        {
            mySoundSystem = new SoundSystem( LibraryLWJGLOpenAL.class );
        }
        catch( SoundSystemException e )
        {
        	System.out.println( "LWJGL OpenAL library is not compatible on this computer" );
            e.printStackTrace();
            return;
        }
           
        // Set up the sound locations 
        try
        {
        	themeRyuURL = new File("resources/ThemeRyu.ogg").toURI().toURL();
        	hadokenURL = new File("resources/Hadoken.ogg").toURI().toURL();
        	boingUrl = new File("resources/boing.wav").toURI().toURL();
        }
        catch (Exception ex)
        {
        	ex.printStackTrace();
        }
        
        // Set up the sounds as streaming sources. You can set priority and looping here, and this allows us to refer to them easily late
        mySoundSystem.newStreamingSource( true, "ThemeRyu", themeRyuURL, "ThemeRyu.ogg", true, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0 );
        mySoundSystem.newStreamingSource( true, "Hadoken", hadokenURL, "Hadoken.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_ROLLOFF, 0 );
        
        try
        {
	        // Test playing a track
	        mySoundSystem.quickPlay(false, boingUrl, "boing.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
	        Thread.sleep(500);
	        
            // Play a looping track. You can also use .backgroundMusic to set a looping high priority track
            mySoundSystem.play("ThemeRyu");
	        //mySoundSystem.backgroundMusic("ThemeRyu", themeRyuURL, "ThemeRyu.ogg", true);
	        Thread.sleep(500);

	        mySoundSystem.quickPlay(false, boingUrl, "boing.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
	        Thread.sleep(500);
	        
	        // You can also set up references to sounds in advance as above and play/loop/stop them anytime
	        mySoundSystem.play("Hadoken");
	        Thread.sleep(500);
	        
	        // You can also use quickplay to play a sound without having previously defined it as a streaming source
	        mySoundSystem.quickPlay(false, boingUrl, "boing.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
	        Thread.sleep(500);
        }
        catch(Exception ex)
        {
        	ex.printStackTrace();
        }
     
        // Always call this after we're done
        mySoundSystem.cleanup();
    }

}
