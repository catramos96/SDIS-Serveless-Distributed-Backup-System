package network;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import network.Message.MessageType;
import peer.Chunk;
import peer.Peer;

public class MessageHandler 
{
	private Peer peer; //peer associado ao listener
	
	public MessageHandler(Peer peer)
	{
		this.peer = peer;
	}

	public void processMessage(byte[] message) 
	{
		Message received = parseMessage(message);
		
		switch (received.getType()) {
		case PUTCHUNK:
			System.out.println("PUTCHUNK "+ received.getChunkNo());
			//A peer must never store the chunks of its own files
			if(peer.getID() != received.getSenderId())
			{
				Chunk c = new Chunk(received.getFileId(), received.getChunkNo(), received.getBody());
				peer.putchunkAction(c);
			}
			break;
		case STORED:
			System.out.println("STORED "+ received.getChunkNo());
			peer.storeAction();
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
			
			MessageType type_rcv = validateMessageType(parts[0]); 
			char[] version_rcv = validateVersion(parts[1]);
			int senderId_rcv = Integer.parseInt(parts[2]);
			String fileId_rcv = parts[3];
			int chunkNo_rcv = validateChunkNo(parts[4],type_rcv);
			int replicationDeg_rcv = validateReplicationDeg(parts[5],type_rcv);
			
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
		char[] peerVersion = peer.getVersion();
		if(vs[0] == peerVersion[0] && vs[1] == peerVersion[1])
			return vs;
		
		return null;	//deve retornar um erro
	}

	private MessageType validateMessageType(String string) 
	{
		//nao sei se ha restricoes aqui
		return MessageType.valueOf(string);
	}

	public Peer getPeer() {
		return peer;
	}

	public void setPeer(Peer peer) {
		this.peer = peer;
	}

}
