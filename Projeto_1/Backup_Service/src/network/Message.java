package network;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import resources.Logs;
import resources.Util;

/**
 * Class Message
 * Used to build and parse the messages received by the communication channels
 * 
 * @attribute type - Type of the message, it belongs to the enum MessageType
 * @attribute version - Version of the sender peer protocols
 * @attribute senderId - Sender peer identification number
 * @attribute fileId - File identification
 * @attribute chunkNo - Chunk identification number
 * @attribute replicationDeg - Desired replication degree of the chunk associated
 * @attribute address - Address of the sender peer (ENHANCEMENT)
 * @attribute port - Port of the sender peer (ENHANCEMENT)
 * @attribute body - Content of the chunk associated
 */
public class Message 
{
	//Message information
	private Util.MessageType type = null;
	private char[] version;
	private int senderId = -1;
	private String fileId = null;
	private int chunkNo = -1;
	private int replicationDeg = -1;
	private String address = null;		//For enhancement
	private int port = -1;				//For enhancement
	private byte[] body = null;
	
	//Special characters for message construction
	private static final char CR = 0xD;								
	private static final char LF = 0xA;
	public static final String LINE_SEPARATOR = "" + CR + LF;
	
	/**
	 * <MessageType> <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
	 * 
	 * PUTCHUNK	<Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> 	<CRLF><CRLF>	<Body>
	 * STORED 	<Version> <SenderId> <FileId> <ChunkNo> 					<CRLF><CRLF>
	 * GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> 					<CRLF><CRLF>
	 * CHUNK 	<Version> <SenderId> <FileId> <ChunkNo> 					<CRLF><CRLF>	<Body>
	 * DELETE 	<Version> <SenderId> <FileId> 								<CRLF><CRLF>
	 * REMOVED 	<Version> <SenderId> <FileId> <ChunkNo> 					<CRLF><CRLF>
	 * 
	 * Enhancement Messages:
	 * 
	 * GETCHUNKENH <Version> <SenderId> <FileId> <ChunkNo> <Address> <Port> <CRLF><CRLF>
	 * GOTCHUNKENH <Version> <SenderId> <FileId> <ChunkNo>					<CRLF><CRLF>
	 * GETINITIATOR <Version> <SenderId> <FileId>								<CRLF><CRLF>
	 * INITIATOR	<Version> <SenderId> <FileId>	
	 */
	
	/**
	 * Constructor of Message for the type PUTCHUNK
	 * @param type - Type of the message, it has to be a PUTCHUNK
	 * @param version - Version of the protocols
	 * @param senderId - Sender identification
	 * @param fileId - File identification
	 * @param chunkNo - Chunk identification number
	 * @param replicationDeg - Desired replication degree of the associated chunk
	 * @param body - Content of the associated chunk
	 */
	public Message(Util.MessageType type, char[] version, int senderId, String fileId, int chunkNo, int replicationDeg, byte[] body)
	{
		if(!type.name().equals("PUTCHUNK"))
			System.out.println("Wrong Constructor putchunk");
		this.type = type;
		this.version = version;
		this.senderId = senderId;
		this.fileId = fileId;
		this.chunkNo = chunkNo;
		this.replicationDeg = replicationDeg;
		this.body = body;
	}
	
	/**
	 * Constructor of Message for the types GETCHUNK, REMOVED, GOTCHUNKENH
	 * @param type - Type of the message, it has to be one of the types mentioned above
	 * @param version - Version of the protocols
	 * @param senderId - Sender identification
	 * @param fileId - File identification
	 * @param chunkNo - Chunk identification number
	 */
	public Message(Util.MessageType type, char[] version, int senderId, String fileId, int chunkNo)
	{
		if(!(type.name().equals("STORED")|| type.name().equals("GETCHUNK") || type.name().equals("REMOVED") || type.name().equals("GOTCHUNKENH")))
			System.out.println("Wrong Constructor stored/getchunk/removed");
		this.type = type;
		this.version = version;
		this.senderId = senderId;
		this.fileId = fileId;
		this.chunkNo = chunkNo;
	}
	
	/**
	 * Constructor of Message for the type CHUNK
	 * @param type - Type of the message, it has to be a CHUNK
	 * @param version - Version of the protocols
	 * @param senderId - Sender identification
	 * @param fileId - File identification
	 * @param chunkNo - Chunk identification number
	 * @param body - Content of the associated chunk
	 */
	public Message(Util.MessageType type, char[] version, int senderId, String fileId, int chunkNo, byte[] body)
	{
		if(!type.name().equals("CHUNK"))
			System.out.println("Wrong Constructor chunk");
		else
		{
			this.type = type;
			this.version = version;
			this.senderId = senderId;
			this.fileId = fileId;
			this.chunkNo = chunkNo;
			this.body = body;
		}
	}

	/**
	 * Constructor of Message for the types DELETE, GETINITIATOR, INITIATOR.
	 * @param type - Type of the message, it has to be one of the types mentioned above
	 * @param version - Version of the protocols
	 * @param senderId - Sender identification
	 * @param fileId - File identification
	 */
	public Message(Util.MessageType type, char[] version, int senderId, String fileId) {
		if(!(type.name().equals("DELETE") || type.name().equals("GETINITIATOR") || type.name().equals("INITIATOR")))
			System.out.println("Wrong Constructor delete/getinitiator/initiator");
		else
		{
			this.type = type;
			this.version = version;
			this.senderId = senderId;
			this.fileId = fileId;
		}
	}
	
	/**
	 * Constructor of Message for the type GETCHUNKENH
	 * @param type - Type of the message, it has to be a GETCHUNKENH
	 * @param version - Version of the protocols
	 * @param senderId - Sender identification
	 * @param fileId - File identification
	 * @param chunkNo - Chunk identification number
	 * @param address - Address of the sender
	 * @param port - Port of the sender
	 */
	public Message(Util.MessageType type, char[] version, int senderId, String fileId, int chunkNo, String address, int port){
		if(!type.name().equals("GETCHUNKENH"))
			System.out.println("Wrong Constructor delete");
		else
		{
			this.type = type;
			this.version = version;
			this.senderId = senderId;
			this.fileId = fileId;
			this.chunkNo = chunkNo;
			this.address = address;
			this.port = port;
		}
	}
	

	/**
	 * Creates a new message, depending on the attributes of the class object.
	 * 
	 * @return The message to be sent in byte[]
	 */
	public byte[] buildMessage() {
		
		String content = type.name() + " " + version[0]+version[1]+version[2] + " " + senderId + " " + fileId + " ";
		
		if(type.compareTo(Util.MessageType.DELETE) != 0 || type.compareTo(Util.MessageType.GETINITIATOR) != 0 || type.compareTo(Util.MessageType.INITIATOR) != 0 )
			content += chunkNo + " ";
		
		if(type.compareTo(Util.MessageType.PUTCHUNK) == 0)
			content += replicationDeg + " ";
		
		if(type.compareTo(Util.MessageType.GETCHUNKENH) == 0)
			content += address + " " + port + " ";
		
		content += LINE_SEPARATOR + LINE_SEPARATOR;
		
		if(type.compareTo(Util.MessageType.PUTCHUNK) == 0 || type.compareTo(Util.MessageType.CHUNK) == 0)
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try 
			{
				baos.write(content.getBytes());
				baos.write(body);
			} 
			catch (IOException e) 
			{
				Logs.exception("buildMessage", "Message", e.toString());
				e.printStackTrace();
			}
			
			byte[] a = baos.toByteArray();
			
			return a;
		}
		
		return content.getBytes();
	}
	
	/*
	 * GETS & SETS
	 */

	/**
	 * Function to get the type of the message.
	 * @return type - Message type
	 */
	public Util.MessageType getType() {
		return type;
	}

	/**
	 * Function to get the chunk number associated with the message.
	 * @return Chunk associated to the message
	 */
	public int getChunkNo() {
		return chunkNo;
	}

	/**
	 * Function to get the desired replication degree associated with the message.
	 * @return replicationDegree
	 */
	public int getReplicationDeg() {
		return replicationDeg;
	}

	/**
	 * Function to get the file identification associated with the message.
	 * @return File identification
	 */
	public String getFileId() {
		return fileId;
	}

	/**
	 * Function to get the chunk's content associated with the message.
	 * @return Chunks content.
	 */
	public byte[] getBody() {
		return body;
	}

	/**
	 * Function to get the sender identification associated with the message.
	 * @return Sender identification
	 */
	public int getSenderId() {
		return senderId;
	}

	/**
	 * Function to get the version of the protocols associated with the message.
	 * @return Protocols version
	 */
	public char[] getVersion() {
		return version;
	}
	
	/**
	 * Function to get the address of the sender associated with the message.
	 * @return Address
	 */
	public String getAddress(){
		return address;
	}
	
	/**
	 * Function to get the port of the sender associated with the message.
	 * @return Port
	 */
	public int getPort(){
		return port;
	}
	
	/**
	 * Receives a message in byte[] and parses it filling the respective attributes
	 * of a new Message object. It also checks if the actual peers version that is
	 * passed as a parameter (peerVersion) is compatible with the message protocol version.
	 * 
	 * @param message
	 * @param peerVersion
	 * @return Message Object
	 */
	public static Message parseMessage(byte[] message, char[] peerVersion)
	{
		Message parsed = null;

		ByteArrayInputStream stream = new ByteArrayInputStream(message);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

		try
		{
			String header = reader.readLine();
			String[] parts = header.split("\\s");

			Util.MessageType type_rcv = validateMessageType(parts[0]);

			//Common to all messages types
			char[] version_rcv = validateVersion(parts[1],peerVersion);
			int senderId_rcv = Integer.parseInt(parts[2]);
			String fileId_rcv = parts[3];

			//Exception for the types DELETE, GETINITIATOR, INITIATOR.
			int chunkNo_rcv = -1;
			if(type_rcv.compareTo(Util.MessageType.DELETE) != 0 && type_rcv.compareTo(Util.MessageType.GETINITIATOR) != 0 && type_rcv.compareTo(Util.MessageType.INITIATOR) != 0 )
				chunkNo_rcv = Integer.parseInt(parts[4]);

			//Exception for type PUTCHUNK
			int replicationDeg_rcv = -1;
			if(type_rcv.compareTo(Util.MessageType.PUTCHUNK) == 0){
				replicationDeg_rcv = Integer.parseInt(parts[5]);
			}
			
			//Exception for the type GETCHUNKENH
			String address_rcv = null;
			Integer port_rcv = null;
			if(type_rcv.compareTo(Util.MessageType.GETCHUNKENH) == 0){
				address_rcv = parts[5];
				port_rcv = Integer.parseInt(parts[6]);
			}

			//Removes the last sequences of white spaces (\s) and null characters (\0)
			//String msg_received = (new String(packet.getData()).replaceAll("[\0 \\s]*$", ""));
			int offset = header.length() + Message.LINE_SEPARATOR.length()*2;
			byte[] body = new byte[64000];
			System.arraycopy(message, offset, body, 0, 64000);

			//Creates the message with the respective attributes
			if(type_rcv.compareTo(Util.MessageType.DELETE) == 0 || type_rcv.compareTo(Util.MessageType.GETINITIATOR) == 0 || type_rcv.compareTo(Util.MessageType.INITIATOR) == 0)
				parsed = new Message(type_rcv,version_rcv,senderId_rcv,fileId_rcv);	
			else if(type_rcv.compareTo(Util.MessageType.GETCHUNK) == 0 || type_rcv.compareTo(Util.MessageType.STORED) == 0 || type_rcv.compareTo(Util.MessageType.REMOVED) == 0 || type_rcv.compareTo(Util.MessageType.GOTCHUNKENH) == 0)
				parsed = new Message(type_rcv,version_rcv,senderId_rcv,fileId_rcv,chunkNo_rcv) ;	
			else if(type_rcv.compareTo(Util.MessageType.PUTCHUNK) == 0)
				parsed = new Message(type_rcv,version_rcv,senderId_rcv,fileId_rcv,chunkNo_rcv,replicationDeg_rcv,body);
			else if(type_rcv.compareTo(Util.MessageType.CHUNK) == 0)
				parsed = new Message(type_rcv,version_rcv,senderId_rcv,fileId_rcv,chunkNo_rcv,body);
			else if(type_rcv.compareTo(Util.MessageType.GETCHUNKENH) == 0)
				parsed = new Message(type_rcv,version_rcv,senderId_rcv,fileId_rcv,chunkNo_rcv,address_rcv,port_rcv);

			reader.close();
			stream.close();
		} 
		catch (IOException e) 
		{
			Logs.exception("parseMessage", "Message", e.toString());
			e.printStackTrace();
		}

		return parsed;
	}
	
	/*
	 * Validations
	 */

	/**
	 * Validates the version of the message by comparing its version with the peer version.
	 * @param messageV - Version of the message
	 * @param peerVersion - Version of the peer
	 * @return Not null if the versions are compatible, null otherwise
	 */
	private static char[] validateVersion(String messageV, char[] peerVersion) 
	{
		char[] vs = messageV.toCharArray();
		if(vs[0] == peerVersion[0] && vs[1] == peerVersion[1] && vs[2] == peerVersion[2])
			return vs;

		return null;	
	}
	
	/**
	 * Validates the type of the message.
	 * @param type - Type of the message
	 * @return MessageType corresponding to the type, if it's null, then the type of the message is not valid.
	 */
	private static Util.MessageType validateMessageType(String type) 
	{
		return Util.MessageType.valueOf(type);
	}
	
}
