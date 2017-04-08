package network;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MessageRMI extends Remote {
	
	/**
	 * Initiate backup protocol with the filename and replication Degree received from client.
	 * @param filename
	 * @param repDeg
	 * @return Message for client
	 * @throws RemoteException
	 */
	 String backup(String filename, int repDeg, boolean enhancement) throws RemoteException;
	 
	 /**
	  * Initiate restore protocol with the filename received from client.
	  * @param filename
	  * @return Message for client
	  * @throws RemoteException
	  */
	 String restore(String filename, boolean enhancement) throws RemoteException;
	 
	 /**
	  * Initiate delete protocol with the filename received from client.
	  * @param filename
	  * @return message for client
	  * @throws RemoteException
	  */
	 String delete(String filename, boolean enhancement) throws RemoteException;
	 
	 /**
	  * Initiate delete protocol with the filename received from client.
	  * @param spaceToReclaim
	  * @return Message for client.
	  * @throws RemoteException
	  */
	 String reclaim(int spaceToReclaim, boolean enhancement) throws RemoteException;
	 
	 /**
	  * Initiate state protocol . 
	  * @return Message for client
	  * @throws RemoteException
	  */
	 String state() throws RemoteException;
}
