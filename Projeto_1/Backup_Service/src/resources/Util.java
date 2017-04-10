package resources;

import java.util.Random;

/**
 * Class Util containing important constants and others.
 */
public class Util {
	
	//Protocol
	public static final int MAX_TRIES = 5;				//Max tries
	public static final int RND_DELAY = 400;			//Max random delay
	public static final int WAITING_TIME = 1000;
	public static final int MAX_AVG_DELAY_TIME = 14000; //Maximum delay time for a protocol such as backup and restore 
														//400+800+1600+3200+6400 (5 rep) + AVG1600
	public static final int TIME_REINFORCEMENT = 2;
	
	//Peer
	public static final int DISK_SPACE_DEFAULT = 1000000;
	
	//Message
	/**
	 * Enumeration to represent the information about the several messages types
	 */
	public static enum MessageType
	{
		PUTCHUNK, STORED, GETCHUNK, CHUNK, DELETE, REMOVED, GETINITIATOR, INITIATOR, GOTCHUNKENH, GETCHUNKENH
	}
	public static int PACKET_MAX_SIZE = 65000;
	
	//Chunk
	public static int CHUNK_MAX_SIZE = 64000;
	
	//FileManager - Directories
	public static final String PEERS_DIR = new String("../peersDisk/");
	public static final String CHUNKS_DIR = new String("/chunks/");
	public static final String RESTORES_DIR = new String("/restores/");
	public static final String LOCAL_DIR = new String("/localFiles/");

	/**
	 * Function that waits a random delay time.
	 */
	public static void randomDelay(){
		Random delay = new Random();
		try {
			Thread.sleep(delay.nextInt(Util.RND_DELAY));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
