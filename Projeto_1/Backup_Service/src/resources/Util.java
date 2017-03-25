package resources;

public class Util {
	
	//protocols
	public static final int MAX_TRIES = 5;
	public static final int RND_DELAY = 400;
	public static final int WAITING_TIME = 1000;
	public static final int TIME_REINFORCEMENT = 2;
	
	//peers
	public static final int DISK_SPACE_DEFAULT = 1000000;
	
	//message
	public static enum MessageType
	{
		PUTCHUNK, STORED, GETCHUNK, CHUNK, DELETE, REMOVED
	}
	
	//chunk
	public static int PACKET_MAX_SIZE = 65000;
	
	//fileManager
	public static final String PEERS_DIR = new String("../peersDisk/");
	public static final String CHUNKS_DIR = new String("/chunks/");
	public static final String RESTORES_DIR = new String("/restores/");

}
