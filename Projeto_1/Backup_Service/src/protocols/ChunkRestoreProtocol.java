package protocols;
import java.security.NoSuchAlgorithmException;

import network.Message;
import peer.FileInfo;
import peer.Peer;
import resources.Util.MessageType;

public class ChunkRestoreProtocol extends Protocol{

	private Peer peer;
	private String filename;

	public ChunkRestoreProtocol(Peer peer, String filename){
		this.peer = peer;
		this.filename = filename;
	}

	@Override
	public void run()  
	{
		/*Espera por receber cada chunk ?
		 * tem delay?*/
	}
}
