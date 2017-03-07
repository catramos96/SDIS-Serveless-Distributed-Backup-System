import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
		
		InetAddress address = InetAddress.getByName(args[0]);
		int port = Integer.parseInt(args[1]);
		String oper = args[2];
		String opnd = "";
		
		// send request
		Socket socket = new Socket(address,port);	//NEW
		
		
		for(int i = 3 ; i < args.length; i++){
			opnd = opnd.concat(" ").concat(args[i]);
		}
		
		String request = oper.concat(opnd);
		
		if(!((oper.compareTo("register") == 0 && args.length == 5) || (oper.compareTo("lookup") == 0 && args.length == 4))){
			System.out.println("<oper opnd> -> <register number name> or <lookup number>");
			return;
		}
		
		PrintWriter out = null;
		out = new PrintWriter(socket.getOutputStream(),true);
		out.println(request);
		
		// get response
		BufferedReader in = null;
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String received = in.readLine();
		System.out.println("Server Response : " + received);
		
		socket.close();
	}

}
