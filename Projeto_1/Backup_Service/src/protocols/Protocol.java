package protocols;

import java.util.Random;

import network.Message;
import network.MessageRecord;
import network.MulticastListener;

public abstract class Protocol extends Thread{
	
	Message msg = null;
	Random delay = null;

	/*Vão ser passados pelo peer*/
	MulticastListener mc;	
	MulticastListener mdb;
	MulticastListener mdr;
	MessageRecord msgRecord;
	
	public abstract void run();
}
