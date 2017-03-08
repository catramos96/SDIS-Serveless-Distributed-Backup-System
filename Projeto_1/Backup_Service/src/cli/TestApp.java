package cli;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class TestApp 
{
	public static DatagramSocket socket = null; 	//socket for comunication with server

	//main teste
	public static void main(String[] args) throws IOException
	{
		if(args.length != 0)
		{
			System.out.println("Usage: java TestApp");
			return;
		}
		
		InetAddress address = InetAddress.getLocalHost();
		int port = 8000;
		
		//abrir o socket de ligacao com o peer
		socket = new DatagramSocket();
		
		//trasmitir informacao
		byte[] sbuf = ("hello").getBytes();	//oper + args
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

	/*
	//main original
	public static void main(String[] args) throws IOException
	{
		if(args.length > 4)
		{
			System.out.println("Usage: java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
		}

		//FALTA : verificar se e ou nao localhost

		//peer_ap <address>:<port>
		String[] parts_ap = args[0].split(":");
		InetAddress address = InetAddress.getByName(parts_ap[0]);
		int port = Integer.parseInt(parts_ap[1]);

		//FALTA : verificacoes para o resto dos argumentos

		//abrir o socket de ligacao com o peer
		socket = new DatagramSocket(port,address);	
	}
	 */
}
