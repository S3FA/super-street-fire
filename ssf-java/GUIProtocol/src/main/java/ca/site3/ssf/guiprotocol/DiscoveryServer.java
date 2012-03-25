package ca.site3.ssf.guiprotocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * The DiscoverServer acts as a constantly listening/waiting to accept entity that will
 * respond to discovery broadcast requests from clients and then send information about
 * how to connect to the server (IP address and port) for guiprotocol communications.
 * 
 * This class (combined with the DiscoveryClient) provides an automatic connection method
 * (so that we don't have to always figure out IP and port number information each time
 * we want a client to connect).
 * 
 * @author Callum
 *
 */
public class DiscoveryServer extends Thread {

	public final static String DISCOVERY_MULTICAST_IP_ADDRESS = "230.42.0.10";
	public final static int DISCOVERY_SERVER_PORT = 42010;
	
	private MulticastSocket socket = null;
	private Discovery.DiscoveryResponse discoveryResponsePkg = null;
	private volatile boolean stopped = false;
	
	public DiscoveryServer(String guiProtocolIpAddr, int guiProtocolPort) {
		Discovery.DiscoveryResponse.Builder responseBuilder = Discovery.DiscoveryResponse.newBuilder();
		responseBuilder.setServerIPAddress(guiProtocolIpAddr);
		responseBuilder.setServerPortNumber(guiProtocolPort);
		
		this.discoveryResponsePkg = responseBuilder.build();
		assert(this.discoveryResponsePkg != null);
		assert(this.discoveryResponsePkg.isInitialized());
	}

	public void run() {
		// Create a new multicast socket for receiving requests from discovery clients
		try {
			this.socket = new MulticastSocket(DiscoveryServer.DISCOVERY_SERVER_PORT);
		}
		catch (IOException e) {
			e.printStackTrace();
			this.stopped = true;
			return;
		}
		
		// Join the multicast group
		InetAddress multicastAddr = null;
		try {
			multicastAddr = InetAddress.getByName(DiscoveryServer.DISCOVERY_MULTICAST_IP_ADDRESS);
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
			this.stopped = true;
			return;
		}
		try {
			this.socket.joinGroup(multicastAddr);
		}
		catch (IOException e) {
			e.printStackTrace();
			this.stopped = true;
			return;
		}
		
		byte[] receiveBuffer1 = new byte[32];
		byte[] receiveBuffer2 = null;
		
		while (!this.stopped) {
		
			// Block and wait for a discovery request package to be received by this server...
			DatagramPacket requestPacket = new DatagramPacket(receiveBuffer1, receiveBuffer1.length);
			try {
				
				this.socket.receive(requestPacket);
				
				String bufferLengthStr = new String(receiveBuffer1);
				bufferLengthStr = bufferLengthStr.trim();
				int buffer2Length = Integer.parseInt(bufferLengthStr);
				receiveBuffer2 = new byte[buffer2Length];
				requestPacket = new DatagramPacket(receiveBuffer2, receiveBuffer2.length);
				
				this.socket.receive(requestPacket);
		
			}
			catch (IOException e) {
				e.printStackTrace();
				this.stopped = true;
				break;
			}
			
			// Parse the discovery request package...
			Discovery.DiscoveryRequest discoveryRequestPkg = null;
			try {
				discoveryRequestPkg = Discovery.DiscoveryRequest.parseFrom(receiveBuffer2);
				System.out.println("Discovery Request from: " + discoveryRequestPkg.toString());
			}
			catch (InvalidProtocolBufferException e) {
				System.err.println("Invalid discovery request datagram received: " + e.getMessage());
				continue;
			}
			assert(discoveryRequestPkg != null);
			
			InetAddress requesterAddr = requestPacket.getAddress();
			int requesterPort = requestPacket.getPort();
			
			byte[] sendBuffer = this.discoveryResponsePkg.toByteArray();
			String bufferLengthStr = "" + sendBuffer.length;
			byte[] bufferLengthBytes = bufferLengthStr.getBytes();
			DatagramPacket responsePacket1 = new DatagramPacket(bufferLengthBytes, bufferLengthBytes.length, requesterAddr, requesterPort);
			DatagramPacket responsePacket2 = new DatagramPacket(sendBuffer, sendBuffer.length, requesterAddr, requesterPort);
			
			try {
				this.socket.send(responsePacket1);
				this.socket.send(responsePacket2);
			}
			catch (IOException e) {
				e.printStackTrace();
				this.stopped = true;
				break;
			}
		}
		
		this.socket.close();
	}
	
	public void stopServer() {
		this.stopped = true;
		this.socket.close();
	}

	public static void main(String[] argv) {
		InetAddress address = null;
		try {
			address = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		System.out.println("Running discovery server...");
		DiscoveryServer discoveryServer = new DiscoveryServer(address.getHostAddress(), 45000);
		discoveryServer.start();
	}
	
}
