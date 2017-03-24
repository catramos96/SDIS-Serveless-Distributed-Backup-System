package network;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import resources.Util;
import peer.Chunk;
import peer.Peer;

public class MessageHandler extends Thread
{
	private Peer peer = null; //peer associado ao listener
	private Message msg = null;
	
	public MessageHandler(Peer peer,byte[] msg)
	{
		this.peer = peer;
		this.msg = parseMessage(msg);
	}

	public void run() 
	{
		/*Don't process messages that were sent by himself*/
		if(peer.getID() != msg.getSenderId())
		{
			switch (msg.getType()) {
			case PUTCHUNK:
				System.out.println(msg.getSenderId() + " - PUTCHUNK "+ msg.getChunkNo());
				//A peer must never store the chunks of its own files
				if(peer.getID() != msg.getSenderId())
				{
					Chunk c = new Chunk(msg.getFileId(), msg.getChunkNo(), msg.getBody());
					c.setReplicationDeg(msg.getReplicationDeg());
					peer.receivedPutchunk(c);
				}
				break;
			case STORED:
				System.out.println(msg.getSenderId() + " - STORED "+ msg.getChunkNo());
				peer.storeAction(msg.getFileId(), msg.getChunkNo(),msg.getSenderId());	
				break;
			case GETCHUNK:
				//restoreProt.executeProtocolAction();
				break;
			case CHUNK:
				//spaceReclProt.executeProtocolAction();
				break;
			case DELETE:
				//deleteProt.executeProtocolAction();
				break;
			case REMOVED:
				
				break;
			default:
				break;
			}
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
