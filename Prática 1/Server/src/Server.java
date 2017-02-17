import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Server {
    public static void main(String[] args) throws IOException {
    	
    	if (args.length != 1) {
    		System.out.println("Usage: java Server <port_number>");
    		return;
    	}
    int port = Integer.parseInt(args[0]);
    DatagramSocket socket = new DatagramSocket(port);
   /* FileReader file = new FileReader("database.txt");*/
    
    System.out.println("Waiting Request ... ");
    
    while(true){
    	// get response
		byte[] rbuf = new byte[256];
		DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
		socket.receive(packet);
		
		// display response
		String received = new String(packet.getData());
		System.out.println("Request Received: " + received);
		
		// process response
		String response;
		int first_arg_index, secnd_arg_index;
		int number;
		String  name;
		
		if(received.startsWith("register")){
			response = "register";
			first_arg_index = 9;
			secnd_arg_index = received.lastIndexOf(" ") + 1;
			number = Integer.parseInt(received.substring(first_arg_index, secnd_arg_index - 1));
			name = received.substring(secnd_arg_index);
			
			//DEBUG
			System.out.println("Number plate: " + number);
			System.out.println("Name : " + name);
			
			/*
			 * Ir verificar se existe na base de dados e devolver na response
			 */
		}
		else if(received.startsWith("lookup")){
			response = "lookup";
			first_arg_index = 7;
			secnd_arg_index = received.lastIndexOf(" ") + 1;
			number = Integer.parseInt(received.substring(first_arg_index));
			
			//DEBUG
			System.out.println("Number plate" + number);
			
			/*
			 * Ir buscar a base de dados e por em response
			 */
		}
		else
			response = "ERROR";
		
		packet = new DatagramPacket((response).getBytes(), (response).length(),packet.getAddress(),packet.getPort());
		socket.send(packet);
		
    }
    
    }
}