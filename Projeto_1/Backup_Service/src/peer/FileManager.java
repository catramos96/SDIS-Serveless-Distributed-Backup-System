package peer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;

import javax.xml.bind.DatatypeConverter;

public class FileManager {
	
	private final String DIR = "resources/";
	private final int CHUNKLENGTH = 64*1000;
	
	FileManager(){}
	
	public Vector<Chunk> splitFileInChunks(String filename) throws IOException
	{
		Vector<Chunk> chunks = null;
		
		File file = new File(DIR+filename);
		
		if(file.exists())
		{
			try 
			{
				String fileID = getFileID(file);
				int numChunks = (int) (file.length() / CHUNKLENGTH) + 1; 
				
				byte[] bytes = Files.readAllBytes(file.toPath());
				
				
				
				
				for(int i = 0; i < numChunks; i++)
				{
					byte[] data = new byte[CHUNKLENGTH];
					
					//aqui -> colocar a informacao que vem do file
					
					//atencao ao ultimo chunk
					
					Chunk c = new Chunk(fileID, i, data);
					chunks.add(c);
				}
			} 
			catch (NoSuchAlgorithmException e) 
			{
				e.printStackTrace();
			}
			
		}
		else
			System.out.println("Error opening "+filename+" file.");
	
		return chunks;
	}
	
	private String getFileID(File file) throws NoSuchAlgorithmException
	{
		//filename, last modification, ownwer
		String textToEncrypt = file.getName() + file.lastModified() + Peer.getId();
	
		return sha256(textToEncrypt);
	}
	
	private String sha256(String textToEncrypt) throws NoSuchAlgorithmException
	{
		MessageDigest sha = MessageDigest.getInstance("SHA-256");
		byte[] hash = sha.digest(textToEncrypt.getBytes());
		
		return DatatypeConverter.printHexBinary(hash);
	}
	
}
