package network;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MessageRMI extends Remote {
	
	 String backup(String filename, int repDeg) throws RemoteException;
	 
	 String restore(String filename) throws RemoteException;
	 
	 String delete(String filename) throws RemoteException;
	 
	 String reclaim(int spaceToReclaim) throws RemoteException;
	 
	 String state() throws RemoteException;
}
