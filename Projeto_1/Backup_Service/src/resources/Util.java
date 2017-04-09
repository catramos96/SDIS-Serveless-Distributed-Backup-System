package resources;

import java.util.Random;

public class Util {
	
	//protocols
	public static final int MAX_TRIES = 5;
	public static final int RND_DELAY = 400;
	public static final int WAITING_TIME = 1000;
	public static final int MAX_AVG_DELAY_TIME = 14000; //400+800+1600+3200+6400 (5 rep) + AVG1600
	public static final int TIME_REINFORCEMENT = 2;
	
	//peers
	public static final int DISK_SPACE_DEFAULT = 1000000;
	
	//message
	public static enum MessageType
	{
		PUTCHUNK, STORED, GETCHUNK, CHUNK, DELETE, REMOVED, GETINITIATOR, INITIATOR, GOTCHUNKENH, GETCHUNKENH
	}
	public static int PACKET_MAX_SIZE = 65000;
	
	//chunk
	public static int CHUNK_MAX_SIZE = 64000;
	
	//fileManager
	public static final String PEERS_DIR = new String("../peersDisk/");
	public static final String CHUNKS_DIR = new String("/chunks/");
	public static final String RESTORES_DIR = new String("/restores/");
	public static final String LOCAL_DIR = new String("/localFiles/");

	public static void randomDelay(){
		Random delay = new Random();
		try {
			Thread.sleep(delay.nextInt(Util.RND_DELAY));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
