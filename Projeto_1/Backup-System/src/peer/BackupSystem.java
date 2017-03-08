package peer;

import java.io.IOException;

public class BackupSystem 
{
	private static Peer peer = null;
	public static int protocol_version = 0;
	
	//main de teste
	public static void main(String[] args) throws IOException
	{
		if(args.length != 1)
		{
			System.out.println("Usage: java BackupSystem <peer_id>");
		}
	
		int peer_id = Integer.parseInt(args[0]);
		
		String[] parts_ap = {"","8000"};
		String[] parts_mc = {"224.0.0.3","4446"};
		
		peer = new Peer(peer_id,parts_ap,parts_mc,null,null);
	}
	
	/*
	//main original
	public static void main(String[] args) throws IOException
	{
		if(args.length != 6)
		{
			System.out.println("Usage: java BackupSystem <version> <peer_id> <peer_ap> <MC> <MDB> <MDR>");
		}
		
		protocol_version = Integer.parseInt(args[0]);
		int peer_id = Integer.parseInt(args[1]);
		
		//FALTA : verificar se e ou nao localhost
		
		//peer_ap <address>:<port>
		String[] parts_ap = args[2].split(":");
		String[] parts_mc = args[3].split(":");
		String[] parts_mdb = args[4].split(":");
		String[] parts_mdr = args[5].split(":");
		
		peer = new Peer(peer_id,parts_ap,parts_mc,parts_mdb,parts_mdr);
	}
	*/
}
