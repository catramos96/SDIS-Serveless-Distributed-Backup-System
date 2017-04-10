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
import java.util.HashMap;
import resources.Util;

import javax.xml.bind.DatatypeConverter;

import resources.Logs;

/**
 * Class responsible for handling all the interactions with the file system.
 * @attribute String diskDIR - actual directory of this peer
 * @attribute int peerId - id of the peer responsible for this file system
 * @attribute int totalSpace - space available for new chunks + space occupied by chunks
 * @attribute int remainingSpace - space available for new chunks
 */
public class FileManager {	

	public String diskDIR = Util.PEERS_DIR;
	private int peerID = -1;
	private int totalSpace = Util.DISK_SPACE_DEFAULT;
	private int remainingSpace = Util.DISK_SPACE_DEFAULT;

	/**
	 * Constructor
	 * @param id
	 * @param totalSpace
	 * @param remaingSpace
	 */
	FileManager(int id, int totalSpace, int remaingSpace){
		this.peerID = id;
		this.totalSpace = totalSpace;
		this.remainingSpace = remaingSpace;
		checkDirectories();	
	}

	/*
	 * BACKUP
	 */
	
	/**
	 * Split file in chunks
	 * @param filename
	 * @return
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
				Logs.exception("splitFileInChunks", "FileManager", e.toString());
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

	/**
	 * Encode the filename 
	 * @param file
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	private String hashFileId(File file) throws NoSuchAlgorithmException
	{
		//filename, last modification, ownwer
		String textToEncrypt = file.getName() + file.lastModified() + peerID;	
		return sha256(textToEncrypt);
	}

	/**
	 * Applies sha256 functions
	 * @param textToEncrypt
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	private String sha256(String textToEncrypt) throws NoSuchAlgorithmException
	{
		MessageDigest sha = MessageDigest.getInstance("SHA-256");
		byte[] hash = sha.digest(textToEncrypt.getBytes());

		return DatatypeConverter.printHexBinary(hash);
	}

	/**
	 * Save chunk in the disk and updates remaining space
	 * @param c
	 */
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
			Logs.exception("saveChunk", "FileManager", e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			Logs.exception("saveChunk", "FileManager", e.toString());
			e.printStackTrace();
		}

		remainingSpace -= data.length;
	}

	/*
	 * RESTORE
	 */

	/**
	 * Restore file from disk
	 * @param filename
	 * @param restores
	 * @throws IOException
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

	/**
	 * Delete file from disk
	 * @param path
	 */
	public void deleteFile(String path)
	{
		File file = new File(path);
		if(fileExists(file))
			try {
				Files.delete(file.toPath());
			} catch (IOException e) {
				Logs.exception("deleteFile", "FileManager", e.toString());
				e.printStackTrace();
			}
	}

	/**
	 * Delete chunks from disk
	 * @param fileId
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
					long length = file.length();
					try {
						Files.delete(file.toPath());
					} catch (IOException e) {
						Logs.exception("deleteChunks", "FileManager", e.toString());
						e.printStackTrace();
					}
					remainingSpace += length;
				}
			}
		}
	}

	/*
	 * RECALIMING
	 */

	/**
	 * Calculates the space needed to release in view of the memory that client wants to reclaim.
	 * @param newTotalSpace
	 * @return
	 */
	public int memoryToRelease(int newTotalSpace){
		int needRelease = newTotalSpace - (totalSpace - remainingSpace);

		/*
		 * DEBUG
		 * System.out.println("NewTotal Space: " + newTotalSpace);
		 * System.out.println("Need release: " + needRelease);
		 * System.out.println("Total space: " + totalSpace);
		 * System.out.println("Remaing Space: " + remainingSpace);
		 */

		if(needRelease < 0)
			return -needRelease;
		else
			return 0;	
	}

	/**
	 * Deletes chunks until it released enough memory
	 * It starts by deleting the chunks with the actual replication degree above
	 * the desired and next the normal chunks
	 * @param chunks
	 * @param spaceToFree
	 * @return
	 */
	public ArrayList<String> deleteNecessaryChunks(ArrayList<Chunk> chunks, int spaceToFree){

		ArrayList<String> chunksDeleted = new ArrayList<String>();

		int spaceReleased = 0;

		//Priority Chunks
		for(Chunk c : chunks){
			String filename = createChunkName(c.getFileId(), c.getChunkNo());

			File file = new File(filename);		
			if(file.exists()){
				remainingSpace += file.length();	//Updates free space
				spaceReleased += file.length();
				
				chunksDeleted.add(c.getChunkNo() + c.getFileId()); //chunkNo + fileId
				file.delete();
			}
			if(spaceReleased >=  spaceToFree)
				break;
		}

		//Remove the rest of Chunks
		if(spaceReleased <  spaceToFree){
			File dir = new File(diskDIR + Util.CHUNKS_DIR);
			if(!(dir.isDirectory() && dir.exists())){
				return chunksDeleted;
			}

			File[] files = dir.listFiles();

			for(File file : files)
			{
				remainingSpace += file.length();	//Updates free space
				spaceReleased += file.length();

				String filename = file.getName();
				
				/*
				 * DEBUG
				 * String fileId = filename.substring(1,filename.length());
				 * Integer chunkNo = Integer.parseInt(filename.substring(0,1));
				 * System.out.println("FILEID: " + fileId);
				 * System.out.println("CHUNKNO: " + chunkNo);
				 * System.out.println("SPACE RELEASED: " + spaceReleased);
				 * System.out.println("SPACE TO FREE: " + spaceToFree);
				 */

				chunksDeleted.add(filename); //chunkNo + fileId
				file.delete();

				if(spaceReleased >=  spaceToFree)
					break;
			}	
			return chunksDeleted;
		}
		return chunksDeleted;
	}

	/*
	 * Gets e Sets
	 */
	
	/**
	 * Calculates fileId from filename
	 * @param filename
	 * @return
	 */
	public String getFileIdFromFilename(String filename)
	{
		File file = new File(filename);
		if(file.exists())
			try {
				return hashFileId(file);
			} catch (NoSuchAlgorithmException e) {
				Logs.errorFileId(filename);
				e.printStackTrace();
			}
		return null;
	}

	/**
	 * Calculates number of chunks from filename
	 * @param filename
	 * @return
	 */
	public int getFileNumChunks(String filename)
	{
		File file = new File(filename);
		if(file.exists())
		{
			return (int) (file.length() / Util.CHUNK_MAX_SIZE) + 1;
		}	
		return -1;
	}

	/**
	 * Search at the file System for the chunk with the pair (ChunkNo, FileId) and retrieve its content.
	 * @param fileNo
	 * @param chunkNo
	 * @return
	 */
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
				Logs.exception("getChunkContent", "FileManager", e.toString());
				e.printStackTrace();
			} catch (IOException e) {
				Logs.exception("getChunkContent", "FileManager", e.toString());
				e.printStackTrace();
			}
		}
		return data;
	}

	/**
	 * Receiving a new TotalSpace, calculates the new RemainingSpace and updates this 2 values.
	 * @param NewSpace
	 */
	public void setTotalSpace(int NewSpace){
		int spaceInUse = totalSpace - remainingSpace;

		remainingSpace = NewSpace - spaceInUse;
		totalSpace = NewSpace;

		if(remainingSpace < 0)
			remainingSpace = totalSpace;
	}

	/**
	 * List all files from a directory
	 * @param dirName
	 * @return
	 */
	public File[] getFilesFromDirectory(String dirName){
		File dir = new File(dirName);
		if(dirExists(dir))
		{
			return dir.listFiles();
		}
		return null;
	}

	/**
	 * True if a file exists and its a file. False otherwise.
	 * @param file
	 * @return
	 */
	public boolean fileExists(File file){
		return file.exists() && file.isFile();
	}
	
	/**
	 * True if a directory exists. False otherwise.
	 * @param file
	 * @return
	 */
	public boolean dirExists(File dir){
		return dir.exists() && dir.isDirectory();
	}

	/*
	 * OTHERS
	 */

	/**
	 * Given a chunk, verifies if the file System has space to create a new File with the chunk's data.
	 * @param c
	 * @return
	 */
	public boolean hasSpaceAvailable(Chunk c){
		return (c.getData().length <= remainingSpace);
	}

	/**
	 * Create path for a new chunk
	 * @param fileNo
	 * @param chunkNo
	 * @return
	 */
	private String createChunkName(String fileNo, int chunkNo){
		return new String(diskDIR + Util.CHUNKS_DIR + chunkNo+ fileNo);
	}
	
	/**
	 * Create new directories for restores, chunks and local files of the peer.
	 */
	private void checkDirectories()
	{
		//init peer
		Logs.log("PEERID" + peerID);

		//init directories
		File dir = new File(new String(diskDIR));
		if(!dirExists(dir))
		{
			dir.mkdir();
		}

		//peer directory name
		diskDIR += "Peer"+ peerID;
		dir = new File(new String(diskDIR));
		if(!dirExists(dir))
		{
			Logs.creatingDir(diskDIR);
			dir.mkdir();
		}

		//chunk directory name
		String name = diskDIR + Util.CHUNKS_DIR;
		dir = new File(new String(name));
		if(!dirExists(dir))
		{
			Logs.creatingDir(name);
			dir.mkdir();
		}

		//restore directory name
		name = diskDIR + Util.RESTORES_DIR;
		dir = new File(new String(name));
		if(!dirExists(dir))
		{
			Logs.creatingDir(name);
			dir.mkdir();
		}

		//local files directory name
		name = diskDIR + Util.LOCAL_DIR;
		dir = new File(new String(name));
		if(!dirExists(dir))
		{
			Logs.creatingDir(name);
			dir.mkdir();
		}
	}

	/**
	 * Check if the given name is a path. 
	 * If the name is not a path, we assume that this file belongs to the local files of the disk.
	 * @param path
	 * @return
	 */
	public String checkPath(String name)
	{
		String[] parts = name.split("/+");

		if(parts.length == 1)
			return new String(diskDIR + Util.LOCAL_DIR + name);

		return name;
	}
	
	/*
	 * Gets and sets
	 */
	
	public int getTotalSpace(){
		return totalSpace;
	}

	public int getRemainingSpace(){
		return this.remainingSpace;
	}
}
