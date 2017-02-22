import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.TimerTask;

public class ServerMulticastThread extends TimerTask{
	protected InetAddress mcast_address;
	protected int mcast_port;
	protected MulticastSocket mcast_socket;
	protected DatagramPacket packet;

	public ServerMulticastThread(InetAddress adr,int port) throws IOException {
		mcast_address = adr;
		mcast_port = port;
		mcast_socket = new MulticastSocket(mcast_port);
		mcast_socket.joinGroup(mcast_address);
	}
	
	@Override
	public void run() {
		
		while(true){
			byte[] msg = ("Message sent by multicast socket").getBytes();
			packet = new DatagramPacket(msg,msg.length,mcast_address,mcast_port);
			try {
				mcast_socket.send(packet);
				System.out.println("Sent");
				Thread.sleep(1000);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
