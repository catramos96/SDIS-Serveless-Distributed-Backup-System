package protocols;

import java.util.Queue;
import java.util.Random;

import network.Message;
import network.MulticastListener;
import network.MulticastRecord;
import peer.Peer;

public abstract class Protocol extends Thread{
	
	Message msg = null;
	Random delay = null;

	/*V�o ser passados pelo peer*/
	MulticastListener mc;	
	MulticastListener mdb;
	MulticastListener mdr;
	MulticastRecord record;
	
	public abstract void run();
}
