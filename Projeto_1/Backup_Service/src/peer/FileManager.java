package peer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import javax.xml.bind.DatatypeConverter;

import resources.Util;

public class FileManager {

	private final String DIR = "../resources/";
	private final String CHUNKSDIR = "/chunks/";
	private String diskDIR = null;
	private final int CHUNKLENGTH = 64*1000;
	private int peerID = -1;
	private int totalSpace = 0;
	private int remaingSpace = 0;

	FileManager(int peerId, int totalSpace){
		this.peerID = peerId;
		this.totalSpace = totalSpace;
		this.remaingSpace = this.totalSpace;

		diskDIR = new String("../peersDisk/");
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

	}

	public ArrayList<Chunk> splitFileInChunks(String filename) 
	{
		ArrayList<Chunk> chunkList = new ArrayList<>();	//list of chunks created for this file
		File file = new File(DIR+filename);	//open file

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
				System.out.println("File not found!");
			}

		}
		else
			System.out.println("Error opening "+filename+" file.");

		return chunkList;
	}
	
	/*
	 * Search by filename
	 */
	public int getFileNumChunks(String filename)
	{
		File file = new File(DIR + filename);
		if(file.exists())
		{
			return (int) (file.length() / CHUNKLENGTH) + 1;
		}	
		return -1;
	}

	public String getFileIdFromResources(String filename) throws NoSuchAlgorithmException
	{
		File file = new File(DIR + filename);
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
		//Verificar se ja existe o folder 'CHUNKS'
		File dir = new File(new String(diskDIR + CHUNKSDIR));
		if(!(dir.exists() && dir.isDirectory()))
		{
			dir.mkdir();
		}

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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

	public boolean fileExists(File file)
	{
		return file.exists();
	}

	public boolean hasSpaceAvailable(Chunk c)
	{
		return (c.getData().length <= remaingSpace);
	}
	
	private String createChunkName(String fileNo, int chunkNo){
		return new String(diskDIR + CHUNKSDIR + chunkNo+ fileNo);
	}

}
