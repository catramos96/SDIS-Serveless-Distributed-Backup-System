package peer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

import network.DatagramListener;
import network.Message;
import network.Message.MessageType;
import network.MessageHandler;
import network.MulticastListener;
import protocols.ChunkBackupProtocol;
import protocols.ChunkRestoreProtocol;
import protocols.FileDeletionProtocol;
import protocols.SpaceReclaimingProtocol;

public class Peer {

	private int ID = 0;
	private char[] version = {'1','.','0'};		//TEMPORARIO

	/*listeners*/
	public DatagramListener socket = null; 	//socket for communication with client

	public MulticastListener mc = null;
	public MulticastListener mdb = null;
	public MulticastListener mdr = null;

	/*Protocols*/
	public ChunkBackupProtocol backupProt = null;
	public ChunkRestoreProtocol restoreProt = null;
	public FileDeletionProtocol deleteProt = null;
	public SpaceReclaimingProtocol spaceReclProt = null;

	/*objects*/
	public FileManager fileManager = null;
	public MessageHandler handler;

	public Peer(int id, String[] access_point, String[] mc_ap, String[] mdb_ap, String[] mdr_ap)
	{
		this.ID = id;
		fileManager = new FileManager(ID);
		handler = new MessageHandler(this); 

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
			mc = new MulticastListener(address,port,handler);

			address = InetAddress.getByName(mdb_ap[0]);
			port = Integer.parseInt(mdb_ap[1]);
			mdb = new MulticastListener(address,port,handler);
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

			Thread.sleep(1000);		//delay para inicializar as variaveis do multicast

			backupProt = new ChunkBackupProtocol(mdb,mc);	//mdb,mc

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
	
	public void putchunkAction(Chunk c)
	{	
		//cria a mensagem a enviar no protocolo
		Message msg = new Message(MessageType.STORED,version,ID,c.getFileId(),c.getChunkNo());
		
		//se o ficheiro ja existir apenas envia a mensagem
		
		//se nao existir, envia e guarda
		backupProt.executeProtocolAction(msg);
		fileManager.save(c);
	}
	
	public void storeAction()
	{
		//verificar que o multicast esta a espera de receber respostas para este fileId ??
		//mapeamento para saber onde esta guardado este chunk ??
		
		backupProt.incStored();
	}

	/**
	 * TODO transformar isto num ClientHandler?
	 * Actions:
	 * 1 - Backup
	 * 2 - Restore
	 * 3 - Delete
	 * 4 - Space Reclaiming
	 */

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
				backupProt.warnPeers(msg);
			}
		}
		else if(action.equals("RESTORE"))
		{
			restoreProt.warnPeers(null);
		}
		else if(action.equals("DELETE"))
		{
			deleteProt.warnPeers(null);
		}
		else if(action.equals("RECLAIM"))
		{
			spaceReclProt.warnPeers(null);
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
}
