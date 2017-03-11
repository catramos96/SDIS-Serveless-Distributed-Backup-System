package protocols;

import java.util.Random;

import network.Message;

public interface Protocol {
	
	Message message = null;
	Random delay = null;
	/*
	 * NetworkInformation ?
	 */

	/*
	 * Common Functions
	 */
	void warnPeers();				//send messages
	
	void executeProtocolAction();	//receive messages and do something
}
