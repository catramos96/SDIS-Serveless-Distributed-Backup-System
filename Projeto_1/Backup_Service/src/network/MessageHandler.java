package network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import resources.Logs;
import resources.Util;
import resources.Util.MessageType;
import peer.Chunk;
import peer.FileInfo;
import peer.Peer;
import peer.Record;
import protocols.ChunkBackupProtocol;

/**
 * This class handles messages received by the MulticastChannels and DatagramChannels.
 */
public class MessageHandler extends Thread
{
	private Peer peer = null;		/**@attribute Peer peer - represents the peer that is part of the comunication channels*/
	private Message msg = null;		/**@attribute Message msg - message to handle*/

	/**
	 * Parse the message to object Message
	 * @param peer
	 * @param msg
	 */
	public MessageHandler(Peer peer,byte[] msg)
	{
		this.peer = peer;

		//parse the msg in byte[] received directly by te channel to a object Message
		this.msg = Message.parseMessage(msg,peer.getVersion());
	}

	/**
	 * Execute actions depending on Message Type
	 */
	public void run() 
	{		
		//Protocols must have the same version
		if(!protocolsCompatible())
		{
			Logs.incompatibleProcols();
			return;
		}

		//Only processes messages sent by others
		if((peer.getID() != msg.getSenderId()) )
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
				handleGetchunk(msg.getFileId(),msg.getChunkNo(),null,null);
				break;

			case CHUNK:
				peer.getMessageRecord().addChunkMessage(msg.getFileId(), msg.getChunkNo());
				handleChunk(msg.getFileId(), msg.getChunkNo(), msg.getBody());
				break;

			case DELETE:
				handleDelete(msg.getFileId());
				break;

			case REMOVED:
				handleRemoved(msg.getFileId(),msg.getChunkNo(),msg.getSenderId());
				break;
			case GETINITIATOR:
				handleGetInitiator(msg.getFileId());
				break;
			case INITIATOR:
				//record this message (INITIATOR) at 'MessageRecord'
				peer.getMessageRecord().addInitiatorMessage(msg.getFileId(), msg.getSenderId());
				break;		
			case GETCHUNKENH:
				handleGetchunk(msg.getFileId(),msg.getChunkNo(),msg.getAddress(),msg.getPort());
				break;

			case GOTCHUNKENH:
				peer.getMessageRecord().addChunkMessage(msg.getFileId(), msg.getChunkNo());
				break;

			default:
				break;

			}
		}
	}

	/**
	 * Check protocols compatibility
	 * Compares the version of the peer and of the message
	 * @Returns true if protocols are compatible, false otherwise
	 */
	private boolean protocolsCompatible() {
		char[] receptor = peer.getVersion();
		char[] sender = msg.getVersion();

		for(int i = 0; i < receptor.length; i++)
			if(receptor[i] != sender[i])
				return false;
		return true;
	}

	/**
	 * Peer response to other peer PUTCHUNK message.
	 * The peer will store the chunk if it has space on its disk and if it doesn't have the chunk already stored.
	 * 
	 * Enhancement: If the conditions are pleased for the chunk to be stored, the peer will gather all the peers
	 * that had stored the same chunk (previously and after receiving the message) and will check if the
	 * number of peers (replication of the chunk) is bellow the desired. If it is, the peer will store the chunk,
	 * otherwise, it will not be stored, ensuring the desired replication degree of that chunk and preventing space
	 * occupation.
	 * 
	 * @param fileId - File identification
	 * @param chunkNo - Chunk identification number
	 * @param repDegree - Chunk desirable chunk replication degree
	 * @param body - Chunk content
	 */
	private synchronized void handlePutchunk(String fileId, int chunkNo, int repDeg, byte[] body)
	{
		boolean enhancement = peer.isEnhancement();

		//Owner of the file with file id
		if(peer.getRecord().checkStoredChunk(fileId, chunkNo) != null)
			return;
		
		Chunk c = new Chunk(fileId, chunkNo, body);

		//create response message : STORED
		Message msg = new Message(Util.MessageType.STORED,peer.getVersion(),peer.getID(),c.getFileId(),c.getChunkNo());

		//verifies chunk existence in this peer
		boolean alreadyExists = peer.getRecord().checkMyChunk(fileId, chunkNo);

		/*
		 * If the peer doesn't have available space, it will try to free some
		 * by releasing chunks with the replication degree above average
		 */
		if(!peer.getFileManager().hasSpaceAvailable(c) && !alreadyExists)
			evictChunks();

		//verifies again (after evicting chunks) if has space available
		if(peer.getFileManager().hasSpaceAvailable(c))
		{
			/*
			 * If the peer already stored the chunk, it will warn immediately the multicast channel.
			 * By doing this, another peer that is pondering on storing the chunk,
			 * can be updated much faster about the actual replication of the chunk.
			 */
			if(alreadyExists)
			{
				peer.getMc().send(msg);
				Logs.sentMessageLog(msg);
			}
			else
			{
				//Waits a random time
				Util.randomDelay();

				int actualRep = 0;

				//Get replication degree recorded before the peer started to process the putchunk message
				ArrayList<Integer> peersWithChunk = peer.getMessageRecord().getPeersWithChunk(fileId, chunkNo);

				if(peersWithChunk != null)
					actualRep = peersWithChunk.size();

				//ENHANCEMENT : If the replication degree is lower that the desired
				if(!(actualRep >= repDeg && enhancement))
				{							
					//send STORED message
					peer.getMc().send(msg);
					Logs.sentMessageLog(msg);

					//save chunk in memory
					peer.getFileManager().saveChunk(c);

					//Save info on 'Record' 
					peer.getRecord().addToMyChunks(fileId, chunkNo, repDeg);

					//Update Actual Replication Degree
					peer.getRecord().setPeersOnMyChunk(fileId, chunkNo, peersWithChunk);
					peer.getRecord().addPeerOnMyChunk(fileId, chunkNo, peer.getID());

				}
				else {
					//only keeps the ones referred
					peer.getMessageRecord().removeStoredMessages(fileId, chunkNo);
				}
				peer.getMessageRecord().removeStoredMessages(fileId, chunkNo);
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

			//Send message to the multicast to warn the other peers so they can update their replication degree of the chunk
			Message msg = new Message(MessageType.REMOVED,peer.getVersion(),peer.getID(),chunks.get(i).getFileId(),chunks.get(i).getChunkNo());
			peer.getMc().send(msg);
			Logs.sentMessageLog(msg);

			//Deletes the chunk from the peers disk
			String filename = chunks.get(i).getChunkNo() + chunks.get(i).getFileId();
			peer.getFileManager().deleteFile(filename);
		}
	}

	/**
	 * Peer response to other peer STORE message.
	 * The peer will record the peers that stored the chunks of the files that he backup.
	 * The peer will update the peers that stored the chunks that he also stored.
	 * 
	 * @param fileId - File identification
	 * @param chunkNo - Chunk identification number
	 * @param senderId - Sender peer identification number
	 */
	private synchronized void handleStore(String fileId, int chunkNo, int senderId){

		//Updates the Replication Degree if the peer has the chunk
		peer.getRecord().addPeerOnMyChunk(fileId,chunkNo,senderId);

		//Record the storedChunks in case the peer is the OWNER of the backup file
		peer.getRecord().recordStoredChunk(fileId, chunkNo, senderId);
	}	

	/**
	 * Peer response to other peer GETCHUNK message.
	 * If the peer has stored the chunkNo of the fileId, it will send the chunk content
	 * to the multicast (without enhancement) or to a private channel with the owner of the file (with enchancement)
	 * in a message of type CHUNK.
	 * 
	 * Enhancement: The peer creates a new channel to communicate with the peer who sent the GETCHUNK message.
	 * The message will be sent by this channel. This way, it will prevent the flow of big amounts of data
	 * (chunks content) in the multicast where the destination is just one peer. Without the enhancement, peers who
	 * aren't interested in this kind of information will dedicate time of their system to parse and receive this
	 * message.
	 * 
	 * @param fileId - File identification
	 * @param chunkNo - Chunk identification number
	 * @param address - Address to send the message with the chunk in case the enhancement is activated
	 * @param port - Port ti send the message with the chunk in case the enhancement is activated
	 */
	private synchronized void handleGetchunk(String fileId, int chunkNo, String address, Integer port){

		try{

			//peer has chunk stored
			if(peer.getRecord().checkMyChunk(fileId, chunkNo))
			{
				DatagramListener sendChunkChannel = null;

				//Creates a private channel with the sender in case of ENHANCEMENTS
				if(address != null && port != null && peer.isEnhancement()){
					sendChunkChannel = new DatagramListener(InetAddress.getLocalHost(),peer);
					sendChunkChannel.start();
				}

				byte[] body = peer.getFileManager().getChunkContent(fileId, chunkNo);

				//create CHUNK message
				Message msg = new Message(Util.MessageType.CHUNK,peer.getVersion(),peer.getID(),fileId,chunkNo,body);

				//Waits random time
				Util.randomDelay();

				//If meanwhile the chunk content wasn't sent to the sender by another peer
				if(!peer.getMessageRecord().receivedChunkMessage(fileId, chunkNo))
				{
					if(peer.isEnhancement()) {
						//send the chunk to the private channel
						sendChunkChannel.send(msg,InetAddress.getByName(address),port);	
					} 
					else{
						//send the chunk to the multicast
						peer.getMdr().send(msg);	
					}

					Logs.sentMessageLog(msg);
				}
				
				if(sendChunkChannel != null)
					sendChunkChannel.destroy();
			}
		}
		catch (UnknownHostException e) {
			Logs.exception("handleGetchunk", "MessageHandler", e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * Peer response to other peer CHUNK message.
	 * If the peer is able to restore the file identified by fileId it will record the chunks content
	 * on Record of the peer.
	 * 
	 * Enhancement: It will send a message of type GOTCHUNKENH to the multicast to warn all
	 * peers that it has received the chunks content that he asked for previously. This is the
	 * only way, for the peers, to know that the chunk was already sent, because with the restore
	 * enhancement, the chunk is sent by a private channel between the peer who sent the GETCHUNKENH
	 * and the peer who sends the CHUNK message.
	 * 
	 * @param fileId - File identification
	 * @param chunkNo - Chunk identification number
	 * @param body - Chunks content
	 */
	private synchronized void handleChunk(String fileId, int chunkNo, byte[] body){
		//chunk message received by initiator peer 
		FileInfo info = peer.getRecord().getRestoredFileInfoById(fileId);

		//This peer is able to restore the file
		if(info != null){
			//record chunk as restored
			if(peer.getRecord().recordRestoredChunk(fileId,chunkNo,body))
				Logs.chunkRestored(chunkNo);

			//enhancement
			if(peer.isEnhancement()){
				//sends message GOTCHUNKENH to the multicast
				Message msg = new Message(Util.MessageType.GOTCHUNKENH,peer.getVersion(),peer.getID(), fileId, chunkNo);
				peer.getMc().send(msg);
				Logs.sentMessageLog(msg);
			}
		}
	}


	/**
	 * Peer response to other peer DELETE message.
	 * Deletes all the chunks stored for the file with the
	 * fileId identification.
	 * 
	 * @param fileId - File identification
	 */
	private synchronized void handleDelete(String fileId){
		//verifies if the current peer has chunks stored from this file
		if(peer.getRecord().myChunksBelongsToFile(fileId))
		{
			//deletes chunks from disk
			peer.getFileManager().deleteChunks(fileId);	
			//remove from record
			peer.getRecord().deleteMyChunksByFile(fileId);
		}
	}


	/**
	 * Peer response to other peer REMOVED message.
	 * If the peer has stored/backup this chunkNo of the fileId,
	 * it will update it's replication degree and peers.
	 * If the actual replication degree drops bellow the desired, a chunk
	 * backup protocol is initiated after a random time and if it didn't
	 * received a PUTCHUNK message for the same fileId and chunkNo meanwhile.
	 * 
	 * @param fileId - File identification
	 * @param chunkNo - Chunk identification number
	 * @param peerNo - Peer who removed the chunk from his disk
	 */
	private synchronized void handleRemoved(String fileId, int chunkNo, int peerNo){

		Record record = peer.getRecord();
		FileInfo info = record.getBackupFileInfoById(fileId);

		byte[] data = null;
		int repDegree = 0;
		int desiredRepDegree = 0;
		boolean hasChunk = false;

		//This peer initiated the backup of this file (with fileId received)
		if(record.checkStoredChunk(fileId, chunkNo) != null && info != null)
		{			
			//Update stored record
			record.deleteStored(fileId, chunkNo, peerNo);
			desiredRepDegree = info.getNumChunks();

			//Actual replication degree
			ArrayList<Integer> peersWithChunk = record.checkStoredChunk(fileId, chunkNo);
			if(peersWithChunk != null)
				repDegree = peersWithChunk.size();

			if(repDegree < desiredRepDegree){
				//Get data of the chunk
				ArrayList<Chunk> chunks = peer.getFileManager().splitFileInChunks(info.getPath());
				Chunk c = chunks.get(chunkNo);
				data = c.getData();
			}
		}
		//Not Owner but has the chunk stored		
		else if(peer.getRecord().checkMyChunk(fileId, chunkNo))
		{
			//remove peer from 'Record'
			peer.getRecord().remPeerWithMyChunk(fileId, chunkNo, peerNo);

			//get data of the chunk
			data = peer.getFileManager().getChunkContent(fileId, chunkNo);
			repDegree = peer.getRecord().getMyChunk(fileId, chunkNo).getAtualRepDeg();
			desiredRepDegree = peer.getRecord().getMyChunk(fileId, chunkNo).getReplicationDeg(); 
			
			hasChunk = true;
		}

		/*
		 * If replicaiton degree is bellow desired it will start the chunkbackup protocol
		 * only if after a random time it doesn't received any putchunk for the same fileId and chunkNo
		 */
		if(repDegree < desiredRepDegree){
			peer.getMessageRecord().removePutChunkMessages(fileId, chunkNo);	//reset recording
			peer.getMessageRecord().startRecordingPutchunks(fileId);	//start record

			Util.randomDelay();

			if(!peer.getMessageRecord().receivedPutchunkMessage(fileId, chunkNo)){

				//sends PUTCHUNK message
				Message msg = new Message(MessageType.PUTCHUNK,peer.getVersion(),peer.getID(),fileId,chunkNo,repDegree,data);
				//Logs.sentMessageLog(msg);
				new ChunkBackupProtocol(peer.getMdb(), peer.getMessageRecord(), msg).start();
				
				
				if(hasChunk){
					Util.randomDelay();
					
					peer.getMessageRecord().addStoredMessage(fileId, chunkNo, peer.getID());
					//Warns the peers that it also has the chunk
					msg = new Message(MessageType.STORED,peer.getVersion(),peer.getID(),fileId,chunkNo);
					peer.getMc().send(msg);
				}
			}
		}
	}
	
	/**
	 * Peer response to other peer message GETINITIATOR
	 * 
	 * If the peer has initiated the backup of the file with the fileId received, 
	 * it will send a message of type 'INITIATOR' through the multicast channel.
	 * @param fileId
	 */
	private void handleGetInitiator(String fileId) 
	{
		//if myChunks contains this fileId, must send INITIATOR message
		if(peer.getRecord().getBackupFileInfoById(fileId) != null)
		{
			Message msg = new Message(MessageType.INITIATOR,peer.getVersion(),peer.getID(),fileId);
			Util.randomDelay();
			peer.getMc().send(msg);
			Logs.sentMessageLog(msg);
		}
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
