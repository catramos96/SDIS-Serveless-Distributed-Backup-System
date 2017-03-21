package protocols;

import java.util.Random;

import network.Message;
import network.MulticastListener;

public class ChunkBackupProtocol extends Protocol{
	
	private int stored = 0;

	/*			MSG="PUTCHUNK"		  --> Peer		MSG="STORED"		sleep(1sec)
	 * InitPeer ---------------> MDB ---> Peer -------------------> MC -------------> InitPeer
	 * 								  --> Peer		Random Delay
	 */
	
	public ChunkBackupProtocol(MulticastListener mdb, MulticastListener mc){
		this.mdb = mdb;
		this.mc = mc;
		this.delay = new Random();
	}
	
	public void incStored(){
		stored++; /*if received store for a specific chunk*/
	}

	@Override
	public void warnPeers(Message msg) {
		stored = 0;
		int rep = 0;
		int waitingTime = 1000;
		
		while(rep < 5)
		{
			mdb.send(msg);		//msg PutChunk
			
			try {
				Thread.sleep(waitingTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(stored >= msg.getReplicationDeg())	//replication degree done
				break;
			
			waitingTime *= 2;	//doubles time for each rep
			rep++;
		}
		
		//stored = 0; //reiniciar os contadores
	}

	@Override
	public void executeProtocolAction(Message msg) {
		System.out.println("2 - Protocol: Executing Chunk Backup Protocol");
	
		try 
		{
			Thread.sleep(delay.nextInt(400)); //delay
		} 
		catch (InterruptedException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		mc.send(msg);
	}
}
