package protocols;

import network.Message;
import network.MulticastListener;
import peer.Record;
import resources.Util;

public class ChunkRestoreProtocol extends Protocol{

	public ChunkRestoreProtocol(MulticastListener mc, Record record, Message msg){
		this.mc = mc;
		this.record = record;
		this.msg = msg;
	}

	@Override
	public void run()  
	{
		boolean end = false;
		int rep = 0;
		int waitingTime = Util.WAITING_TIME;
		String fileNo = msg.getFileId();
		int chunkNo = msg.getChunkNo();
		
		while(rep < Util.MAX_TRIES)	
		{
			System.out.println("Times :" + rep + " of " +  chunkNo);
			
			mc.send(msg);		//msg GETCHUNK
			
			try {
				Thread.sleep(waitingTime);
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(record.checkChunkRestored(fileNo, chunkNo)){
				end = true;
				System.out.println("restored chunk n: " + chunkNo);
				break;
			}
			
			//waitingTime *= Util.TIME_REINFORCEMENT;	//doubles time for each rep
			rep++;
		}
		if(!end){
			System.out.println("chunk no: " + chunkNo + " not restored");
		}
		/*
		String fileId;
		boolean restored = false;
		
		try	{
			filename = peer.fileManager.checkPath(filename);
			fileId = peer.fileManager.getFileIdFromFilename(filename);
			
			int chunks = peer.fileManager.getFileNumChunks(filename);

			//start recording chunk restores
			FileInfo info = new FileInfo(fileId,filename,chunks,1);			//1 - TMP -> REPLICATION DEGREE
			
			peer.record.startRecordRestores(info);

			//create and send message for each chunk
			for(int i = 0; i < info.getNumChunks(); i++)
			{
				Message msg = new Message(MessageType.GETCHUNK,peer.getVersion(),peer.getID(),info.getFileId(),i);
				Logs.sentMessageLog(msg);
				new ChunkRestoreProtocol(peer.getMc(),peer.getMulticastRecord(),msg).start();
			}
			
			long startTime = System.currentTimeMillis(); //fetch starting time
			
			while((System.currentTimeMillis()-startTime)<Util.MAX_AVG_DELAY_TIME)	
			{
			    if(peer.getMulticastRecord().allRestored(info)){
			    	peer.fileManager.restoreFile(info.getFilename(), peer.record.getRestores(info));
					Logs.fileRestored(info.getFilename());
					restored = true;
					break;
			    }
			}
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		peer.setMessage("restore");
		if(!restored)
			System.out.println("File Couldn't be restored");
		
		
		
	/*	//verifica de 100 em 100 ms se ja foram restaurados todos os chunks
		try{
			while(!peer.record.allRestored(info))
			{
				Thread.sleep(100);
			}
			//fileRestore
			peer.fileManager.restoreFile(info.getFilename(), peer.record.getRestores(info));
			Logs.fileRestored(info.getFilename());
		} 
		catch (InterruptedException e)
		{
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
		
	}
}
