package protocols;

import java.util.Random;

import network.Message;
import network.MulticastListener;
import network.MulticastRecord;

public abstract class Protocol extends Thread{
	
	Message msg = null;
	Random delay = null;

	/*Vão ser passados pelo peer*/
	MulticastListener mc;	
	MulticastListener mdb;
	MulticastListener mdr;
	MulticastRecord record;
	
	public abstract void run();
}
