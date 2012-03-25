package ca.site3.ssf.guiprotocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ca.site3.ssf.guiprotocol.Discovery.DiscoveryRequest.DiscoveryAppType;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.UninitializedMessageException;

public class DiscoveryClient extends Thread {
	
	private final long discoveryTimeInMs;
	private final Discovery.DiscoveryRequest discoveryRequestPkg;
	private DatagramSocket socket = null;
	private volatile boolean isListening = false;
	private Timer listenTimer = null;
	
	private BlockingQueue<Discovery.DiscoveryResponse> responseQueue = new LinkedBlockingQueue<Discovery.DiscoveryResponse>();
	
	public DiscoveryClient(String clientName, Discovery.DiscoveryRequest.DiscoveryAppType type,
			               int discoveryTimeInMs) {
		
		Discovery.DiscoveryRequest.Builder discoveryReqPkgBuilder = Discovery.DiscoveryRequest.newBuilder();
		discoveryReqPkgBuilder.setAppName(clientName);
		discoveryReqPkgBuilder.setAppType(type);
		
		this.discoveryRequestPkg = discoveryReqPkgBuilder.build();
		this.discoveryTimeInMs = discoveryTimeInMs;
		
		assert(this.discoveryRequestPkg != null);
	}

	public Discovery.DiscoveryResponse getDiscoveryNoBlocking() {
		Discovery.DiscoveryResponse result = null;
		try {
			result = this.responseQueue.remove();
		}
		catch (NoSuchElementException e) {
			result = null;
		}
		
		if (result != null) {
			if (!result.isInitialized()) {
				result = null;
			}
		}
		
		return result;
	}
	
	public Discovery.DiscoveryResponse getDiscoveryBlocking() {
		if (!this.isAlive()) {
			this.start();
		}
		
		Discovery.DiscoveryResponse result = null;
		try {
			result = this.responseQueue.take();
		}
		catch (InterruptedException e) {
			result = null;
		}
		
		if (result != null) {
			if (!result.isInitialized()) {
				result = null;
			}
		}
		
		return result;
	}
	
	
	public void run() {
		
		// Setup the socket for the client
		try {
			this.socket = new DatagramSocket();
		}
		catch (SocketException e) {
			e.printStackTrace();
			return;
		}
		
		// Setup the multicast address for requesting a discovery from the server
		InetAddress multicastAddr = null;
		try {
			multicastAddr = InetAddress.getByName(DiscoveryServer.DISCOVERY_MULTICAST_IP_ADDRESS);
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		}
		
		// Request discovery...
		byte[] requestBuffer = this.discoveryRequestPkg.toByteArray();
		String bufferLengthStr = "" + requestBuffer.length;
		byte[] bufferLengthBytes = bufferLengthStr.getBytes();
		
		DatagramPacket requestPacket1 = new DatagramPacket(bufferLengthBytes, bufferLengthBytes.length, multicastAddr, DiscoveryServer.DISCOVERY_SERVER_PORT);
		DatagramPacket requestPacket2 = new DatagramPacket(requestBuffer, requestBuffer.length, multicastAddr, DiscoveryServer.DISCOVERY_SERVER_PORT);
		
		try {
			this.socket.send(requestPacket1);
			this.socket.send(requestPacket2);
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		// Now we wait for the specified discovery time for a response to arrive...
		this.isListening = true;
		
		// Setup a timer to close the socket when the listen time has expired
		this.listenTimer = new Timer();
		this.listenTimer.schedule(new CloseSocketTask(), this.discoveryTimeInMs);
		
		this.responseQueue.clear();
		boolean responseReceived = false;
		DatagramPacket responsePacket = null;
		
		byte[] receiveBuffer1 = new byte[32];
		byte[] receiveBuffer2 = null; 
		while (this.isListening) {

			try {
				responsePacket = new DatagramPacket(receiveBuffer1, receiveBuffer1.length);
				this.socket.receive(responsePacket);
				bufferLengthStr = new String(receiveBuffer1);
				bufferLengthStr = bufferLengthStr.trim();
				int buffer2Length = Integer.parseInt(bufferLengthStr);
				receiveBuffer2 = new byte[buffer2Length];
				responsePacket = new DatagramPacket(receiveBuffer2, receiveBuffer2.length);
				this.socket.receive(responsePacket);
			}
			catch (IOException e) {
				continue;
			}
			catch (NumberFormatException e) {
				continue;
			}

			// We've received a response from the discovery server, add it to a synchronized queue...
			Discovery.DiscoveryResponse discoveryResponsePkg;
			try {
				discoveryResponsePkg = Discovery.DiscoveryResponse.parseFrom(receiveBuffer2);
				if (discoveryResponsePkg == null) {
					continue;
				}
			}
			catch (InvalidProtocolBufferException e) {
				continue;
			}

			try {
				if (this.responseQueue.add(discoveryResponsePkg)) {
					responseReceived = true;
				}
				break;
			}
			catch (IllegalStateException e) {
				break;
			}

		}
		
		if (!responseReceived) {
			Discovery.DiscoveryResponse uninitPkg = null;
			try {
				uninitPkg = Discovery.DiscoveryResponse.newBuilder().buildPartial();
			}
			catch (UninitializedMessageException e) {
			}
			assert(!uninitPkg.isInitialized());
			this.responseQueue.add(uninitPkg);
		}
		
		this.socket.close();
		this.listenTimer.cancel();
	}
	
	
	class CloseSocketTask extends TimerTask {
		@Override
		public void run() {
			isListening = false;
			socket.close();
		}
	}
	
	public static void main(String[] argv) throws InterruptedException {
		
		while (true) {
			System.out.println("Attempting discovery...");
			DiscoveryClient client = new DiscoveryClient("my client", DiscoveryAppType.GUI, 5000);
			Discovery.DiscoveryResponse response = client.getDiscoveryBlocking();
			
			if (response == null) {
				System.out.println("No response received.");
			}
			else {
				System.out.println("Response received: " + response.toString());
			}
			sleep(5000);
		}
		
	}
	
}
