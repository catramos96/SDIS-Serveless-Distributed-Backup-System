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
import peer.FileInfo;
import peer.Peer;
import peer.Record;
import protocols.ChunkBackupProtocol;

/**
 * This class handles messages received by the multicast channels.
 */
public class MessageHandler extends Thread
{
	private Peer peer = null;
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
		//protocols have the same version
		if(!protocolsCompatible())
		{
			System.out.println("Peers protocols not compatible.");
			return;
		}
		//Don't process messages that were sent by himself!
		if((peer.getID() != msg.getSenderId()) )
		{	
			Logs.receivedMessageLog(this.msg);

			switch (msg.getType()) {
			case PUTCHUNK:
				//record this message (PUTCHUNK) at 'MessageRecord'
				peer.getMessageRecord().addPutchunkMessage(msg.getFileId(), msg.getChunkNo());
				handlePutchunk(msg.getFileId(),msg.getChunkNo(),msg.getReplicationDeg(),msg.getBody());
				break;
			case STORED:
				//record this message (STORED) at 'MessageRecord'
				peer.getMessageRecord().addStoredMessage(msg.getFileId(), msg.getChunkNo(), msg.getSenderId());
				handleStore(msg.getFileId(), msg.getChunkNo(),msg.getSenderId());	
				break;
			case GETCHUNK:
				handleGetchunk(msg.getFileId(),msg.getChunkNo());
				break;
			case CHUNK:
				//record this message (CHUNK) at 'MessageRecord'
				peer.getMessageRecord().addChunkMessage(msg.getFileId(), msg.getChunkNo());
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

	private boolean protocolsCompatible() {
		char[] receptor = peer.getVersion();
		char[] sender = msg.getVersion();
		
		for(int i = 0; i < receptor.length; i++)
			if(receptor[i] != sender[i])
				return false;
		return true;
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
	private synchronized void handlePutchunk(String fileId, int chunkNo, int repDeg, byte[] body){
		Chunk c = new Chunk(fileId, chunkNo, body);

		//create response message : STORED
		Message msg = new Message(Util.MessageType.STORED,peer.getVersion(),peer.getID(),c.getFileId(),c.getChunkNo());

		//verifies chunk existence in this peer
		boolean alreadyExists = peer.getMulticastRecord().checkMyChunk(fileId, chunkNo);

		//no space available and chunk wasn't stored yet -> can't store
		if(!peer.fileManager.hasSpaceAvailable(c) && !alreadyExists)
		{
			evictChunks();
		}
		else
		{
			if(alreadyExists)				//warns immediately
			{
				peer.getMc().send(msg);
				Logs.sentMessageLog(msg);
			}
			else
			{
				//waiting time
				randomDelay();
				int rep = 0;
				//Get replication degree recorded before the peer processed the store
				ArrayList<Integer> peersWithChunk = peer.getMessageRecord().getPeersWithChunk(fileId, chunkNo);

				if(peersWithChunk != null)
					rep = peersWithChunk.size();

				//If the replication degree is lower that the desired
				//if(rep < repDeg){							//--> Enhancement*/
				//send STORED message
				peer.getMc().send(msg);
				Logs.sentMessageLog(msg);

				//save chunk in memory
				peer.fileManager.saveChunk(c);

				//Save info on 'Record' 
				peer.getMulticastRecord().addToMyChunks(fileId, chunkNo, repDeg);

				//Update Actual Replication Degree
				peer.getMulticastRecord().setPeersOnMyChunk(fileId, chunkNo, peersWithChunk);
				peer.getMulticastRecord().addPeerOnMyChunk(fileId, chunkNo, peer.getID());

				//System.out.println("CHUNK " + chunkNo + " REPLICATION: " + (int)(rep+1) + " DESIRED: " + repDeg);	
				/*}
				else
					peer.getMessageRecord().removeStoredMessages(fileId, chunkNo);	//only keeps the ones refered to his backupChunks --> Enhancement
				 */
			}
		}
	}

	/**
	 * The peer will try to free some space by evicting chunks whose actual replication degree is higher 
	 * than the desired replication degree
	 */
	private void evictChunks() 
	{
		//find chunks whose actual replication degree is higher than the desired replication
		ArrayList<Chunk> chunks = peer.record.getChunksWithRepAboveDes();

		for (int i = 0; i < chunks.size(); i++) {
			String filename = chunks.get(i).getChunkNo() + chunks.get(i).getFileId();
			peer.fileManager.deleteFile(filename);
		}
	}

	/**
	 * Peer response to other peer STORE message
	 */
	private synchronized void handleStore(String fileId, int chunkNo, int senderId){
		//Updates the Replication Degree if the peer has the chunk
		peer.getMulticastRecord().addPeerOnMyChunk(fileId,chunkNo,senderId);

		Chunk c = peer.getMulticastRecord().getMyChunk(fileId, chunkNo);

		if(c != null)
			System.out.println("CHUNK " + chunkNo + " REPDEG: " + c.getAtualRepDeg());

		//Record the storedChunks in case the peer is the OWNER of the backup file
		peer.getMulticastRecord().recordStoredChunk(fileId, chunkNo, senderId);
	}	

	/**
	 * Peer response to other peer GETCHUNK message
	 */
	private synchronized void handleGetchunk(String fileId, int chunkNo){

		//peer has chunk stored
		if(peer.record.checkMyChunk(fileId, chunkNo))
		{
			//body
			byte[] body = peer.fileManager.getChunkContent(fileId, chunkNo);
			//create CHUNK message
			Message msg = new Message(Util.MessageType.CHUNK,peer.getVersion(),peer.getID(),fileId,chunkNo,body);

			//wait 0-400 ms
			randomDelay();

			//chunk still needed by the initiator peer
			if(!peer.getMessageRecord().receivedChunkMessage(fileId, chunkNo))
			{
				peer.getMdr().send(msg);
				Logs.sentMessageLog(msg);
			}
		}
		//peer.getMessageRecord().removeChunkMessages(fileId, chunkNo);
	}

	/**
	 * Peer response to other peer CHUNK message
	 */
	private synchronized void handleChunk(String fileId, int chunkNo, byte[] body){
		//chunk message received by initiator peer 
		FileInfo info = peer.getMulticastRecord().getRestoredFileInfoById(fileId);

		//this peer is able to restore the file
		if(info != null){
			//record chunk as restored
			if(peer.getMulticastRecord().recordRestoredChunk(fileId,chunkNo,body))
				Logs.chunkRestored(chunkNo);
		}
	}

	/**
	 * Peer response to other peer DELETE message
	 */
	private synchronized void handleDelete(String fileId){
		//verifies if the current peer has chunks stored from this file
		if(peer.getMulticastRecord().myChunksBelongsToFile(fileId))
		{
			//deletes chunks from disk
			peer.fileManager.deleteChunks(fileId);	
			//remove from record
			peer.getMulticastRecord().deleteMyChunksByFile(fileId);
		}
	}

	/**
	 * Peer response to other peer REMOVED message
	 */
	private synchronized void handleRemoved(String fileId, int chunkNo, int peerNo){

		Record record = peer.getMulticastRecord();
		FileInfo info = record.getBackupFileInfoById(fileId);	//from stored

		if(info == null)
			return;

		byte[] data = null;
		int repDegree = 0;
		int desiredRepDegree = 0;

		//This peer initiated the backup of this file (with fileId received)
		if(record.checkStoredChunk(fileId, chunkNo) != null)
		{			
			//Update stored record
			record.deleteStored(fileId, chunkNo, peerNo);
			desiredRepDegree = info.getNumChunks();

			//Actual replication degree
			ArrayList<Integer> peersWithChunk = record.checkStoredChunk(fileId, chunkNo);
			if(peersWithChunk != null)
				repDegree = peersWithChunk.size();

			if(repDegree < desiredRepDegree){
				ArrayList<Chunk> chunks = peer.fileManager.splitFileInChunks(info.getPath());
				Chunk c = chunks.get(chunkNo);
				data = c.getData();
			}
		}
		//Not Owner but has the chunk stored
		else if(peer.getMulticastRecord().checkMyChunk(fileId, chunkNo))
		{
			//remove peer from 'Record'
			peer.getMulticastRecord().remPeerWithMyChunk(fileId, chunkNo, peerNo);

			//get data to start backup protocol
			data = peer.fileManager.getChunkContent(fileId, chunkNo);
			repDegree = peer.getMulticastRecord().getMyChunk(fileId, chunkNo).getAtualRepDeg();
			desiredRepDegree = peer.getMulticastRecord().getMyChunk(fileId, chunkNo).getReplicationDeg();
		}

		/*
		 * If replicaiton degree is bellow desired it will start the chunkbackup protocol
		 * only if after a random time it doesn't received any putchunk for the same fileId and chunkNo
		 */
		if(repDegree < desiredRepDegree){
			peer.getMessageRecord().removePutChunkMessages(fileId, chunkNo);	//reset recording
			peer.getMessageRecord().startRecordingPutchunks(fileId, chunkNo);	//start record

			randomDelay();

			if(!peer.getMessageRecord().receivedPutchunkMessage(fileId, chunkNo)){
				Message msg = new Message(MessageType.PUTCHUNK,peer.getVersion(),peer.getID(),fileId,chunkNo,repDegree,data);
				Logs.sentMessageLog(msg);
				new ChunkBackupProtocol(peer.getMdb(), record, msg).start();
			}
		}

	}

	/**
	 * Fill the object 'Message'
	 * @param message
	 * @return
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

	/*
	 * Validates
	 */

	private char[] validateVersion(String string) 
	{
		char[] vs = string.toCharArray();
		char[] peerVersion = peer.getVersion();
		if(vs[0] == peerVersion[0] && vs[1] == peerVersion[1] && vs[2] == peerVersion[2])
			return vs;

		return null;	//deve retornar um erro
	}

	private Util.MessageType validateMessageType(String string) 
	{
		//nao sei se ha restricoes aqui
		return Util.MessageType.valueOf(string);
	}

	/*
	 * gets e sets
	 */
	public Peer getPeer() {
		return peer;
	}

	public void setPeer(Peer peer) {
		this.peer = peer;
	}

}
