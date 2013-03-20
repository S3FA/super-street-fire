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
import paulscode.sound.libraries.LibraryJavaSound;

public class SoundPlayerControllerTester implements Runnable{

	static SoundPlayerController soundPlayerController;

    SoundSystem mySoundSystem;

    boolean musicPlayStop = false;
    boolean streamPlayStop = false;
    boolean JavaOpenAL = true; 

    // URLs for all the sound bytes
    private URL hadokenURL = null;
    private URL themeRyuURL = null;
    private URL beatsURL = null;
    private URL beethovenUrl = null;
    private URL bellUrl = null; 
    private URL boingUrl = null;
    private URL clickUrl = null; 
    private URL comeonUrl = null;
    private URL explosionUrl = null;
	
	/** Plays audio from given file names. */
	// A test class that tests a hadouken attack and a game state changed event which causes a looping track (which is then explicitly stopped)
	public static void main(String[] args) 
	{
		new SoundPlayerControllerTester();
	} 
	
	public SoundPlayerControllerTester()
	{
		System.out.println("Started the SoundPlayerControllerTester.");
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
        	beatsURL = new File("resources/beats.ogg").toURI().toURL();
        	themeRyuURL = new File("resources/ThemeRyu.ogg").toURI().toURL();
        	hadokenURL = new File("resources/Hadoken.ogg").toURI().toURL();
        	beethovenUrl = new File("resources/beethoven.mid").toURI().toURL();
        	bellUrl = new File("resources/bell.wav").toURI().toURL();
        	boingUrl = new File("resources/boing.wav").toURI().toURL();
        	clickUrl = new File("resources/click.wav").toURI().toURL();
        	comeonUrl = new File("resources/comeon.wav").toURI().toURL();
        	explosionUrl = new File("resources/explosion.wav").toURI().toURL();
        }
        catch (Exception ex)
        {
        	ex.printStackTrace();
        }
        
        // Set up the sound resources
        mySoundSystem.newStreamingSource( true, "ThemeRyu", themeRyuURL, "ThemeRyu.ogg", true, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0 );
        
        try
        {
	        // Test playing a track
	        mySoundSystem.quickPlay(false, boingUrl, "boing.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
	        Thread.sleep(500);
	        mySoundSystem.backgroundMusic("ThemeRyu", themeRyuURL, "ThemeRyu.ogg", true);
	        Thread.sleep(500);

	        mySoundSystem.quickPlay(false, boingUrl, "boing.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
	        Thread.sleep(500);
	        mySoundSystem.quickPlay(false, hadokenURL, "Hadoken.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
	        Thread.sleep(500);
	        mySoundSystem.quickPlay(false, boingUrl, "boing.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
	        Thread.sleep(500);
        }
        catch(Exception ex)
        {
        	ex.printStackTrace();
        }
     
        mySoundSystem.cleanup();
    }

}
