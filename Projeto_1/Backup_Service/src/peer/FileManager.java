package peer;

import java.io.File;
import java.util.Vector;

public class FileManager {
	
	private final String DIR = "resources/";
	
	FileManager(){}
	
	boolean removeFile(String filename){
		return false;
		
	}
	
	Vector<Chunk> splitFileInChunks(String filename)
	{
		Vector<Chunk> chunks = null;
		
		File file = new File(DIR+filename);
		
		if(file.exists())
		{
			
		}
		else
			System.out.println("erro a abrir ficheiro indicado");
	
		return chunks;
	}

}
