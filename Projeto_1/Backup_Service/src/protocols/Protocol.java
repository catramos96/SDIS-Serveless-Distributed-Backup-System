package protocols;

import java.util.Queue;
import java.util.Random;

import network.Message;
import network.MulticastListener;
import network.MulticastRecord;
import peer.Peer;

public abstract class Protocol{
	
	Message message = null;
	Random delay = null;

	/*Vão ser passados pelo peer*/
	MulticastListener mc;	
	MulticastListener mdb;
	MulticastListener mdr;
	MulticastRecord record;

	/*
	 * Common Functions
	 */	
	abstract void warnPeers(Message msg);				//send messages
	
	abstract void executeProtocolAction(Message msg);	//receive messages and perform an action
}
