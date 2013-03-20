package ca.site3.ssf.Sound;

import com.threed.jpct.util.KeyMapper;
import com.threed.jpct.util.KeyState;

/**  IMPORT THIS FOR AN APPLET: **/
/**/
//import javax.swing.JApplet;
/**/
/*********************************/

/**  IMPORT THESE FOR AN APPLICATION: **/

 import java.awt.Insets;
 import javax.swing.JFrame;

/***************************************/

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemException;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.codecs.CodecJOgg;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;
import paulscode.sound.libraries.LibraryJavaSound;

/**
 * The SoundSystemPlayerApplet class demonstrates playing MIDI, streaming .ogg,
 * and using the quickPlay() method from the SoundSystem core library.
 *
 *<br><br><br>
 *<b><i>    SoundSystemPlayerApplet License:</b></i><br><b><br>
 *    You are free to use this class for any purpose, commercial or otherwise.
 *    You may modify this class or source code, and distribute it any way you
 *    like, provided the following conditions are met:
 *<br>
 *    1) You may not falsely claim to be the author of this class or any
 *    unmodified portion of it.
 *<br>
 *    2) You may not copyright this class or a modified version of it and then
 *    sue me for copyright infringement.
 *<br>
 *    3) If you modify the source code, you must clearly document the changes
 *    made before redistributing the modified source code, so other users know
 *    it is not the original code.
 *<br>
 *    4) You are not required to give me credit for this class in any derived
 *    work, but if you do, you must also mention my website:
 *    http://www.paulscode.com
 *<br>
 *    5) I the author will not be responsible for any damages (physical,
 *    financial, or otherwise) caused by the use if this class or any portion
 *    of it.
 *<br>
 *    6) I the author do not guarantee, warrant, or make any representations,
 *    either expressed or implied, regarding the use of this class or any
 *    portion of it.
 * <br><br>
 *    Author: Paul Lamb
 * <br>
 *    http://www.paulscode.com
 * </b>
 */
public class SoundSystemPlayerApplet extends /*JApplet*/ JFrame implements Runnable
{
    boolean running = true;
    SoundSystem mySoundSystem;
    
    private int width = 640;
    private int height = 480;

    KeyMapper keyMapper;
    
    boolean musicPlayStop = false;
    boolean streamPlayStop = false;
    boolean JavaOpenAL = true;
//    boolean JavaOpenAL = false;
    
    BufferedImage frameBuffer = null;

    // top and left insets (both zero for an applet)
    private int titleBarHeight = 0;
    private int leftBorderWidth = 0;
    
    // URLs for all the sound bytes
    private URL beatsUrl = null; 
    private URL beethovenUrl = null;
    private URL bellUrl = null; 
    private URL boingUrl = null;
    private URL clickUrl = null; 
    private URL comeonUrl = null;
    private URL explosionUrl = null;
    
/**  USE THIS FOR AN APPLICATION  **/

    public static void main( String[] args )
    {
        new SoundSystemPlayerApplet();
    }
    public SoundSystemPlayerApplet()
    { 		
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        setTitle( "SoundSystem Player Demo" );
        pack();
        Insets insets = getInsets();
        titleBarHeight = insets.top - 1;
        leftBorderWidth = insets.left - 1;
        setSize( width + leftBorderWidth + insets.right - 1,
                 height + titleBarHeight + insets.bottom - 1 );
        setResizable( false );
        setLocationRelativeTo( null );
        setVisible( true );

        init();
    }

/***********************************/


/**  USE @Override FOR AN APPLET  **/
/**/
 //   @Override
/**/
/***********************************/
    public void init()
    {
        // get keystrokes picked up by the canvas:
        keyMapper = new KeyMapper( this );
        
        frameBuffer = new BufferedImage( width, height,
                                         BufferedImage.TYPE_4BYTE_ABGR );

        drawFrame();

        this.requestFocus();
        this.requestFocusInWindow();
        
        // start the main loop
        new Thread(this).start();
    }

    // Demonstrate OpenAL 3D panning with source at various distances:
    @Override
    public void run()
    {
        // Load some library and codec pluggins:
        try
        {
            SoundSystemConfig.addLibrary( LibraryLWJGLOpenAL.class );
            SoundSystemConfig.addLibrary( LibraryJavaSound.class );
            SoundSystemConfig.setCodec( "wav", CodecWav.class );
            SoundSystemConfig.setCodec( "ogg", CodecJOgg.class );
        }
        catch( SoundSystemException e )
        {
            System.out.println("error linking with the pluggins" );
        }

        // Instantiate the SoundSystem:
        try
        {
//            mySoundSystem = new SoundSystem( LibraryJavaSound.class );
            mySoundSystem = new SoundSystem( LibraryLWJGLOpenAL.class );
        }
        catch( SoundSystemException e )
        {
//            System.out.println( "JavaSound library is not compatible on " +
//                                "this computer" );
            System.out.println( "LWJGL OpenAL library is not compatible on " +
                                "this computer" );
            e.printStackTrace();
            return;
        }
        
        
        
        try
        {
        	beatsUrl = new File("resources/beats.ogg").toURI().toURL();
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
        
        mySoundSystem.newStreamingSource( true, "OGG Music", boingUrl, "boing.wav",
                true, 0, 0, 0,
                 SoundSystemConfig.ATTENUATION_NONE,
                 0 );
        
        mySoundSystem.newStreamingSource( true, "MIDI Music", beethovenUrl, "beethoven.mid",
                true, 0, 0, 0,
                 SoundSystemConfig.ATTENUATION_NONE,
                 0 );

        while( running )
        {
            pollKeyboard();  // check for keyboard input
            
            this.repaint();
            try
            {
                Thread.sleep( 20 );
            }
            catch(Exception e){}
        }
        mySoundSystem.cleanup();
    }
    
    @Override
    public void paint( Graphics g )
    {
        if( frameBuffer == null )
            return;
        
        g.drawImage( frameBuffer, leftBorderWidth, titleBarHeight, null );
    }
    
/**  USE @Override FOR AN APPLET  **/
/**/
 //   @Override
/**/
/***********************************/
    public void destroy()
    {
        // end the main thread:
    	running = false;
    }
    
    public void drawFrame()
    {
        Graphics gB = frameBuffer.getGraphics();
        
        gB.setColor( Color.white );
        gB.fillRect( 0, 0, width, height );
        gB.setColor( Color.black );
        
        if( JavaOpenAL )
            gB.drawString( "<S> Switches to JavaSound", 5, 25 );
        else
            gB.drawString( "<S> Switches to OpenAL", 5, 25 );
        
        if( musicPlayStop )
            gB.drawString( "<Enter> Stops beethoven.mid", 5, 125 );
        else
            gB.drawString( "<Enter> Plays beethoven.mid", 5, 125 );
        if( streamPlayStop )
            gB.drawString( "<Spacebar> Stops beats.ogg stream", 5, 175 );
        else
            gB.drawString( "<Spacebar> Streams beats.ogg", 5, 175 );
        
        gB.drawString( "<1> Quick-Plays boing.wav", 5, 275 );
        gB.drawString( "<2> Quick-Plays click.wav", 5, 325 );
        gB.drawString( "<3> Quick-Plays comeon.wav", 5, 375 );
        gB.drawString( "<4> Quick-Plays explosion.wav", 5, 425 );
        gB.drawString( "<5> Quick-Plays bell.wav", 5, 475 );
        
        gB.drawString( "<ESC> Shuts down", 5, 525 );
    }
    
    // Use the KeyMapper to poll the keyboard
    private void pollKeyboard()
    {
        KeyState state = null;
        do
        {
            state = keyMapper.poll();
            if( state != KeyState.NONE )
            {
                keyAffected( state );
            }
        } while( state != KeyState.NONE );
    }

    private void keyAffected( KeyState state )
    {
        int code = state.getKeyCode();
        boolean event = state.getState();

        switch( code )
        {
            case( KeyEvent.VK_S ):
            {
                if( event )
                {
                    try
                    {
                        if( JavaOpenAL )
                            mySoundSystem.switchLibrary(
                                                       LibraryJavaSound.class );
                        else
                            mySoundSystem.switchLibrary(
                                                     LibraryLWJGLOpenAL.class );
                        musicPlayStop = false;
                        streamPlayStop = false;
                        JavaOpenAL = !JavaOpenAL;
                    }
                    catch( SoundSystemException e )
                    {
                        if( JavaOpenAL )
                            System.out.println( "JavaSound library not " +
                                                "compatible on this computer" );
                        else
                            System.out.println( "OpenAL library not " +
                                                "compatible on this computer" );
                        e.printStackTrace();
                    }
                    drawFrame();
                }
                break;
            }
            case( KeyEvent.VK_ESCAPE ):
            {
                running = event;
                break;
            }
            case( KeyEvent.VK_ENTER ):
            {
                if( event )
                {
                    if( musicPlayStop )
                        mySoundSystem.stop( "MIDI Music" );
                    else
                        mySoundSystem.play( "MIDI Music" );
                    
                    mySoundSystem.setVolume( "MIDI Music", 0.5f );
                    musicPlayStop = !musicPlayStop;
                }
                break;
            }
            case( KeyEvent.VK_SPACE ):
            {
                if( event )
                {
                    if( streamPlayStop )
                    {
                        mySoundSystem.stop( "OGG Music" );
                    }
                    else
                    {
                        mySoundSystem.setLooping( "OGG Music", true );
                        mySoundSystem.play("OGG Music");
                    }
                    
                    streamPlayStop = !streamPlayStop;
                }
                break;
            }            
            case( KeyEvent.VK_1 ):
            {
                if( event )
                {
                    mySoundSystem.quickPlay(false, boingUrl, "boing.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
                }
                break;
            }            
            case( KeyEvent.VK_2 ):
            {
                if( event )
                {
                    mySoundSystem.quickPlay( false, "click.wav", false, 0, 0, 0,
                                        SoundSystemConfig.ATTENUATION_ROLLOFF,
                                        SoundSystemConfig.getDefaultRolloff() );
                }
                break;
            }            
            case( KeyEvent.VK_3 ):
            {
                if( event )
                {
                    mySoundSystem.quickPlay( false, "comeon.wav", false,
                                        0, 0, 0,
                                        SoundSystemConfig.ATTENUATION_ROLLOFF,
                                        SoundSystemConfig.getDefaultRolloff() );
                }
                break;
            }            
            case( KeyEvent.VK_4 ):
            {
                if( event )
                {
                    mySoundSystem.quickPlay( false, "explosion.wav", false,
                                        0, 0, 0,
                                        SoundSystemConfig.ATTENUATION_ROLLOFF,
                                        SoundSystemConfig.getDefaultRolloff() );
                }
                break;
            }            
            case( KeyEvent.VK_5 ):
            {
                if( event )
                {
                    mySoundSystem.quickPlay( false, "bell.wav", false,
                                        0, 0, 0,
                                        SoundSystemConfig.ATTENUATION_ROLLOFF,
                                        SoundSystemConfig.getDefaultRolloff() );
                }
                break;
            }
        }
    }    
}
