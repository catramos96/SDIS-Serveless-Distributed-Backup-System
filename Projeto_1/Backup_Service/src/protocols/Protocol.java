package protocols;

import java.util.Random;

import network.Message;
import network.MessageRecord;
import network.MulticastListener;
import peer.Record;

public abstract class Protocol extends Thread{
	
	Message msg = null;
	Random delay = null;

	/*Vão ser passados pelo peer*/
	MulticastListener mc;	
	MulticastListener mdb;
	MulticastListener mdr;
	Record record;
	MessageRecord msgRecord;
	
	public abstract void run();
}
