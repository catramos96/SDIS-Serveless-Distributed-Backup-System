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

public class FileManager{	
	
	private String diskDIR = Util.PEERS_DIR;;
	private int peerID = -1;
	private int totalSpace = Util.DISK_SPACE_DEFAULT;
	private int remaingSpace = Util.DISK_SPACE_DEFAULT;
	
	
	FileManager(int id, int totalSpace, int remaingSpace){
		this.peerID = id;
		this.totalSpace = totalSpace;
		this.remaingSpace = remaingSpace;
		checkDirectories();	
	}

	/*
	 * BACKUP
	 */

	public ArrayList<Chunk> splitFileInChunks(String filename) 
	{
		ArrayList<Chunk> chunkList = new ArrayList<>();	//list of chunks created for this file
		File file = new File(filename);	//open file

		//verifies file existence
		if(file.exists())
		{
			try 
			{
				String fileID = hashFileId(file);
				int numChunks = (int) (file.length() / Util.CHUNK_MAX_SIZE) + 1; 
				byte[] bytes = Files.readAllBytes(file.toPath());
				int byteCount = 0;


				for(int i = 0; i < numChunks; i++)
				{
					int length = Util.CHUNK_MAX_SIZE;

					if (i == numChunks-1) 
					{	
						length = (int) (bytes.length % Util.CHUNK_MAX_SIZE);
					}
					byteCount = 0;
					byte[] data = new byte[length];

					for (int j = i*Util.CHUNK_MAX_SIZE; j < Util.CHUNK_MAX_SIZE*i+length; j++) 
					{
						data[byteCount] = bytes[j];
						byteCount++;
					}
					Chunk c = new Chunk(fileID, i, data);
					chunkList.add(c);
				}

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

	private String hashFileId(File file) throws NoSuchAlgorithmException
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

	public void saveChunk(Chunk c)
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

	/*
	 * RESTORE
	 */

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

	/*
	 * DELETE
	 */

	public void deleteChunks(String fileId) 
	{
		File[] files = getFilesFromDirectory(diskDIR + Util.CHUNKS_DIR);
		
		if(files != null)
		{
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

	/*
	 * RECALIMING
	 */

	public int memoryToRelease(int newTotalSpace){
		int needRelease = newTotalSpace - (totalSpace - remaingSpace);

		System.out.println("NewTotal Space: " + newTotalSpace);
		System.out.println("Need release: " + needRelease);
		System.out.println("Total space: " + totalSpace);
		System.out.println("Remaing Space: " + remaingSpace);

		if(needRelease < 0)
			return -needRelease;
		else
			return 0;	
	}
	
	public ArrayList<String> deleteNecessaryChunks(int spaceToFree){
		
		ArrayList<String> chunksDeleted = new ArrayList();
		
		int spaceReleased = 0;
		
		File dir = new File(diskDIR + Util.CHUNKS_DIR);
		if(dir.exists() && dir.isDirectory())
		{
			File[] files = dir.listFiles();
			
			for(File file : files)
			{
				remaingSpace += file.length();	//Atualiza o espa�o dispon�vel
				spaceReleased += file.length();
				
				String filename = file.getName();
				String fileId = filename.substring(1,filename.length());	//TMP - JUST 4 PRINT
				Integer chunkNo = Integer.parseInt(filename.substring(0,1));

				System.out.println("FILEID: " + fileId);
				System.out.println("CHUNKNO: " + chunkNo);
				System.out.println("SPACE RELEASED: " + spaceReleased);
				System.out.println("SPACE TO FREE: " + spaceToFree);
				
			
					chunksDeleted.add(filename); //chunkNo + fileId
					file.delete();
				
				
				if(spaceReleased >=  spaceToFree)
					break;
			}			
		}

		return chunksDeleted;
	}

	/*
	 * Gets e Sets
	 */
	public String getFileIdFromFilename(String filename) throws NoSuchAlgorithmException
	{
		File file = new File(filename);
		if(file.exists())
			return hashFileId(file);
		return null;
	}

	public int getFileNumChunks(String filename)
	{
		File file = new File(filename);
		if(file.exists())
		{
			return (int) (file.length() / Util.CHUNK_MAX_SIZE) + 1;
		}	
		return -1;
	}

	public byte[] getChunkContent(String fileNo,int chunkNo)
	{
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
	
	public int getTotalSpace(){
		return totalSpace;
	}
	
	public int getRemainingSpace(){
		return this.remaingSpace;
	}
	
	
	public void setTotalSpace(int NewSpace){
		
		int spaceInUse = totalSpace - remaingSpace;
		
		remaingSpace = NewSpace - spaceInUse;
		
		totalSpace = NewSpace;
		
		if(remaingSpace < 0)
			remaingSpace = totalSpace;
	}
	
	public File[] getFilesFromDirectory(String dirName)
	{
		File dir = new File(dirName);
		if(dir.exists() && dir.isDirectory())
		{
			return dir.listFiles();
		}
		return null;
	}

	public boolean fileExists(File file){
		return file.exists();
	}
	
	//Receives a fileNo and chunkNo
	public boolean chunkExists(String fileId, int chunkNo)
	{
		String chunkName = createChunkName(fileId,chunkNo);
		File file = new File(chunkName);

		return (file.exists() && file.isFile());
	}

	public boolean hasSpaceAvailable(Chunk c){
		return (c.getData().length <= remaingSpace);
	}

	/*
	 * OTHERS
	 */

	private String createChunkName(String fileNo, int chunkNo)
	{
		return new String(diskDIR + Util.CHUNKS_DIR + chunkNo+ fileNo);
	}
	
	private void checkDirectories(){
		System.out.println("PEERID" + peerID);
		
		File dir = new File(new String(diskDIR));
		if(!(dir.exists() && dir.isDirectory()))
		{
			dir.mkdir();
		}
		
		diskDIR += "Peer"+ peerID;
		System.out.println(diskDIR);
		
		dir = new File(new String(diskDIR));
		if(!(dir.exists() && dir.isDirectory()))
		{
			System.out.println("CRIAR DISK PEER");
			dir.mkdir();
		}

		dir = new File(new String(diskDIR + Util.CHUNKS_DIR));
		System.out.println(diskDIR + Util.CHUNKS_DIR);
		if(!(dir.exists() && dir.isDirectory()))
		{
			System.out.println("CRIAR DISK CHUNKS");
			dir.mkdir();
		}

		dir = new File(new String(diskDIR + Util.RESTORES_DIR));
		System.out.println(diskDIR + Util.RESTORES_DIR);
		if(!(dir.exists() && dir.isDirectory()))
		{
			System.out.println("CRIAR DISK RESTORE");
			dir.mkdir();
		}
		
		dir = new File(new String(diskDIR + Util.LOCAL_DIR));
		System.out.println(diskDIR + Util.LOCAL_DIR);
		if(!(dir.exists() && dir.isDirectory()))
		{
			System.out.println("CRIAR DISK LOCAL");
			dir.mkdir();
		}
	}

	public String checkPath(String path){
		if(!path.contains("/")){
			return new String(diskDIR + Util.LOCAL_DIR + path);
		}
		return path;
	}
}
