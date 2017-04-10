package resources;

import network.Message;

public class Logs {
	
	public static void log(String string) {
		System.out.println(string);
	}
	
	/*
	 * Arguments
	 */
	
	public static void argsBackupSystemInfo(){
		System.out.println("Usage: java BackupSystem <version> <peer_id> <peer_ap> <MC> <MDB> <MDR> ");
	}
	
	public static void argsClientInfo(){
		System.out.println("Usage: java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>*");
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
		
		System.out.println(tmp + protocol + args);

	}
	
	/*
	 * Messages
	 */
	
	public static void sentMessageLog(Message msg){
		System.out.println("(Sent) Type : "+ msg.getType() + " from sender : "+ msg.getSenderId() + " with chunk "+ msg.getChunkNo());
	}
	
	public static void receivedMessageLog(Message msg){
		System.out.println("(Received) Type : "+ msg.getType() + " from sender : "+ msg.getSenderId() + " with chunk "+ msg.getChunkNo());
	}
	
	/*
	 * Files
	 */
	
	public static void errorFileId(String filename){
		System.out.println("Error searching for fileId of "+filename);
	}
	
	public static void errorRestoringFile(String filename){
		System.out.print("Erro restoring file " +  filename);
	}
	
	public static void errorOpeningFile(String filename){
		System.out.println("Error opening "+filename+" file.");
	}
	
	public static void errorFindingFile(String filename){
		System.out.println("Error "+filename+" not found.");
	}
	
	public static void fileRestored(String filename){
		System.out.println("File "+filename+" restored");
	}
	
	public static void chunkRestored(int chunkNo){
		System.out.println("Chunk Number "+chunkNo+" restored");
	}
	
	public static void diskWithoutSpace(){
		System.out.println("Disk without space to store the chunk");
	}
	
	public static void chunkAlreadyExists(){
		System.out.println("Chunk Already Exists");
	}
	
	/*
	 * Directory
	 */
	public static void creatingDir(String string) {
		System.out.println("Creating directory "+ string);
	}
	
	/*
	 * Protocols
	 */
	
	public static void initProtocol(String name){
		System.out.println(name+" Protocol initiated...");
	}
	
	public static void tryNrStoreChunk(int nr,int chunkNo){
		System.out.println("Try number "+ nr + " to stored chunk number "+chunkNo);
	}
	
	public static void tryNrReceiveChunk(int nr,int chunkNo){
		System.out.println("Try number "+ nr + " to receive chunk number "+chunkNo);
	}
	
	public static void allChunksNrStored(int chunkNo){
		System.out.println("All Chunks with number "+ chunkNo+ " Stored");	
	}
	
	public static void chunkRepDegNotAccepted(int chunkNo, int stored){
		System.out.println(stored + " Replication Degree not pleased for chunk number " + chunkNo);
	}
	
	public static void incompatibleProcols(){
		System.out.println("The peers protocols are not compatible");
	}
	
	/*
	 * Exceptions
	 */
	
	public static void exception(String function, String myClass, String error){
		System.out.println("Exception at "+function+ " from class "+myClass+" : "+error);
	}
	
	/*
	 * Client
	 */
	public static void serverResponse(String response){
		System.out.println("Server response : "+response);
	}
	
	public static void enter(){
		System.out.println("Press ENTER to exit...");
	}
	
	/*
	 * Server
	 */

	public static void rmiReady() {
		System.out.println("Server ready!");
	}

	public static void updated(String string) {
		System.out.println("Chunks "+string+" updated");
	}

	public static void serializeWarn(String string, int ID) {
		System.out.println("Serialized data "+string+" peersDisk/peer"+ID+"/record.ser");
	}

	public static void checkChunks(String string) {
		System.out.println("Check chunks "+string+"...");
	}
}
