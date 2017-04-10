package cli;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import network.MessageRMI;
import resources.Logs;

/**
 * Client application for communication with the peers
 * @attribute MessageRMI stub - Represents the RMI object used for receiving and sending messages.
 * @attribute String response - Represents the peer response to messages
 */
public class TestApp 
{
	private static MessageRMI stub = null;
	private static String response = null;

	/**
	 * Client execution
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		Logs.log("CLIENT");
		
		//args verification
		if(args.length < 2)
		{
			Logs.argsClientInfo();
			wait_to_close();
			return;
		}

		//start rmi for server communication
		startRMI(args[0]);

		//protocol verification and message delivery
		if(!protocolVerAndSend(args))
		{
			Logs.argsProtocolInfo(args[1]);
			wait_to_close();
			return;
		}

		Logs.serverResponse(response);
		
		wait_to_close();
	}

	/**
	 * Connect to server with rmi service
	 * @param remoteObjName
	 */
	private static void startRMI(String remoteObjName) {
		try 
		{
			Registry registry = LocateRegistry.getRegistry(null);
			stub = (MessageRMI) registry.lookup(remoteObjName); 
		} 
		catch (Exception e) 
		{
			Logs.exception("startRMI", "TestApp", e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * Verifies args from each protocol, and execute the correct message.
	 * @param args
	 * @return
	 */
	private static boolean protocolVerAndSend(String args[]) 
	{
		boolean enhancement = false;
		if(args[1].contains("ENH"))
			enhancement = true;
		
		try {
			switch (args[1]) {
			case "BACKUP":
			case "BACKUPENH":
				if(args.length != 4) 
					return false;
				response = stub.backup(args[2],Integer.parseInt(args[3]),enhancement);
				break;
			case "RESTORE":
			case "RESTOREENH":
				if(args.length != 3)
					return false;
				response = stub.restore(args[2],enhancement);
				break;
			case "DELETE":
			case "DELETEENH":
				if(args.length != 3)
					return false;
				response = stub.delete(args[2],enhancement);
				break;
			case "RECLAIM":
			case "RECLAIMENH":
				if(args.length != 3)
					return false;
				response = stub.reclaim(Integer.parseInt(args[2]),enhancement);
				break;
			case "STATE":
				if(args.length != 2)
					return false;
				response = stub.state();
				break;
			default:
				Logs.exception("startRMI", "protocolVerAndSend", "Error sending message to peer");
				return false;
			}
		} 
		catch (NumberFormatException | RemoteException e) 
		{
			Logs.exception("startRMI", "protocolVerAndSend", e.toString());
			e.printStackTrace();
			return false;
		} 

		return true;
	}

	/**
	 * Waits for ENTER
	 */
	private static void wait_to_close() {
		Logs.enter();
		
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		scanner.close();
	}
}
