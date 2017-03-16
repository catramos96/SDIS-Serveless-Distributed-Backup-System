package peer;


import java.io.IOException;
import java.net.InetAddress;

import network.DatagramListener;
import network.Message;
import network.Message.MessageType;
import network.MulticastListener;
import protocols.ChunkBackupProtocol;
import protocols.ChunkRestoreProtocol;
import protocols.FileDeletionProtocol;
import protocols.SpaceReclaimingProtocol;

public class Peer {

	public static int ID = 0;
	public DatagramListener socket = null; 	//socket for communication with client

	public MulticastListener mc = null;
	public MulticastListener mdb = null;
	public MulticastListener mdr = null;

	/*Protocols*/
	public ChunkBackupProtocol backupProt = null;
	public ChunkRestoreProtocol restoreProt = null;
	public FileDeletionProtocol deleteProt = null;
	public SpaceReclaimingProtocol spaceReclProt = null;

	public FileManager fileManager = null;

	public Peer(int id, String[] access_point, String[] mc_ap, String[] mdb_ap, String[] mdr_ap)
	{
		this.ID = id;
		fileManager = new FileManager();

		try 
		{
			//socket de conexao com o cliente
			InetAddress address = InetAddress.getByName(access_point[0]);
			int port = Integer.parseInt(access_point[1]);
			socket = new DatagramListener(address, port+id,this);	

			//sockets multicast
			if(mc_ap[0] == "")
				address = InetAddress.getLocalHost();
			else	
				address = InetAddress.getByName(mc_ap[0]);

			port = Integer.parseInt(mc_ap[1]);
			mc = new MulticastListener(address,port,this);

			/*
			address = InetAddress.getByName(mdb_ap[0]);
			port = Integer.parseInt(mdb_ap[1]);
			mdb = new MulticastListener(address,port);

			address = InetAddress.getByName(mdr_ap[0]);
			port = Integer.parseInt(mdr_ap[1]);
			mdr = new MulticastListener(address,port);
			 */			

			//inicializacao dos channels
			socket.start();

			mc.start();
			/*
			mdb.start();
			mdr.start();
			 */

			Thread.sleep(1000);		//delay para inicializar as vari�veis do multicast

			backupProt = new ChunkBackupProtocol(mc,mc);	//mrd,mc
			restoreProt = new ChunkRestoreProtocol(mc,mc);	//mdr,mc
			deleteProt = new FileDeletionProtocol(mc);
			spaceReclProt = new SpaceReclaimingProtocol(mc);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* Actions:
	 * 1 - Backup
	 * 2 - Restore
	 * 3 - Delete
	 * 4 - Space Reclaiming
	 */

	public void doAction(String action, String filename){
		Message msg = new Message(MessageType.PUTCHUNK,1,2,3,4,5,"HELLO");
		System.out.println("Message: " + msg.buildMessage());

		if(action.equals("BACKUP")){
			fileManager.splitFileInChunks(filename);
			backupProt.warnPeers(msg);
		}
		else if(action.equals("RESTORE")){
			restoreProt.warnPeers(msg);
		}
		else if(action.equals("DELETE")){
			deleteProt.warnPeers(msg);
		}
		else if(action.equals("RECLAIM")){
			spaceReclProt.warnPeers(msg);
		}
		else{
			System.out.println("Invalid Action");
		}
	}

	/*
	 * FROM MULTICAST 
	 */
	public void notify(String message){
		System.out.println("Notification: " + message);
		System.out.println("Length: " + message.length());

		/*Depois de resolver o problema dos white spaces passar para um switch*/
		if(message.contains("backup"))				backupProt.executeProtocolAction();
		else if(message.contains("deletion"))		deleteProt.executeProtocolAction();
		else if(message.contains("restore"))		restoreProt.executeProtocolAction();
		else if(message.contains("space_reclaim"))	spaceReclProt.executeProtocolAction();
		else										System.out.println("Notification: ??");
	}

	/**
	 * Gets e sets
	 */
	public static int getId()
	{
		return ID;
	}
}
