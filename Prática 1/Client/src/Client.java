import java.io.IOException;
import java.net.*;

public class Client {
	
	public static void main(String[] args) throws IOException{
		
		/*
		 * Hostname example for running client-server on same machine -> 127.0.0.1 
		 */
		
		if (args.length < 3) {
			System.out.println("java Client <host_name> <port_number> <oper> <opnd>*");
			return;
		}
		
		// send request
		DatagramSocket socket = new DatagramSocket();
		InetAddress address = InetAddress.getByName(args[0]);
		int port = Integer.parseInt(args[1]);
		String oper = args[2];
		String opnd = "";
		
		for(int i = 3 ; i < args.length; i++){
			opnd = opnd.concat(" ").concat(args[i]);
		}
		
		byte[] buffer = (oper.concat(opnd)).getBytes();	//oper + args
		
		if(!((oper.compareTo("register") == 0 && args.length == 5) || (oper.compareTo("lookup") == 0 && args.length == 4))){
			System.out.println("<oper opnd> -> <register number name> or <lookup number>");
			return;
		}
		
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length,address, port);
		socket.send(packet);
		
		// get response
		byte[] rbuf = new byte[buffer.length];
		packet = new DatagramPacket(rbuf, rbuf.length);
		socket.receive(packet);
		
		// display response
		String received = new String(packet.getData());
		System.out.println("Server Response: " + received);
		
		socket.close();
	}

}
