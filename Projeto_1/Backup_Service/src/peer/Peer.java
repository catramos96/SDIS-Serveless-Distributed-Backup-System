package peer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;

import network.DatagramListener;
import network.Message;
import resources.Util.MessageType;
import network.MulticastListener;
import network.MulticastRecord;
import protocols.ChunkBackupProtocol;
import protocols.ChunkRestoreProtocol;
import protocols.FileDeletionProtocol;
import protocols.SpaceReclaimingProtocol;
import resources.Util;

public class Peer {

	private int ID = 0;
	private char[] version = {'1','.','0'};		//TEMPORARIO

	/*listeners*/
	public DatagramListener socket = null; 	//socket for communication with client

	public MulticastListener mc = null;
	public MulticastListener mdb = null;
	public MulticastListener mdr = null;

	/*objects*/
	public FileManager fileManager = null;
	
	/*MulticastRecord*/
	public MulticastRecord record = null;

	public Peer(int id, String[] access_point, String[] mc_ap, String[] mdb_ap, String[] mdr_ap)
	{
		this.ID = id;
		fileManager = new FileManager(ID,Util.DISK_SPACE_DEFAULT);
		record = new MulticastRecord();
		
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

			address = InetAddress.getByName(mdb_ap[0]);
			port = Integer.parseInt(mdb_ap[1]);
			mdb = new MulticastListener(address,port,this);
			/*
			address = InetAddress.getByName(mdr_ap[0]);
			port = Integer.parseInt(mdr_ap[1]);
			mdr = new MulticastListener(address,port);
			 */			

			//inicializacao dos channels
			socket.start();

			mc.start();
			mdb.start();
			/*
			mdr.start();
			 */

			Thread.sleep(Util.WAITING_TIME);		//delay para inicializar as variaveis do multicast

			/*backupProt = new ChunkBackupProtocol(mdb,mc,record);	//mdb,mc
			restoreProt = new ChunkRestoreProtocol(mc,mc,record);	//mdr,mc
			deleteProt = new FileDeletionProtocol(mc,record);
			spaceReclProt = new SpaceReclaimingProtocol(mc,record);*/

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void putchunkAction(Chunk c)
	{	
		//cria a mensagem a enviar no protocolo
		Message msg = new Message(Util.MessageType.STORED,version,ID,c.getFileId(),c.getChunkNo());
		
		//se nao existir e nao tiver espaco 
		if(!fileManager.hasSpaceAvailable(c))
			return;
		else
		{
			randomDelay();
			if(record.checkStored(msg.getFileId(), msg.getChunkNo()) < c.getReplicationDeg()){
				mc.send(msg);
				fileManager.save(c);
			}	
		}
	}
	
	public void storeAction(String fileId,int chunkNo)
	{
		record.recordStoreChunk(fileId, chunkNo, ID);
	}

	public void initiateProtocol(String action, String filename, int replicationDegree){

		if(action.equals("BACKUP"))
		{
			//separa o ficheiro em chunks
			ArrayList<Chunk> chunks = fileManager.splitFileInChunks(filename);	
			//faz warn dos peers para cada chunk
			for (int i = 0; i < chunks.size(); i++) 
			{
				Chunk c = chunks.get(i);
				Message msg = new Message(MessageType.PUTCHUNK,version,ID,c.getFileId(),c.getChunkNo(),replicationDegree,c.getData());
				new ChunkBackupProtocol(mdb,record,msg).start();
			}
		}
		else if(action.equals("RESTORE"))
		{
			//restoreProt.warnPeers(null);
		}
		else if(action.equals("DELETE"))
		{
			//deleteProt.warnPeers(null);
		}
		else if(action.equals("RECLAIM"))
		{
			//spaceReclProt.warnPeers(null);
		}
		else
		{
			System.out.println("Invalid Action");
		}
	}

	public char[] getVersion() {
		return version;
	}

	public void setVersion(char[] version) {
		this.version = version;
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}
	
	public MulticastListener getMc(){
		return mc;
	}
	
	public MulticastListener getMdb(){
		return mdb;
	}
	
	public MulticastRecord getMulticastRecord(){
		return record;
	}
	
	public void randomDelay(){
		Random delay = new Random();
		try {
			Thread.sleep(delay.nextInt(Util.RND_DELAY));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
