import java.io.IOException;

public class Server
{
	public static void main(String[] args) throws IOException
	{
		if (args.length != 1)
		{
			System.out.println("Usage: java Server <srvc_port>");
			return;
		}
		
		int port = Integer.parseInt(args[0]);
		
		new ServerThread(port).start();
	}
}