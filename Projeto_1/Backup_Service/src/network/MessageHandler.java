package network;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import resources.Logs;
import resources.Util;
import peer.Chunk;
import peer.Peer;

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
			switch (msg.getType()) {
			case PUTCHUNK:
				handlePutchunk(msg.getFileId(),msg.getChunkNo(),msg.getBody());
				break;
			case STORED:
				handleStore(msg.getFileId(), msg.getChunkNo(),msg.getSenderId());	
				break;
			case GETCHUNK:
				handleGetchunk(msg.getFileId(),msg.getChunkNo());
				break;
			case CHUNK:
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
			
			Logs.receivedMessageLog(msg);
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
	private void handlePutchunk(String fileId, int chunkNo,byte[] body){
		Chunk c = new Chunk(fileId, chunkNo, body);

		//response message : STORED
		Message msg = new Message(Util.MessageType.STORED,peer.getVersion(),peer.getID(),c.getFileId(),c.getChunkNo());
		Logs.sentMessageLog(msg);
		
		//verifies chunk existence in this peer
		boolean alreadyExists = peer.fileManager.chunkExists(c.getFileId(),c.getChunkNo());

		//no space available and file does not exist -> can't store
		if(!peer.fileManager.hasSpaceAvailable(c) && !alreadyExists)
			return;
		else
		{
			//waiting time
			randomDelay();

			/*if(record.checkStored(msg.getFileId(), msg.getChunkNo()) < c.getReplicationDeg()){*/

			//send STORED message
			peer.getMc().send(msg);
			//only save if file doesn't exist
			if(!alreadyExists)
				peer.fileManager.saveChunk(c);

			/*}	*/
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
		
		//if peer is owner of original file
		if(peer.getMulticastRecord().deleteStored(fileId, chunkNo, peerNo)){
			
			//calculate replicationDegreeLeft
			
			//initiate backup protocol
			
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

			char[] version_rcv = validateVersion(parts[1]);
			int senderId_rcv = Integer.parseInt(parts[2]);
			String fileId_rcv = parts[3];
			int chunkNo_rcv = -1;
			if(type_rcv.compareTo(Util.MessageType.DELETE) != 0)
				chunkNo_rcv = Integer.parseInt(parts[4]);
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
			
			parsed = new Message(type_rcv,version_rcv,senderId_rcv,fileId_rcv,chunkNo_rcv,replicationDeg_rcv,body);			
		
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
