package cli;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class TestApp 
{
	private static DatagramSocket socket = null; 	//socket for comunication with server
	private static InetAddress address = null;
	private static int port = 0;

	public static void main(String[] args) throws IOException
	{
		int argsLength = args.length;
		
		if(argsLength < 3)
		{
			System.out.println("Usage: java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>*");
			return;
		}
		
		if(!addressVerification(args[0]))
			return;
		
		if(!protocolVerification(args[1], argsLength))
			return;
		
		String message = args[1];
		for (int i = 2; i < argsLength; i++) {
			message += " " + args[i];
		}
		
		//abrir o socket de ligacao com o peer
		socket = new DatagramSocket();
	
		//trasmitir informacao
		byte[] sbuf = (message).getBytes();	//oper + args
		DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length,address,port);
		socket.send(packet);
		
		//receber informacao
		byte[] rbuf = new byte[256];
		packet = new DatagramPacket(rbuf, rbuf.length);
		socket.receive(packet);
		
		System.out.println(new String(packet.getData()));
		
		//fechar socket
		socket.close();
		
	}

	private static boolean protocolVerification(String arg, int length) {
		
		if(arg.equals("BACKUP"))
		{
			if(length != 4)
			{
				System.out.println("Usage: java TestApp <peer_ap> BACKUP <file> <replicationDegree>");
				return false;
			}
		}
		else if(arg.equals("RESTORE") || arg.equals("DELETE") || arg.equals("RECLAIM"))
		{
			if(length != 3)
			{
				System.out.println("Usage: java TestApp <peer_ap> "+ arg +" <file>");
				return false;
			}
		}
		else if(arg.equals("STATE"))
		{
			if(length != 2)
			{
				System.out.println("Usage: java TestApp <peer_ap> STATE");
				return false;
			}
		}
		else 
			return false;
		
		return true;		
	}

	private static boolean addressVerification(String arg) {
		String[] parts_ap = arg.split(":");
		//localhost
		if(parts_ap.length == 1)
		{
			try
			{
				address = InetAddress.getLocalHost();
			} 
			catch (UnknownHostException e) 
			{
				e.printStackTrace();
				return false;
			}
			port = Integer.parseInt(parts_ap[0]);
		}
		else
		{
			try 
			{
				address = InetAddress.getByName(parts_ap[0]);
			} 
			catch (UnknownHostException e) 
			{
				e.printStackTrace();
				return false;
			}
			port = Integer.parseInt(parts_ap[1]);
		}
		
		return true;
	}
}
