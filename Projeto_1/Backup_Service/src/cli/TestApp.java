package cli;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import resources.Logs;

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
			Logs.argsClientInfo();
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
		
		System.out.println("abriu o socket");
	
		//trasmitir informacao
		byte[] sbuf = (message+'\n').getBytes();	//oper + args
		DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length,address,port);
		socket.send(packet);
		
		System.out.println("enviou mensagem "+message);
		
		//receber informacao
		byte[] rbuf = new byte[256];
		packet = new DatagramPacket(rbuf, rbuf.length);
		socket.receive(packet);
		
		System.out.println(new String(packet.getData()));
		
		//fechar socket
		socket.close();
		
	}

	private static boolean protocolVerification(String arg, int length) {		
		boolean fail = false;
		
		if(arg.equals("BACKUP")) 
	    { 
	      if(length != 4) fail = true;
	    } 
	    else if(arg.equals("RESTORE") || arg.equals("DELETE") || arg.equals("RECLAIM")) 
	    { 
	      if(length != 3) fail = true;
	    } 
	    else if(arg.equals("STATE")) 
	    { 
	      if(length != 2) fail = true;
	    } 
	    else	fail = true;

		if(fail){
			Logs.argsProtocolInfo(arg);
			return false;
		}
		
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
