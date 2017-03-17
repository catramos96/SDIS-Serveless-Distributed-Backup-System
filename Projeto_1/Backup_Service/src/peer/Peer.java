package peer;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;

import network.DatagramListener;
import network.Message;
import network.Message.MessageType;
import network.MulticastListener;
import protocols.ChunkBackupProtocol;
import protocols.ChunkRestoreProtocol;
import protocols.FileDeletionProtocol;
import protocols.SpaceReclaimingProtocol;

public class Peer {

	public int ID = 0;
	public final char[] version = {'1','.','0'};

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
		fileManager = new FileManager(ID);

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

	/* Actions:
	 * 1 - Backup
	 * 2 - Restore
	 * 3 - Delete
	 * 4 - Space Reclaiming
	 */

	public void doAction(String action, String filename, int replicationDegree){

		if(action.equals("BACKUP"))
		{
			ArrayList<Chunk> chunks = fileManager.splitFileInChunks(filename);
			Chunk c = chunks.get(0);	
			
			//TODO fazer um ciclo while de warn de peers
			Message msg = new Message(MessageType.PUTCHUNK,version,ID,c.getFileId(),c.getChunkNo(),replicationDegree,c.getData());

			backupProt.warnPeers(msg);
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

	/*
	 * FROM MULTICAST 
	 */
	public void notify(byte[] message){

		Message received = parseMessage(message);

		/*if(message.contains("PUTCHUNK"))
		{
			//duvida : e aqui que executa "guardar o chunk" e "criar messsage" ?
			//backupProt.executeProtocolAction(msg); ?
			backupProt.executeProtocolAction();
		}*/

		/*Depois de resolver o problema dos white spaces passar para um switch
		if(message.contains("backup"))				backupProt.executeProtocolAction();
		else if(message.contains("deletion"))		deleteProt.executeProtocolAction();
		else if(message.contains("restore"))		restoreProt.executeProtocolAction();
		else if(message.contains("space_reclaim"))	spaceReclProt.executeProtocolAction();
		else										System.out.println("Notification: ??");
		 */
	}

	/*
	 * Preenche os atributos da classe com os respetivos valores 
	 */
	private Message parseMessage(byte[] message)
	{
		
		ByteArrayInputStream stream = new ByteArrayInputStream(message);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
	
		try
		{
			String header = reader.readLine();	//a primeira linha corresponde a header
			
			//interpretação da header
			String[] parts = header.split("\\s");
			
			MessageType type_rcv = validateMessageType(parts[0]); 
			char[] version_rcv = validateVersion(parts[1]);
			int senderId_rcv = Integer.parseInt(parts[2]);
			String fileId_rcv = parts[3];
			int chunkNo_rcv = validateChunkNo(parts[4],type_rcv);
			int replicationDeg_rcv = validateReplicationDeg(parts[5],type_rcv);
			
			//temporario?
			int offset = header.length() + Message.LINE_SEPARATOR.length()*2;
			byte[] body = new byte[64000];
			System.arraycopy(message, offset, body, 0, 64000);
			
			return new Message(type_rcv,version_rcv,senderId_rcv,fileId_rcv,chunkNo_rcv,replicationDeg_rcv,body);			
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	private int validateReplicationDeg(String string, MessageType type_rcv) 
	{
		if(type_rcv.compareTo(MessageType.PUTCHUNK) == 0)
			return Integer.parseInt(string);
		return -1;
	}

	private int validateChunkNo(String string, MessageType type) 
	{
		if(type.compareTo(MessageType.DELETE) != 0)
			return Integer.parseInt(string);
		return -1;
	}

	private char[] validateVersion(String string) 
	{
		char[] vs = string.toCharArray();
		if(vs[0] == version[0] && vs[1] == version[1])
			return vs;
		
		return null;	//deve retornar um erro
	}

	private MessageType validateMessageType(String string) 
	{
		//nao sei se ha restricoes aqui
		return MessageType.valueOf(string);
	}
}
