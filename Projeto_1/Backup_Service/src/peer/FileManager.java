package peer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import resources.Util;

import javax.xml.bind.DatatypeConverter;

import resources.Logs;

public class FileManager implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String diskDIR = null;
	private final int CHUNKLENGTH = 64*1000;
	private int peerID = -1;
	private int totalSpace = 0;
	private int remaingSpace = 0;

	FileManager(int peerId, int totalSpace){
		this.peerID = peerId;
		this.totalSpace = totalSpace;
		this.remaingSpace = this.totalSpace;

		diskDIR = Util.PEERS_DIR;
		File dir = new File(new String(diskDIR));
		if(!(dir.exists() && dir.isDirectory()))
		{
			dir.mkdir();
		}
		diskDIR += "Peer"+ peerId;
		dir = new File(new String(diskDIR));
		if(!(dir.exists() && dir.isDirectory()))
		{
			dir.mkdir();
		}
		
		dir = new File(new String(diskDIR + Util.CHUNKS_DIR));
		if(!(dir.exists() && dir.isDirectory()))
		{
			dir.mkdir();
		}
		
		dir = new File(new String(diskDIR + Util.RESTORES_DIR));
		if(!(dir.exists() && dir.isDirectory()))
		{
			dir.mkdir();
		}

	}

	public ArrayList<Chunk> splitFileInChunks(String filename) 
	{
		ArrayList<Chunk> chunkList = new ArrayList<>();	//list of chunks created for this file
		File file = new File(filename);	//open file

		//verifies file existence
		if(file.exists())
		{
			try 
			{
				String fileID = getFileID(file);
				int numChunks = (int) (file.length() / CHUNKLENGTH) + 1; 
				byte[] bytes = Files.readAllBytes(file.toPath());
				int byteCount = 0;


				for(int i = 0; i < numChunks; i++)
				{
					int length = CHUNKLENGTH;

					if (i == numChunks-1) 
					{	
						length = (int) (bytes.length % CHUNKLENGTH);
					}
					byteCount = 0;
					byte[] data = new byte[length];

					for (int j = i*CHUNKLENGTH; j < CHUNKLENGTH*i+length; j++) 
					{
						data[byteCount] = bytes[j];
						byteCount++;
					}
					Chunk c = new Chunk(fileID, i, data);
					chunkList.add(c);
				}

				//Teste de clonagem de ficheiro
				/*byte[] clonedData = new byte[(int)file.length()];
				byteCount = 0;
				for (Chunk c : chunkList) {

					for (byte b : c.getData()) {
						clonedData[byteCount] = b;
						byteCount++;
					}
				}
				System.out.println("Writing test clone file");
				FileOutputStream fos = new FileOutputStream("test_file.png");
				fos.write(clonedData);
				fos.close();
				System.out.println("Test clone file written.");*/

			} 
			catch (NoSuchAlgorithmException e) 
			{
				e.printStackTrace();
			} 
			catch (IOException e) 
			{
				Logs.errorFindingFile(filename);
				e.printStackTrace();
			}

		}
		else
			Logs.errorOpeningFile(filename);

		return chunkList;
	}

	/*
	 * Search by filename
	 */
	public int getFileNumChunks(String filename)
	{
		File file = new File(filename);
		if(file.exists())
		{
			return (int) (file.length() / CHUNKLENGTH) + 1;
		}	
		return -1;
	}

	public String getFileIdFromResources(String filename) throws NoSuchAlgorithmException
	{
		File file = new File(filename);
		if(file.exists())
			return getFileID(file);
		return null;
	}

	private String getFileID(File file) throws NoSuchAlgorithmException
	{
		//filename, last modification, ownwer
		String textToEncrypt = file.getName() + file.lastModified() + peerID;	
		return sha256(textToEncrypt);
	}

	private String sha256(String textToEncrypt) throws NoSuchAlgorithmException
	{
		MessageDigest sha = MessageDigest.getInstance("SHA-256");
		byte[] hash = sha.digest(textToEncrypt.getBytes());

		return DatatypeConverter.printHexBinary(hash);
	}

	//Receives a fileNo and chunkNo
	public boolean chunkExists(String fileNo, int chunkNo){
		String chunkName = createChunkName(fileNo,chunkNo);
		File file = new File(chunkName);

		return (file.exists() && file.isFile());
	}

	public void save(Chunk c)
	{
		byte data[] = c.getData();
		FileOutputStream out;
		try 
		{
			out = new FileOutputStream(createChunkName(c.getFileId(),c.getChunkNo()));
			out.write(data);
			out.close();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		remaingSpace -= data.length;
	}

	public byte[] getChunkContent(String fileNo,int chunkNo){
		String chunkName = createChunkName(fileNo,chunkNo);
		File file = new File(chunkName);
		byte[] data = null;

		if(file.exists() && file.isFile()){
			FileInputStream in;
			data = new byte[(int) file.length()];
			try {
				in = new FileInputStream(chunkName);
				in.read(data);
				in.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return data;
	}

	public boolean fileExists(File file){
		return file.exists();
	}

	public boolean hasSpaceAvailable(Chunk c){
		return (c.getData().length <= remaingSpace);
	}

	private String createChunkName(String fileNo, int chunkNo){
		return new String(diskDIR + Util.CHUNKS_DIR + chunkNo+ fileNo);
	}

	public void restoreFile(String filename, HashMap<Integer, byte[]> restores) throws IOException
	{
		FileOutputStream out = new FileOutputStream(diskDIR + Util.RESTORES_DIR +filename);

		for (int i = 0; i < restores.size(); i++) 
		{			
			//search for chunks from 0 to size
			if(restores.containsKey(new Integer(i)))
			{
				byte data[] = restores.get(new Integer(i));
				out.write(data);
			}
			else{
				Logs.errorRestoringFile(filename);
			}
		}
		
		out.close();
	}

	public void deleteChunks(String fileId) 
	{
		File dir = new File(diskDIR + Util.CHUNKS_DIR);
		if(dir.exists() && dir.isDirectory())
		{
			File[] files = dir.listFiles();
			
			for(File file : files)
			{
				String filename = file.getName();
				String fileIdCalc = filename.substring(1,filename.length());
				if(fileIdCalc.equals(fileId))
				{
					System.out.println(fileIdCalc);
					try {
						Files.delete(file.toPath());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public int memoryToRelease(int newTotalSpace){
		int needRelease = newTotalSpace - (totalSpace - remaingSpace);
		
		System.out.println("NewTotal Space: " + newTotalSpace);
		System.out.println("Need release: " + needRelease);
		System.out.println("Total space: " + totalSpace);
		System.out.println("Remaing Space: " + remaingSpace);
		
		if(needRelease < 0)
			return needRelease;
		else
			return 0;	
	}
	
	public ArrayList<String> deleteNecessaryChunks(int spaceToReclaim){
		
		ArrayList<String> chunksDeleted = new ArrayList();
		
		File dir = new File(diskDIR + Util.CHUNKS_DIR);
		if(dir.exists() && dir.isDirectory())
		{
			File[] files = dir.listFiles();
			
			for(File file : files)
			{
				spaceToReclaim -= file.getTotalSpace();
				String filename = file.getName();
				String fileId = filename.substring(1,filename.length());	//TMP - JUST 4 PRINT
				Integer chunkNo = Integer.parseInt(filename.substring(0,1));
				
				System.out.println("FILEID: " + fileId);
				System.out.println("CHUNKNO: " + chunkNo);
				
				//try {
				
					chunksDeleted.add(filename); //chunkNo + fileId
						
					//Files.delete(file.toPath());
					
				/*} catch (IOException e) {
					e.printStackTrace();
				}*/
				
				if(spaceToReclaim <= 0)
					break;
			}
			
		}
		
		return chunksDeleted;
	}
		  
	
}
