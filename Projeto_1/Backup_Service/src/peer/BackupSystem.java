package peer;

import java.io.IOException;

public class BackupSystem 
{
	/* Action
	 * 1 - Backup
	 * 2 - Restore
	 * 3 - Delete
	 * 4 - Space Reclaiming
	 */
	public static int protocol_version = 0;
	
	//main de teste
	public static void main(String[] args) throws IOException
	{
		if(args.length != 1)
		{
			System.out.println("Usage: java BackupSystem <peer_id>"); //action tmp
			return;
		}
	
		int peer_id = Integer.parseInt(args[0]);
		
		String[] parts_ap = {"","8000"};
		String[] parts_mc = {"224.0.0.3","4446"};
		String[] parts_mdb = {"224.0.0.2","4446"};
		
		new Peer(peer_id,parts_ap,parts_mc,parts_mdb,null);		
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
