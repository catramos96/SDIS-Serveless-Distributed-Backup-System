package protocols;
import peer.Peer;

public class ChunkRestoreProtocol extends Protocol{

	private Peer peer = null;
	private String filename = null;

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
