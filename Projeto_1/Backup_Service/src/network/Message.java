package network;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

import resources.Util;

public class Message 
{
	private Util.MessageType type = null;
	private char[] version;
	private int senderId = -1;
	private String fileId = null;
	private int chunkNo = -1;
	private int replicationDeg = -1;
	//Enhancement messages attributes
	private String address = null;
	private int port = -1;
	
	private byte[] body = null;
	
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
	 * Enhancement Messages:
	 * GETCHUNKENH <Version> <SenderId> <FileId> <ChunkNo> <Address> <Port> <CRLF><CRLF>
	 * GOTCHUNKENH <Version> <SenderId> <FileId> <ChunkNo>					<CRLF><CRLF>
	 */
	
	public Message(Util.MessageType type, char[] version, int senderId, String fileId, int chunkNo, int ReplicationDeg, byte[] body)
	{
		if(!type.name().equals("PUTCHUNK"))
			System.out.println("Wrong Constructor putchunk");
		this.type = type;
		this.version = version;
		this.senderId = senderId;
		this.fileId = fileId;
		this.chunkNo = chunkNo;
		this.replicationDeg = ReplicationDeg;
		this.body = body;
	}
	
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

	public Message(Util.MessageType type, char[] version, int senderId, String fileId) {
		if(!type.name().equals("DELETE"))
			System.out.println("Wrong Constructor delete");
		else
		{
			this.type = type;
			this.version = version;
			this.senderId = senderId;
			this.fileId = fileId;
		}
	}
	
	/*
	 * Enhancement Message
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
	 * Create new message
	 * @return
	 */
	public byte[] buildMessage() {
		
		String content = type.name() + " " + version[0]+version[1]+version[2] + " " + senderId + " " + fileId + " ";
		
		if(type.compareTo(Util.MessageType.DELETE) != 0)
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			byte[] a = baos.toByteArray();
			
			return a;
		}
		
		return content.getBytes();
	}
	
	/*
	 * Getters and Setters
	 */

	public Util.MessageType getType() {
		return type;
	}

	public void setType(Util.MessageType type) {
		this.type = type;
	}

	public int getChunkNo() {
		return chunkNo;
	}

	public void setChunkNo(int chunkNo) {
		this.chunkNo = chunkNo;
	}

	public int getReplicationDeg() {
		return replicationDeg;
	}

	public void setReplicationDeg(int replicationDeg) {
		this.replicationDeg = replicationDeg;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}

	public int getSenderId() {
		return senderId;
	}

	public void setSenderId(int senderId) {
		this.senderId = senderId;
	}

	public char[] getVersion() {
		return version;
	}

	public void setVersion(char[] version) {
		this.version = version;
	}
	
	public String getAddress(){
		return address;
	}
	
	public void setAddress(String address){
		this.address = address;
	}
	
	public int getPort(){
		return port;
	}
	
	public void setPort(int port){
		this.port = port;
	}
	
	/* Fill the object 'Message'
	 * @param message
	 * @return
	 */
	public static Message parseMessage(byte[] message, char[] peerVersion)
	{
		Message parsed = null;

		ByteArrayInputStream stream = new ByteArrayInputStream(message);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

		try
		{
			String header = reader.readLine();	//a primeira linha corresponde a header

			//interpretacao da header
			String[] parts = header.split("\\s");

			Util.MessageType type_rcv = validateMessageType(parts[0]);

			//common
			char[] version_rcv = validateVersion(parts[1],peerVersion);
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
			
			//getchunkenh
			String address_rcv = null;
			Integer port_rcv = null;
			if(type_rcv.compareTo(Util.MessageType.GETCHUNKENH) == 0){
				address_rcv = parts[5];
				port_rcv = Integer.parseInt(parts[6]);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return parsed;
	}
	
	/*
	 * Validates
	 */

	private static char[] validateVersion(String string, char[] peerVersion) 
	{
		char[] vs = string.toCharArray();
		if(vs[0] == peerVersion[0] && vs[1] == peerVersion[1] && vs[2] == peerVersion[2])
			return vs;

		return null;	//deve retornar um erro
	}
	
	private static Util.MessageType validateMessageType(String string) 
	{
		//nao sei se ha restricoes aqui
		return Util.MessageType.valueOf(string);
	}
	
}
