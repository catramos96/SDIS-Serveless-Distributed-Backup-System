package peer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import resources.Logs;

public class BackupSystem 
{
	public static void main(String[] args) throws IOException
	{		
		if(args.length != 6)
		{
			Logs.argsBackupSystemInfo();
			return;
		}

		char[] protocol_version = args[0].toCharArray();

		int peer_id = Integer.parseInt(args[1]);

		String remoteObjName = args[2];
		//peer_ap <address>:<port>
		String[] parts_mc = addressVerification(args[3]);
		String[] parts_mdb = addressVerification(args[4]);
		String[] parts_mdr = addressVerification(args[5]);

		new Peer(protocol_version,peer_id,remoteObjName,parts_mc,parts_mdb,parts_mdr);	
	}

	private static String[] addressVerification(String arg) 
	{
		String[] parts_ap = arg.split(":");
		String[] parts = new String[2];

		//localhost
		if(parts_ap.length == 1)
		{
			try{
				parts[0] = InetAddress.getLocalHost().getHostName();
			} 
			catch (UnknownHostException e) {
				e.printStackTrace();
			}
			parts[1] = parts_ap[0];
		}
		else
			parts = parts_ap;

		return parts;
	}


}
