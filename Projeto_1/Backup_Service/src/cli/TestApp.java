package cli;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import network.MessageRMI;
import resources.Logs;

public class TestApp 
{
	private static MessageRMI stub = null;
	private static String response = null;

	public static void main(String[] args) throws IOException
	{
		if(args.length < 2)
		{
			Logs.argsClientInfo();
			wait_to_close();
			return;
		}

		startRMI(args[0]);

		if(!protocolVerAndSend(args))
		{
			Logs.argsProtocolInfo(args[1]);
			wait_to_close();
			return;
		}

		System.out.println("response: " + response);

		wait_to_close();
	}

	private static void startRMI(String remoteObjName) {
		try 
		{
			Registry registry = LocateRegistry.getRegistry(null);
			stub = (MessageRMI) registry.lookup(remoteObjName); 
		} 
		catch (Exception e) 
		{
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}
	}

	private static boolean protocolVerAndSend(String args[]) 
	{
		try {

			switch (args[1]) {
			case "BACKUP":
				if(args.length != 4) 
					return false;
				response = stub.backup(args[2],Integer.parseInt(args[3]));
				break;
			case "RESTORE":
				if(args.length != 3)
					return false;
				response = stub.restore(args[2]);
				break;
			case "DELETE":
				if(args.length != 3)
					return false;
				response = stub.delete(args[2]);
				break;
			case "RECLAIM":
				if(args.length != 3)
					return false;
				response = stub.delete(args[2]);
				break;
			case "STATE":
				if(args.length != 2)
					return false;
				response = stub.state();
				break;
			default:
				System.out.println("Error sending message to peer");
				return false;
			}
		} catch (NumberFormatException | RemoteException e) {
			e.printStackTrace();
		} 

		return true;
	}

	private static void wait_to_close() {
		System.out.println("Press ENTER to exit...");

		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		scanner.close();
	}
}
