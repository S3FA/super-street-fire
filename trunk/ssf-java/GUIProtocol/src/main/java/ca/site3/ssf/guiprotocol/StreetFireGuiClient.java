package ca.site3.ssf.guiprotocol;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.site3.ssf.gamemodel.ActionFactory;
import ca.site3.ssf.gamemodel.ExecutePlayerActionCommand;
import ca.site3.ssf.gamemodel.FireEmitter.Location;
import ca.site3.ssf.gamemodel.IGameModel.Entity;
import ca.site3.ssf.gamemodel.InitiateNextStateCommand;
import ca.site3.ssf.gamemodel.KillGameCommand;
import ca.site3.ssf.gamemodel.TogglePauseGameCommand;
import ca.site3.ssf.gamemodel.TouchFireEmitterCommand;
import ca.site3.ssf.guiprotocol.GuiCommand.Command;
import ca.site3.ssf.guiprotocol.GuiCommand.Command.Builder;
import ca.site3.ssf.guiprotocol.GuiCommand.Command.CommandType;

/**
 * Class for connecting to and communicating with a {@link StreetFireServer} 
 * over the network.
 * 
 * @author greg
 */
public class StreetFireGuiClient {

	private Logger log = LoggerFactory.getLogger(getClass());
	
	private int port;
	private InetAddress serverAddress;
	
	private Socket socket;
	
	
	public StreetFireGuiClient(InetAddress ioserver, int port) {
		serverAddress = ioserver;
		this.port = port;
	}
	
	
	/**
	 * Connect to the IO Server. This must be called before any 
	 * attempts are made at communication. 
	 * 
	 * @return true if the connection was successful, false otherwise
	 * @throws IOException
	 */
	public boolean connect() throws IOException {
		socket = new Socket(serverAddress, port);
		return socket.isConnected();
	}
	
	public boolean isConnected() {
		if (socket == null) {
			return false;
		}
		return socket.isConnected();
	}
	
	
	
	
	/**
	 * @see KillGameCommand
	 * @throws IOException
	 */
	public void killGame() throws IOException {
		Builder b = Command.newBuilder().setType(CommandType.KILL_GAME);
		submitCommand(b.build());
	}
	
	/**
	 * @see InitiateNextStateCommand
	 * @throws IOException
	 */
	public void initiateNextState() throws IOException {
		Builder b = Command.newBuilder().setType(CommandType.NEXT_STATE);
		submitCommand(b.build());
	}
	
	/**
	 * @see TogglePauseGameCommand
	 * @throws IOException
	 */
	public void togglePauseGame() throws IOException {
		Builder b = Command.newBuilder().setType(CommandType.TOGGLE_PAUSE);
		submitCommand(b.build());
	}

	/**
	 * @see ExecutePlayerActionCommand
	 * @throws IOException
	 */
	public void executePlayerAction(int playerNum, 
									ActionFactory.PlayerActionType playerActionType, 
									boolean usesLeftHand,
									boolean usesRightHand) throws IOException {
		
		Builder b = Command.newBuilder().setType(CommandType.EXECUTE_PLAYER_ACTION)
					.setPlayer(SerializationHelper.playerFromNum(playerNum))
					.setPlayerAction(SerializationHelper.actionToProtobuf(playerActionType))
					.setLeftHand(usesLeftHand)
					.setRightHand(usesRightHand);
		submitCommand(b.build());
	}
	
	
	/**
	 * @see TouchFireEmitterCommand
	 * @throws IOException
	 */
	public void activateEmitter(Location location, 
								int index, 
								float intensity, 
								EnumSet<Entity> contributors) throws IOException {
		
		Builder b = Command.newBuilder().setType(CommandType.TOUCH_EMITTER)
				.setEmitterType(SerializationHelper.locationToProtobuf(location))
				.setEmitterIndex(index)
				.setIntensity(intensity)
				.addAllEmitterEntities(SerializationHelper.entitiesToProtobuf(contributors));
		submitCommand(b.build());
	}
	
	
	
	/**
	 * Sends the given command to the server.
	 * 
	 * @param cmd
	 * @throws IOException
	 */
	private void submitCommand(Command cmd) throws IOException {
		if (isConnected() == false) {
			throw new IllegalStateException("Not connected to server");
		}
		
		cmd.writeDelimitedTo(socket.getOutputStream());
	}
	
}
