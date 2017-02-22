import java.io.IOException;
import java.net.InetAddress;

public class Server
{
	public static void main(String[] args) throws IOException
	{
		if (args.length != 3)
		{
			System.out.println("Usage: java Server <srvc_port> <mcast_addr> <mcast_port> ");
			return;
		}
		
		int port = Integer.parseInt(args[0]);
		InetAddress mcast_address = InetAddress.getByName(args[1]);
		int mcast_port = Integer.parseInt(args[2]);
		
		new ServerThread(port).start();
		new ServerMulticastThread(mcast_address,mcast_port).run();
	}
}