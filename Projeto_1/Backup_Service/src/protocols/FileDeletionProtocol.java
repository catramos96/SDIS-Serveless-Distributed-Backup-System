package protocols;

import network.Message;
import network.MulticastListener;
import peer.Record;

public class FileDeletionProtocol extends Protocol{
	
	/*			MSG="DELETE"		 --> Peer	
	 * InitPeer ---------------> MC ---> Peer 
	 * 								 --> Peer	
	 */

	public FileDeletionProtocol(MulticastListener mc, Record record,Message msg){
		this.mc = mc;
		this.record = record;
		this.msg = msg;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		mc.send(msg);
		
	}
	
}
