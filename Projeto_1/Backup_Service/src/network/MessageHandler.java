package network;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import resources.Logs;
import resources.Util;
import resources.Util.MessageType;
import peer.Chunk;
import peer.Peer;
import peer.Record;
import protocols.ChunkBackupProtocol;

public class MessageHandler extends Thread
{
	private Peer peer = null; //peer associado ao listener
	private Message msg = null;
	
	/**
	 * Parse the message to object Message
	 * @param peer
	 * @param msg
	 */
	public MessageHandler(Peer peer,byte[] msg)
	{
		this.peer = peer;
		this.msg = parseMessage(msg);
	}

	/**
	 * Execute actions depending on Message Type
	 */
	public void run() 
	{		
		//Don't process messages that were sent by himself
		if(peer.getID() != msg.getSenderId())
		{	
			Logs.receivedMessageLog(this.msg);
			
			switch (msg.getType()) {
			case PUTCHUNK:
				peer.getMessageRecord().addPutchunkMessage(msg.getFileId(), msg.getChunkNo());
				handlePutchunk(msg.getFileId(),msg.getChunkNo(),msg.getReplicationDeg(),msg.getBody());
				break;
			case STORED:
				peer.getMessageRecord().addStoredMessage(msg.getFileId(), msg.getChunkNo(), msg.getSenderId());
				handleStore(msg.getFileId(), msg.getChunkNo(),msg.getSenderId());	
				break;
			case GETCHUNK:
				handleGetchunk(msg.getFileId(),msg.getChunkNo());
				break;
			case CHUNK:
				//peer.getMessageRecord().addChunkMessage(msg.getFileId(), msg.getChunkNo());
				handleChunk(msg.getFileId(), msg.getChunkNo(), msg.getBody());
				break;
			case DELETE:
				handleDelete(msg.getFileId());
				break;
			case REMOVED:
				handleRemoved(msg.getFileId(),msg.getChunkNo(),msg.getSenderId());
				break;
			default:
				break;
			}
		}
	}
	
	/**
	 * Random Delay
	 */
	public void randomDelay(){
		Random delay = new Random();
		try {
			Thread.sleep(delay.nextInt(Util.RND_DELAY));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Peer response to other peer PUTCHUNK message
	 * @param c
	 */
	private void handlePutchunk(String fileId, int chunkNo,int repDeg,byte[] body){
		Chunk c = new Chunk(fileId, chunkNo, body);

		//response message : STORED
		Message msg = new Message(Util.MessageType.STORED,peer.getVersion(),peer.getID(),c.getFileId(),c.getChunkNo());
		
		//verifies chunk existence in this peer
		boolean alreadyExists = peer.fileManager.chunkExists(c.getFileId(),c.getChunkNo());

		//no space available and file does not exist -> can't store
		if(!peer.fileManager.hasSpaceAvailable(c) && !alreadyExists)
			return;
		else
		{
			//waiting time
			randomDelay();
			int rep, repdes;
			rep = peer.getMessageRecord().getChunkReplication(msg.getFileId(), msg.getChunkNo());
			
			System.out.println("REPLICATION: " + rep + " DESIRED: " + repDeg);

			//If the replication degree is lower thatn the desired
			//if(rep < repDeg){							//--> Enhancement
				
				
				//send STORED message
				peer.getMc().send(msg);
				Logs.sentMessageLog(msg);
				
				//only save if file doesn't exist
				if(!alreadyExists)
					peer.fileManager.saveChunk(c);
			//}
		//	else
			//	peer.getMessageRecord().removeStoredMessages(fileId, chunkNo);	//only keeps the ones refered to his backupChunks --> Enhancement
		}
	}

	/**
	 * Peer response to other peer STORE message
	 */
	private void handleStore(String fileId, int chunkNo, int senderId){
		peer.getMulticastRecord().recordStoreChunks(fileId, chunkNo, senderId);
	}	

	/**
	 * Peer response to other peer GETCHUNK message
	 */
	private void handleGetchunk(String fileId, int chunkNo){
		//peers has stored this chunk
			//if(peer.fileManager.chunkExists(fileId,chunkNo) && !peer.getMessageRecord().receivedChunkMessage(fileId, chunkNo))
		if(peer.fileManager.chunkExists(fileId,chunkNo))
			{
				//body
				byte[] body = peer.fileManager.getChunkContent(fileId, chunkNo);
				//create CHUNK message
				Message msg = new Message(Util.MessageType.CHUNK,peer.getVersion(),peer.getID(),fileId,chunkNo,body);
				randomDelay();
				//chunk still needed by the initiator peer
				if(!peer.chunkRestored(fileId, chunkNo))
				{
					Logs.sentMessageLog(msg);
					peer.getMdr().send(msg);
				}
			}
			//peer.getMessageRecord().removeChunkMessages(fileId, chunkNo);
	}
	
	/**
	 * Peer response to other peer CHUNK message
	 */
	private void handleChunk(String fileId, int chunkNo, byte[] body){
		//verifies if the file belongs to record --> initiator peer
		if(peer.getMulticastRecord().checkRestore(fileId))
		{
			//chunk restore
			if(peer.getMulticastRecord().recordRestoreChunks(fileId,chunkNo,body))
				Logs.chunkRestored(chunkNo);
		}

		//save history of chunks at mdr (chunkNo, fileId)
		//System.out.println("guardou "+chunkNo+fileId);
		peer.addRestoredChunk(chunkNo, fileId);
	}
	
	/**
	 * Peer response to other peer DELETE message
	 */
	private void handleDelete(String fileId){
		peer.fileManager.deleteChunks(fileId);
	}
	
	/**
	 * Peer response to other peer REMOVED message
	 */
	private void handleRemoved(String fileId, int chunkNo, int peerNo){
		
		Record record = peer.getMulticastRecord();
		
		String filename = record.getFilename(fileId);	//from stored
		
		if(filename == null)
			return;
		
		int repChunks = 0;
		ArrayList<Integer> tmp = record.checkStored(fileId, chunkNo);
		
		if(tmp != null)
			repChunks = tmp.size();
		else
			return;
			
		//System.out.println("STORED: " + repChunks);
		
		//if peer is owner of original file
		if(record.deleteStored(fileId, chunkNo, peerNo)){
			
			System.out.println("OWNER");
			
			//calculate replicationDegreeLeft
			int repDegree = record.getReplicationDegree(fileId);
			//System.out.println("REPDEGREE: " + repDegree);
			
			//array de peers que fizeram backup
			tmp = record.checkStored(fileId, chunkNo);
			
			if(tmp !=null)
				repChunks = tmp.size();
			
			if(repDegree<repChunks){
				//System.out.println("STORED: " + repChunks);
				//System.out.println("Filename: " + filename);
				
				ArrayList<Chunk> chunks = peer.fileManager.splitFileInChunks(Util.PEERS_DIR + "Peer" + peer.getID() + Util.RESTORES_DIR + filename);
				if(chunks.size() < chunkNo){
					System.out.println("Ficheiro não foi recuperado totalmente");
					return;
				}
				
				Chunk c = chunks.get(chunkNo);
				
				randomDelay();
				
				//if(!record.checkPutchunk(fileId, chunkNo)){
					Message msg = new Message(MessageType.PUTCHUNK,peer.getVersion(),peer.getID(),fileId,chunkNo,repDegree,c.getData());
					Logs.sentMessageLog(msg);
					//FileInfo fileinfo = new FileInfo(msg.getFileId(),filename,chunks.size(),repDegree);
					//record.startRecordStores(fileinfo);
					new ChunkBackupProtocol(peer.getMdb(), record, msg).start();
				//}
			}//
			
		}
		
	}
	
	/*
	 * Preenche os atributos da classe com os respetivos valores 
	 */
 	private Message parseMessage(byte[] message)
	{
		Message parsed = null;
		
		ByteArrayInputStream stream = new ByteArrayInputStream(message);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
	
		try
		{
			String header = reader.readLine();	//a primeira linha corresponde a header
			
			//interpretação da header
			String[] parts = header.split("\\s");
			
			Util.MessageType type_rcv = validateMessageType(parts[0]);

			//common
			char[] version_rcv = validateVersion(parts[1]);
			int senderId_rcv = Integer.parseInt(parts[2]);
			String fileId_rcv = parts[3];
			
			//all except delete
			int chunkNo_rcv = -1;
			if(type_rcv.compareTo(Util.MessageType.DELETE) != 0)
				chunkNo_rcv = Integer.parseInt(parts[4]);
				
			//just putchunk
			int replicationDeg_rcv = -1;
			if(type_rcv.compareTo(Util.MessageType.PUTCHUNK) == 0){
				replicationDeg_rcv = Integer.parseInt(parts[5]);
			}
			
			//Removes the last sequences of white spaces (\s) and null characters (\0)
			//String msg_received = (new String(packet.getData()).replaceAll("[\0 \\s]*$", ""));
			//temporario?
			int offset = header.length() + Message.LINE_SEPARATOR.length()*2;
			byte[] body = new byte[64000];
			System.arraycopy(message, offset, body, 0, 64000);
			
			//create messages
			if(type_rcv.compareTo(Util.MessageType.DELETE) == 0)
				parsed = new Message(type_rcv,version_rcv,senderId_rcv,fileId_rcv);	
			else if(type_rcv.compareTo(Util.MessageType.GETCHUNK) == 0 || type_rcv.compareTo(Util.MessageType.STORED) == 0 || type_rcv.compareTo(Util.MessageType.REMOVED) == 0)
				parsed = new Message(type_rcv,version_rcv,senderId_rcv,fileId_rcv,chunkNo_rcv) ;	
			else if(type_rcv.compareTo(Util.MessageType.PUTCHUNK) == 0)
				parsed = new Message(type_rcv,version_rcv,senderId_rcv,fileId_rcv,chunkNo_rcv,replicationDeg_rcv,body);
			else if(type_rcv.compareTo(Util.MessageType.CHUNK) == 0)
				parsed = new Message(type_rcv,version_rcv,senderId_rcv,fileId_rcv,chunkNo_rcv,body);
			
			reader.close();
			stream.close();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		return parsed;
	}

	private char[] validateVersion(String string) 
	{
		char[] vs = string.toCharArray();
		char[] peerVersion = peer.getVersion();
		if(vs[0] == peerVersion[0] && vs[1] == peerVersion[1])
			return vs;
		
		return null;	//deve retornar um erro
	}

	private Util.MessageType validateMessageType(String string) 
	{
		//nao sei se ha restricoes aqui
		return Util.MessageType.valueOf(string);
	}

	public Peer getPeer() {
		return peer;
	}

	public void setPeer(Peer peer) {
		this.peer = peer;
	}

}
