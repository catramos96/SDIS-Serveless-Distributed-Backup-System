package protocols;

import java.util.Random;

import network.Message;
import network.MessageRecord;
import network.MulticastListener;

/**
 * Abstract Class Protocol used as a template for the protocols
 * structure
 * @attribute msg - Message to be sent to a certain MulticastListener channel
 * @attribute delay - Random delay time to be used to wait between sending a message
 * @attribute mc - Multicast channel
 * @attribute mdb - Multicast channel
 * @attribute mdr - Multicast channel
 * @attribute msgRecord - MessageRecord of the peer
 */
public abstract class Protocol extends Thread{
	
	Message msg = null;			
	Random delay = null;

	/*Passed by the peer*/
	MulticastListener mc;	
	MulticastListener mdb;
	MulticastListener mdr;
	MessageRecord msgRecord;
	
	public abstract void run();
}
