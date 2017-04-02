package resources;

import network.Message;

public class Logs {
	
/*Arguments*/
	
	public static void argsBackupSystemInfo(){
		//System.out.println("Usage: java BackupSystem <version> <peer_id> <peer_ap> <MC> <MDB> <MDR> ");
	}
	
	public static void argsClientInfo(){
		//System.out.println("Usage: java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>*");
	}
	
	public static void argsProtocolInfo(String protocol){
		String tmp = new String("Usage: java TestApp <peer_ap> ");
		String args = new String();
		
		switch(protocol){
			case "BACKUP":{
				args.concat(" <file> <replicationDegree>");
				break;
			}
			case "RESTORE":
			case "DELETE" :
			case "RECLAIM" :{
				args.concat("  <file>");
				break;
			}
			case "STATE":{
				break;
			}
		}
		
		//System.out.println(tmp + protocol + args);

	}
	
	/*Messages*/
	
	public static void sentMessageLog(Message msg){
		//System.out.println("(Sent) Type : "+ msg.getType() + " from sender : "+ msg.getSenderId() + " with chunk "+ msg.getChunkNo());
	}
	
	public static void receivedMessageLog(Message msg){
		//System.out.println("(Received) Type : "+ msg.getType() + " from sender : "+ msg.getSenderId() + " with chunk "+ msg.getChunkNo());
	}
	
	/*Files*/
	
	public static void errorFileId(String filename){
		//System.out.println("Error searching for fileId of "+filename);
	}
	
	public static void errorRestoringFile(String filename){
		//System.out.print("Erro restoring file " +  filename);
	}
	
	public static void errorOpeningFile(String filename){
		//System.out.println("Error opening "+filename+" file.");
	}
	
	public static void errorFindingFile(String filename){
		//System.out.println("Error "+filename+" not founf.");
	}
	
	public static void fileRestored(String filename){
		//System.out.println("File "+filename+" restored");
	}
	
	public static void chunkRestored(int chunkNo){
		//System.out.println("Chunk Number "+chunkNo+" restored");
	}
	
	/*ChunkBackupProtocol*/
	public static void tryNrStoreChunk(int nr,int chunkNo){
		//System.out.println("Try number "+ nr + " to stored chunk number "+chunkNo);
	}
	
	public static void allChunksNrStored(int chunkNo){
		//System.out.println("All Chunks with number "+ chunkNo+ " Stored");	
	}
	
	public static void chunkRepDegNotAccepted(int chunkNo, int stored){
		//System.out.println(stored + " Replication Degree not pleased for chunk number " + chunkNo);
	}
	
}
