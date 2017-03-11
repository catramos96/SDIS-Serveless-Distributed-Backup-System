package protocols;

import java.util.Random;

import network.Message;
import network.MulticastListener;

public abstract class Protocol {
	
	Message message = null;
	Random delay = null;
	/*Vão ser passados pelo peer*/
	MulticastListener mc;	
	MulticastListener mdb;
	MulticastListener mdr;

	/*
	 * Common Functions
	 */	
	abstract void warnPeers();				//send messages
	
	abstract void executeProtocolAction();	//receive messages (or not) and perform an action
}
