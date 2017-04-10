# SDIS - Distributed Backup Service #

### Compile in WINDOWS : ###

1. navigate to the project's root folder;
2. create a folder named *bin*;
3. open a terminal at root folder and type the following command

		"javac -d bin -sourcepath src -encoding ISO-8859-1 src/cli/TestApp.java src/peer/BackupSystem.java"

### Run the peer : ###

1. navigate to the project's *bin* folder;
2. open a terminal in that directory and type the following command

		"java peer.BackupSystem <version> <peer_id> <rmiObject> <mc_addr_and_port> <mdb_addr_and_port> <mdr_addr_and_port>"
    
where:

	<version> : 1.0 for non-enhancement version, other for enhancements
	<peer_id> : peer’s unique identifier
	<rmiObject> : rmi identifier
	<mc_addr_and_port> - IP address and of the control channel (*)
	<mdb_addr_and_port> – IP address of the backup data channel (*)
	<mdr_addr_and_port> - IP address of the restore data channel (*)
	
### Run the TestApp : ###

1. navigate to the project's *bin* folder;
2. open a terminal in that directory and type the following command

		"java cli.TestApp <rmiObject> <sub-protocol> <opnd_1> <opnd_2>"
    
where:

	<peer_app> : rmi identifier
	<sub-protocol> : BACKUP(ENH), RESTORE(ENH), RECLAIM(ENH), DELETE(ENH), STATE 
	<opnd_1> : path name (**) of the file to backup/restore/delete; the amount of space to reclaim (in KByte) 
	<opnd_2> : integer that specifies the desired replication degree and applies only to the backup protocol (or its enhancement) 
  
The STATE operation takes no operands.

### Directories : ###

* **peersDisk** : contains all the peers disk space using the same computer
  * **peerX** : where X stands for peer's unique identifier; contains all the sub directories and the metadata
    * **CHUNKS** : contains all the chunks stored by this peer.
    * **LOCAL** : contains local files inserted by the user.
    * **RESTORES** : contains all the restores made by this peer.
         
### NOTES : ###

(\*) the IP format is <address:port> and if the address is not specified we'll assume the address is the localhost address.        
(\**) in case of no path given, we'll assume that the file it's located at the local files directory
