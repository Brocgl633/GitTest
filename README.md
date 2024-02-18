package iit_550_self;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientInformation {

    private static final int Client_SOCKET_PORT = 12233;

    private static final int Server_SOCKET_PORT = 22233;

    private static final String REPLICA_LOCATION = "backupfiles/";

    /***
     * key: client number # ip # timestamp
     * value: [the list of file registered at timestamp on client number and ip]
     * eg. {1#127.0.0.1#155121=[a.txt, desktop.ini, 图标 - 副本 (2).jpg, 图标 - 副本 (3).jpg, 图标 - 副本 (4).jpg, 图标 - 副本.jpg, 图标.jpg]}
     */
    private static ConcurrentHashMap<String, ArrayList<String>> spellIpFileMap = new ConcurrentHashMap<>();

    /**
     * key: ip
     * value: [the list of the directories of files stored under this client(ip)]
     * eg. {127.0.0.1=[C:/Users/c30035198/Pictures/Camera Roll/]}
     */
    private static ConcurrentHashMap<String, ArrayList<String>> ipDirMap = new ConcurrentHashMap<>();

    /**
     * list of ip of backup's client
     */
    private static List<String> backupClientIpList = Collections.synchronizedList(new ArrayList<String>());

    private static int clientCount = 0;

    /**
     * Indexing Server's main method to run the server. It runs in an infinite
     * loop listening on port 12233. When a connection is requested, it spawns a
     * new thread to do the servicing and immediately returns to listening. The
     * server keeps a unique peer id for each peer that connects to the server
     * for file sharing.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("****** Client information is been maintained ******");
        int clientId = 1;

        ServerSocket listener = new ServerSocket(Client_SOCKET_PORT);
        try {
            while (true) {
                new IpFileInfo(listener.accept(), clientId++).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class IpFileInfo extends Thread {
        private Socket socket;

        private int clientNumber;

        public IpFileInfo(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            System.out.println("New connection ---> client id: " + clientNumber + ", ip: " + socket.getInetAddress());
            clientCount++;
            System.out.println("There are " + clientCount + " clients connected with the server.");
        }

        public void run() {
            try {
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                String clientIp = socket.getInetAddress().getHostAddress();

                Response response = new Response();
                response.setResponseCode(200);
                response.setResponseData("Set client [ " + clientNumber + " ] as a backup node? (Y/N):");
                out.writeObject(response);

                Request clientRequest = (Request) in.readObject();
                String requestType = clientRequest.getRequestType();
                String requestData = (String) clientRequest.getRequestData();

                if (requestData.equalsIgnoreCase("Y")) {
                    System.out.println("Client [ " + clientNumber + " ] set to be a backup node.");

                    if (!backupClientIpList.contains(clientIp)) {
                        backupClientIpList.add(clientIp);
                    }

                    if (ipDirMap.containsKey(clientIp)) {
                        ipDirMap.get(clientIp).add(REPLICA_LOCATION);
                    } else {
                        ArrayList<String> paths = new ArrayList<>();
                        paths.add(REPLICA_LOCATION);
                        ipDirMap.put(clientIp, paths);
                    }

                    response = new Response();
                    response.setResponseCode(200);
                    response.setResponseData(spellIpFileMap);
                    out.writeObject(response);
                }

                response = new Response();
                response.setResponseCode(200);
                response.setResponseData(ipDirMap.get(clientIp));
                out.writeObject(response);

                while (true) {
					clientRequest = (Request) in.readObject();
                    requestType = clientRequest.getRequestType();

                    if (requestType.equalsIgnoreCase("REGISTER")) {
						ArrayList<String> registerFileList = (ArrayList<String>) clientRequest.getRequestData();
						ArrayList<String> indexedLocations = register(clientIp, registerFileList);
                        response = new Response();
                        response.setResponseCode(200);
                        response.setResponseData(indexedLocations);
                        out.writeObject(response);
                    } else if (requestType.equalsIgnoreCase("LOOKUP")) {
                        print("\nLooking up a file.");
                        String fileName = (String) clientRequest.getRequestData();

                        // If Request Type = LOOKUP, then call search(...) method to search for the specified file
                        print("Request from Peer # " + clientNumber + " (" + clientIp + ") to look for file "
                                + fileName);
                        HashMap<Integer, String> searchResults = search(fileName);

                        // If file found then respond with all the peer locations that contain the file or else send File Not Found message
                        if (searchResults.size() > 0) {
                            response = new Response();
                            response.setResponseCode(200);
                            response.setResponseData(searchResults);
                            out.writeObject(response);
                            print("File Found.");
                        } else {
                            response = new Response();
                            response.setResponseCode(404);
                            response.setResponseData("File Not Found.");
                            out.writeObject(response);
                            print("File Not Found.");
                        }
                    } else if (requestType.equalsIgnoreCase("UNREGISTER")) {
                        // If Request Type = UNREGISTER, then call unregister(...) method to remove all the files of the requested
                        // peer from the indexing server's database
                        response = new Response();
                        if (unregister(clientIp)) {
                            response.setResponseCode(200);
                            response.setResponseData("Your files have been un-registered from the indexing server.");
                            print("Peer # " + clientNumber + " (" + clientIp + ") has un-registered all its files.");
                        } else {
                            response.setResponseCode(400);
                            response.setResponseData("Error in un-registering files from the indexing server.");
                        }
                        out.writeObject(response);
                    } else if (requestType.equalsIgnoreCase("GET_BACKUP_NODES")) {
                        // Sends replication peers/nodes to the peer who is not able to download a file from its original peer.
                        System.out.println(
                                "\n" + clientIp + " requested backup nodes info. Sending backup nodes info.");
                        response = new Response();
                        response.setResponseCode(200);
                        response.setResponseData(backupClientIpList);
                        out.writeObject(response);
                        System.out.println("Backup nodoes information sent.");
                    } else if (requestType.equalsIgnoreCase("GET_FILES_LIST")) {
                        response = new Response();
                        response.setResponseCode(200);
                        response.setResponseData(spellIpFileMap);
                        out.writeObject(response);
                        System.out.println("All files information sent.");
                    } else if (requestType.equalsIgnoreCase("DISCONNECT")) {
                        print("\nPeer # " + clientNumber + " disconnecting...");
                        try {
                            // Close the connection and then stop the thread.
                            socket.close();
                        } catch (IOException e) {
                            print("Couldn't close a socket.");
                        }
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (EOFException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                print("Error handling Peer # " + clientNumber + ": " + e);
                Thread.currentThread().interrupt();
            }
        }

        // Stop thread once the peer has disconnected or some error has occurred in serving the peer.
        public void interrupt() {
            print("\nConnection with Peer # " + clientNumber + " closed");
            clientCount--;
            print("Total number of peers connected:" + clientCount);
            if (clientCount == 0) {
                print("No more peers connected.");
            }
        }

        /***
         * This method prints the message.
         * @param message Message to be printed on the console screen
         */
        private void print(String message) {
            LogUtil log = new LogUtil("server");
            log.write(message);
            log.close();
            System.out.println(message);
        }

		/**
		 * Register the files in ip.
		 * @param ip 				register ip
		 * @param registerFileList  register files list
		 * @return list of registered files name
		 * @throws IOException
		 */
        private ArrayList<String> register(String ip, ArrayList<String> registerFileList) throws IOException {
			System.out.println("Start registering files from client: " + ip);

			// registerFileList.get(0): registered files' directory
            if (ipDirMap.containsKey(ip)) {
                ipDirMap.get(ip).add(registerFileList.get(0));
            } else {
                ArrayList<String> cur = new ArrayList<>();
				cur.add(registerFileList.get(0));
                ipDirMap.put(ip, cur);
            }
			// only reserve the files name
			registerFileList.remove(0);

            StringBuffer sb = new StringBuffer();
            sb.append(clientNumber).append("#").append(ip).append("#").append(System.currentTimeMillis());
            spellIpFileMap.put(sb.toString(), registerFileList);

			System.out.println(registerFileList.size() + " files have registered in client [ " + clientNumber + " ] and added into spellIpFileMap");

            System.out.println("------------------------");
            System.out.println(spellIpFileMap);
            System.out.println(ipDirMap);
            System.out.println("------------------------");

            ConcurrentHashMap<String, ArrayList<String>> curFileList = new ConcurrentHashMap<>();
			curFileList.put(sb.toString(), registerFileList);
			backupFiles(curFileList);

            return ipDirMap.get(ip);
        }

        /***
         * This methods removes the file entries of the requested peer from the indeexDB
         * @param peerAddress    IP Address of the peer whose files are to be removed from the indexing server's database
         * @return Returns true if operation is successful else false
         */
        private boolean unregister(String peerAddress) throws IOException {
            int oldSize = spellIpFileMap.size();
            ArrayList<String> deleteFiles = null;

            for (Map.Entry e : spellIpFileMap.entrySet()) {
                String key = e.getKey().toString();
                ArrayList<String> value = (ArrayList<String>) e.getValue();

                if (key.contains(peerAddress)) {
                    deleteFiles = spellIpFileMap.get(key);
                    spellIpFileMap.remove(key);
                }
            }
            int newSize = spellIpFileMap.size();

            // Send request to delete the unregistered files from the replication node
            if (newSize < oldSize) {
                Request serverRequest = new Request();
                Socket socket = null;
                try {
                    serverRequest.setRequestType("DELETE_DATA");
                    for (String node : backupClientIpList) {
                        socket = new Socket(node, Server_SOCKET_PORT);
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        serverRequest.setRequestData(deleteFiles);
                        out.writeObject(serverRequest);
                        out.close();
                        socket.close();
                    }
                    socket = null;
                } catch (Exception e) {
                    print("Error in replication:" + e);
                } finally {
                    serverRequest = null;
                    if (socket != null && socket.isConnected()) {
                        socket.close();
                    }
                }
            }

            return (newSize < oldSize);
        }

        /***
         * This method searches the specified fileName in the indexing server's database
         * @param fileName    Name of the file to be searched in the indexing server's database
         * @return Returns a hashmap which contains <Peer ID, Peer IP Address> of all the peers that contain the searched file
         */
        private HashMap<Integer, String> search(String fileName) {
            HashMap<Integer, String> searchResults = new HashMap<Integer, String>();
            for (Map.Entry e : spellIpFileMap.entrySet()) {
                String key = e.getKey().toString();
                ArrayList<String> value = (ArrayList<String>) e.getValue();

                for (String file : value) {
                    if (file.equalsIgnoreCase(fileName)) {
                        int peerId = Integer.parseInt(key.split("#")[0].trim());
                        String hostAddress = key.split("#")[1].trim();
                        searchResults.put(peerId, hostAddress);
                    }
                }
            }
            return searchResults;
        }

        /***
         * This method is called whenever a peer registers its files.
         * The Indexing server sends a REPLICATE_DATA to the replication nodes to update its replication data.
         * @param newFiles    HashMap contaning peer address and list of new files which has been registered by the Peer
         */
        private void backupFiles(ConcurrentHashMap<String, ArrayList<String>> newFiles) throws IOException {
            Request serverRequest = new Request();
            Socket socket = null;
            try {
                serverRequest.setRequestType("REPLICATE_DATA");
                for (String node : backupClientIpList) {
                    socket = new Socket(node, Server_SOCKET_PORT);
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    serverRequest.setRequestData(newFiles);
                    out.writeObject(serverRequest);
                    out.close();
                    socket.close();
                }
                socket = null;
            } catch (Exception e) {
                print("Error in replication:" + e);
            } finally {
                serverRequest = null;
                if (socket != null && socket.isConnected()) {
                    socket.close();
                }
            }
        }
    }
}





package iit_550_self;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtil {

	private static final int BUFFER_SIZE = 1024 * 64;

	private static final String DOWNLOAD_PATH = "downloads/";

	private static final String BACKUP_PATH = "backupfiles/";

	/***
	 * Get all file names in the path.
	 * @param path 	traverse file names in the path
	 * @return 		list of file names
	 */
	public static ArrayList<String> getFiles(String path) {
		ArrayList<String> files = new ArrayList<>();
		File folder = new File(path);
		if (folder.isDirectory()) {
			List<String> fileNameList = Arrays.stream(folder.listFiles())
					.filter(File::isFile)
					.map(File::getName)
					.collect(Collectors.toList());
			files.addAll(fileNameList);
		} else if (folder.isFile()) {
			files.add(path.substring(path.lastIndexOf("/") + 1));
		}

		return files;
	}

	/***
	 * Find the absolute path of fileName in the directory list.
	 * @param fileName	search file name
	 * @param dirList	list of all the directory
	 * @return			the absolute path of the input parameter fileName
	 */
	public static String getFileLocation(String fileName, List<String> dirList) {
		String fileAbsPath = "";
		boolean isFound = false;

		for (String dir : dirList) {
			File folder = new File(dir);
			File[] listOfFiles = folder.listFiles();

			for (File file : listOfFiles) {
				if (file.getName().equals(fileName)) {
					fileAbsPath = dir.endsWith("/") ? dir : dir.concat("/");
					isFound = true;
					break;
				}
			}
		}
		fileAbsPath = isFound ? fileAbsPath : "File not found.";
		return fileAbsPath;
	}

	/***
	 * Download the file from the client(ip).
	 * @param ip		download file in this ip
	 * @param port		download file in this ip's port
	 * @param fileName	name of the file
	 * @return			true: download successfully; false: download unsuccessfully
	 */
	public static boolean downloadFile(String ip, int port, String fileName) {
		InputStream in = null;
		BufferedOutputStream fileOutput = null;
		ObjectOutputStream out = null;
		Socket socket = null;
		boolean isDownloaded = false;

		try {
			System.out.println("\nDownloading file: " + fileName);

			File file = new File(DOWNLOAD_PATH);
			if (!file.exists()) {
				file.mkdir();
			}

			socket = new Socket(ip, port);
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();

			System.out.println("Requesting file ...");
			Request request = new Request();
			request.setRequestType("DOWNLOAD");
			request.setRequestData(fileName);
			out.writeObject(request);

			System.out.println("Downloading file ...");
			byte[] mybytearray = new byte[BUFFER_SIZE];
			in = socket.getInputStream();
			fileOutput = new BufferedOutputStream(new FileOutputStream(DOWNLOAD_PATH + fileName));

			int bytesRead;
			while ((bytesRead = in.read(mybytearray, 0, mybytearray.length)) > 0) {
				fileOutput.write(mybytearray, 0, bytesRead);
			}

			File curFile = new File(DOWNLOAD_PATH + fileName);
			if(curFile.length() == 0) {
				isDownloaded = false;
				curFile.delete();
			} else {
				isDownloaded = true;
			}
		} catch(SocketException e) {
			//System.out.println("Unable to connect to the host. Unable to  download file. Try using a different peer if available.");
			isDownloaded = false;
			//System.out.println("Error:" + e);
			//e.printStackTrace();
		} catch (Exception e) {
			//System.out.println("Unable to download file. Please check if you have write permission.");
			isDownloaded = false;
			//System.out.println("Error:" + e);
			//e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
				if (fileOutput != null) {
					fileOutput.close();
				}
				if (socket != null) {
					socket.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return isDownloaded;
	}

	/***
	 * Backup the file from the client(ip). (like downloadFile())
	 * @param ip		backup file in this ip
	 * @param port		backup file in this ip's port
	 * @param fileName	name of the file
	 * @return		    true: backup successfully; false: backup unsuccessfully
	 */
	public static boolean backupFile(String ip, int port, String fileName) {
		InputStream in = null;
		BufferedOutputStream fileOutput = null;
		ObjectOutputStream out = null;
		Socket socket = null;
		boolean isBackedUp = false;
		LogUtil log = new LogUtil("Backup");

		try {
			long startTime = System.currentTimeMillis();

			File file = new File(BACKUP_PATH);
			if (!file.exists()) {
				file.mkdir();
			}

			socket = new Socket(ip, port);
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();

			log.write("Backing up file ... " + fileName);
			Request request = new Request();
			request.setRequestType("DOWNLOAD");
			request.setRequestData(fileName);
			out.writeObject(request);

			log.write("Downloading file ... " + fileName);
			byte[] mybytearray = new byte[BUFFER_SIZE];
			in = socket.getInputStream();
			fileOutput = new BufferedOutputStream(new FileOutputStream(BACKUP_PATH + fileName));

			int bytesRead;
			while ((bytesRead = in.read(mybytearray, 0, mybytearray.length)) > 0) {
				fileOutput.write(mybytearray, 0, bytesRead);
			}

			long endTime = System.currentTimeMillis();
			double time = (double) Math.round(endTime - startTime) / 1000;
			log.write("File backed up successfully in " + time + " seconds.");
			isBackedUp = true;
		} catch(SocketException e) {
			log.write("Unable to connect to the host. Unable to backed up file.");
			isBackedUp = false;
			log.write("Error:" + e);
		} catch (Exception e) {
			log.write("Unable to backed up file. Please check if you have write permission.");
			isBackedUp = false;
			log.write("Error:" + e);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
				if (fileOutput != null) {
					fileOutput.close();
				}
				if (socket != null) {
					socket.close();
				}
				if (log != null) {
					log.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return isBackedUp;
	}

	/***
	 * Print the content of the text file.
	 * @param fileName	print this file
	 */
	public static void printFile(String fileName) {
		File file = new File(DOWNLOAD_PATH + fileName);
		if (file.exists()) {
			System.out.println("\n****** The content of the file is as follows: ******");
			System.out.println("----------------------------------------------------");
			BufferedReader br = null;

			try {
				br = new BufferedReader(new FileReader(DOWNLOAD_PATH + fileName));
				String line = null;
				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}
			} catch (Exception e) {
				System.out.println("Error:" + e);
			} finally {
				try {
					if (br != null) {
						br.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println("----------------------------------------------------");
		} else {
			System.out.println("\nThe file is not downloaded, please download it firstly.");
		}
	}

}




package iit_550_self;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtil {

	private static final int BUFFER_SIZE = 1024 * 64;

	private static final String DOWNLOAD_PATH = "downloads/";

	private static final String BACKUP_PATH = "backupfiles/";

	/***
	 * Get all file names in the path.
	 * @param path 	traverse file names in the path
	 * @return 		list of file names
	 */
	public static ArrayList<String> getFiles(String path) {
		ArrayList<String> files = new ArrayList<>();
		File folder = new File(path);
		if (folder.isDirectory()) {
			List<String> fileNameList = Arrays.stream(folder.listFiles())
					.filter(File::isFile)
					.map(File::getName)
					.collect(Collectors.toList());
			files.addAll(fileNameList);
		} else if (folder.isFile()) {
			files.add(path.substring(path.lastIndexOf("/") + 1));
		}

		return files;
	}

	/***
	 * Find the absolute path of fileName in the directory list.
	 * @param fileName	search file name
	 * @param dirList	list of all the directory
	 * @return			the absolute path of the input parameter fileName
	 */
	public static String getFileLocation(String fileName, List<String> dirList) {
		String fileAbsPath = "";
		boolean isFound = false;

		for (String dir : dirList) {
			File folder = new File(dir);
			File[] listOfFiles = folder.listFiles();

			for (File file : listOfFiles) {
				if (file.getName().equals(fileName)) {
					fileAbsPath = dir.endsWith("/") ? dir : dir.concat("/");
					isFound = true;
					break;
				}
			}
		}
		fileAbsPath = isFound ? fileAbsPath : "File not found.";
		return fileAbsPath;
	}

	/***
	 * Download the file from the client(ip).
	 * @param ip		download file in this ip
	 * @param port		download file in this ip's port
	 * @param fileName	name of the file
	 * @return			true: download successfully; false: download unsuccessfully
	 */
	public static boolean downloadFile(String ip, int port, String fileName) {
		InputStream in = null;
		BufferedOutputStream fileOutput = null;
		ObjectOutputStream out = null;
		Socket socket = null;
		boolean isDownloaded = false;

		try {
			System.out.println("\nDownloading file: " + fileName);

			File file = new File(DOWNLOAD_PATH);
			if (!file.exists()) {
				file.mkdir();
			}

			socket = new Socket(ip, port);
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();

			System.out.println("Requesting file ...");
			Request request = new Request();
			request.setRequestType("DOWNLOAD");
			request.setRequestData(fileName);
			out.writeObject(request);

			System.out.println("Downloading file ...");
			byte[] mybytearray = new byte[BUFFER_SIZE];
			in = socket.getInputStream();
			fileOutput = new BufferedOutputStream(new FileOutputStream(DOWNLOAD_PATH + fileName));

			int bytesRead;
			while ((bytesRead = in.read(mybytearray, 0, mybytearray.length)) > 0) {
				fileOutput.write(mybytearray, 0, bytesRead);
			}

			File curFile = new File(DOWNLOAD_PATH + fileName);
			if(curFile.length() == 0) {
				isDownloaded = false;
				curFile.delete();
			} else {
				isDownloaded = true;
			}
		} catch(SocketException e) {
			//System.out.println("Unable to connect to the host. Unable to  download file. Try using a different peer if available.");
			isDownloaded = false;
			//System.out.println("Error:" + e);
			//e.printStackTrace();
		} catch (Exception e) {
			//System.out.println("Unable to download file. Please check if you have write permission.");
			isDownloaded = false;
			//System.out.println("Error:" + e);
			//e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
				if (fileOutput != null) {
					fileOutput.close();
				}
				if (socket != null) {
					socket.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return isDownloaded;
	}

	/***
	 * Backup the file from the client(ip). (like downloadFile())
	 * @param ip		backup file in this ip
	 * @param port		backup file in this ip's port
	 * @param fileName	name of the file
	 * @return		    true: backup successfully; false: backup unsuccessfully
	 */
	public static boolean backupFile(String ip, int port, String fileName) {
		InputStream in = null;
		BufferedOutputStream fileOutput = null;
		ObjectOutputStream out = null;
		Socket socket = null;
		boolean isBackedUp = false;
		LogUtil log = new LogUtil("Backup");

		try {
			long startTime = System.currentTimeMillis();

			File file = new File(BACKUP_PATH);
			if (!file.exists()) {
				file.mkdir();
			}

			socket = new Socket(ip, port);
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();

			log.write("Backing up file ... " + fileName);
			Request request = new Request();
			request.setRequestType("DOWNLOAD");
			request.setRequestData(fileName);
			out.writeObject(request);

			log.write("Downloading file ... " + fileName);
			byte[] mybytearray = new byte[BUFFER_SIZE];
			in = socket.getInputStream();
			fileOutput = new BufferedOutputStream(new FileOutputStream(BACKUP_PATH + fileName));

			int bytesRead;
			while ((bytesRead = in.read(mybytearray, 0, mybytearray.length)) > 0) {
				fileOutput.write(mybytearray, 0, bytesRead);
			}

			long endTime = System.currentTimeMillis();
			double time = (double) Math.round(endTime - startTime) / 1000;
			log.write("File backed up successfully in " + time + " seconds.");
			isBackedUp = true;
		} catch(SocketException e) {
			log.write("Unable to connect to the host. Unable to backed up file.");
			isBackedUp = false;
			log.write("Error:" + e);
		} catch (Exception e) {
			log.write("Unable to backed up file. Please check if you have write permission.");
			isBackedUp = false;
			log.write("Error:" + e);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
				if (fileOutput != null) {
					fileOutput.close();
				}
				if (socket != null) {
					socket.close();
				}
				if (log != null) {
					log.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return isBackedUp;
	}

	/***
	 * Print the content of the text file.
	 * @param fileName	print this file
	 */
	public static void printFile(String fileName) {
		File file = new File(DOWNLOAD_PATH + fileName);
		if (file.exists()) {
			System.out.println("\n****** The content of the file is as follows: ******");
			System.out.println("----------------------------------------------------");
			BufferedReader br = null;

			try {
				br = new BufferedReader(new FileReader(DOWNLOAD_PATH + fileName));
				String line = null;
				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}
			} catch (Exception e) {
				System.out.println("Error:" + e);
			} finally {
				try {
					if (br != null) {
						br.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println("----------------------------------------------------");
		} else {
			System.out.println("\nThe file is not downloaded, please download it firstly.");
		}
	}

}






package iit_550_self;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LogUtil {

	private final String logPath = "logs/";

	private String logFileName = "";

	private BufferedWriter bw = null;

	public LogUtil(String logType) {
		try {
			if (logType.equalsIgnoreCase("Client")) {
				logFileName = "client.log";
			} else if (logType.equalsIgnoreCase("Server")) {
				logFileName = "server.log";
			} else if (logType.equalsIgnoreCase("Backup")) {
				logFileName = "backup.log";
			}
			File file = new File(logPath);
			if (!file.exists()) {
				file.mkdir();
			}
			bw = new BufferedWriter(new FileWriter(logPath + logFileName, true));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/***
	 * write operation information into the log.
	 * @param logContent operation information
	 */
	public void write(String logContent) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String timeLog = dateFormat.format(Calendar.getInstance().getTime());
			if (bw != null) {
				logContent = String.format("%s => %s", timeLog, logContent);
				bw.write(logContent);
				String newline = System.getProperty("line.separator");
				bw.write(newline);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/***
	 * Print the content of the log.
	 */
	public void print() {
		BufferedReader br = null;
		int count = 0;

		System.out.println("\n****** The content of the " + logFileName + " log is as follows: ******");
		System.out.println("------------------------------------------------------------");

		try {
			br = new BufferedReader(new FileReader(logPath + logFileName));
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				count += line.length();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (count == 0) {
			System.out.println("There are no log printing.");
		}

		System.out.println("------------------------------------------------------------");
	}

	public void close() {
		try {
			if (bw != null) {
				String newline = System.getProperty("line.separator");
				bw.write(newline);
				bw.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		if (bw != null) {
			bw.close();
		}
		super.finalize();
	}

}





package iit_550_self;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Peer {

    // myIndexedLoc stores the list of all the locations whose files are registered with the Indexing Server.
    private static List<String> myIndexedLoc = Collections.synchronizedList(new ArrayList<String>());

    private static final int PEER_SERVER_PORT = 22233;

    private static final String REPLICATION_PATH = "backupfiles/";

    public static void main(String[] args) throws IOException {
        // Start a new Thread which acts as Client on Peer side
        System.out.println("********** PEER CLIENT STARTED **********");
        new PeerClient().start();

        /**
         * Peer's server implementation. It runs in an infinite loop listening
         * on port 22233. When a a file download is requested, it spawns a new
         * thread to do the servicing and immediately returns to listening.
         */
        System.out.println("********** PEER SERVER STARTED **********");
        ServerSocket listener = new ServerSocket(PEER_SERVER_PORT);
        try {
            while (true) {
                new PeerServer(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class PeerServer extends Thread {
        private Socket socket;

        private LogUtil log = new LogUtil("peer");

        public PeerServer(Socket socket) {
            this.socket = socket;
            log.write("File downloading with " + socket.getInetAddress() + " started.");
        }

        // Services this thread's peer client by sending the requested file.
        public void run() {
            OutputStream out = null;
            ObjectInputStream in = null;
            BufferedInputStream fileInput = null;

            try {
                String clientIp = socket.getInetAddress().getHostAddress();
                log.write("Serving download request for " + clientIp);

                in = new ObjectInputStream(socket.getInputStream());
                Request request = (Request) in.readObject();

                if (request.getRequestType().equalsIgnoreCase("DOWNLOAD")) {
                    String fileName = (String) request.getRequestData();
                    String fileLocation = FileUtil.getFileLocation(fileName, myIndexedLoc);
                    log.write("Uploding/Sending file " + fileName);

                    File file = new File(fileLocation + fileName);
                    byte[] mybytearray = new byte[(int) file.length()];
                    fileInput = new BufferedInputStream(new FileInputStream(file));
                    fileInput.read(mybytearray, 0, mybytearray.length);
                    out = socket.getOutputStream();
                    out.write(mybytearray, 0, mybytearray.length);
                    out.flush();
                    log.write("File sent successfully.");
                } else if (request.getRequestType().equalsIgnoreCase("REPLICATE_DATA")) {
                    ConcurrentHashMap<String, ArrayList<String>> data
                            = (ConcurrentHashMap<String, ArrayList<String>>) request.getRequestData();
                    new ReplicationService(data).start();
                } else if (request.getRequestType().equalsIgnoreCase("DELETE_DATA")) {
                    ArrayList<String> deleteFiles = (ArrayList<String>) request.getRequestData();
                    if (deleteFiles != null) {
                        for (String fileName : deleteFiles) {
                            File file = new File(REPLICATION_PATH + fileName);
                            file.delete();
                            file = null;
                        }
                    }
                }
            } catch (Exception e) {
                log.write("Error in sending file.");
                log.write("ERROR:" + e);
            } finally {
                try {
                    // Closing all streams. Close the stream only if it is initialized
                    if (out != null) {
                        out.close();
                    }

                    if (in != null) {
                        in.close();
                    }

                    if (fileInput != null) {
                        fileInput.close();
                    }

                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void interrupt() {
            log.close();
            super.interrupt();
        }
    }

    private static class PeerClient extends Thread {

        // Thread implementation for Peer to serve as CLient
        public void run() {
            Socket socket = null;
            ObjectInputStream in = null;
            BufferedReader input = null;
            ObjectOutputStream out = null;
            Request peerRequest = null;
            Response serverResponse = null;

            try {
                input = new BufferedReader(new InputStreamReader(System.in));
                System.out.println("Enter Server IP Address:");
                String serverAddress = input.readLine();
                long startTime, endTime;
                double time;

                if (serverAddress.trim().length() == 0 || !IPServer.validate(serverAddress)) {
                    System.out.println("Invalid Server IP Address.");
                    System.exit(0);
                }

                // Make connection with server using the specified Host Address and Port 12233
                socket = new Socket(serverAddress, 12233);

                // Initializing output stream using the socket's output stream
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();

                // Initializing input stream using the socket's input stream
                in = new ObjectInputStream(socket.getInputStream());

                // Read the initial welcome message from the server
                serverResponse = (Response) in.readObject();
                System.out.print((String) serverResponse.getResponseData());
                String replicaChoice = input.readLine();

                // Setup a Request object with Request Type = REPLICATION and Request Data = Choice
                peerRequest = new Request();
                peerRequest.setRequestType("REPLICATION");
                peerRequest.setRequestData(replicaChoice);
                out.writeObject(peerRequest);

                if (replicaChoice.equalsIgnoreCase("Y")) {
                    // Read the Replication response from the server
                    myIndexedLoc.add(REPLICATION_PATH);
                    serverResponse = (Response) in.readObject();
                    ConcurrentHashMap<String, ArrayList<String>> data
                            = (ConcurrentHashMap<String, ArrayList<String>>) serverResponse.getResponseData();
                    new ReplicationService(data).start();
                }

                // Previously indexed locations if any
                serverResponse = (Response) in.readObject();
                ArrayList<String> indexedLocations = (ArrayList<String>) serverResponse.getResponseData();
                if (indexedLocations != null) {
                    for (String x : indexedLocations) {
                        if (!myIndexedLoc.contains(x)) {
                            myIndexedLoc.add(x);
                        }
                    }
                }

                while (true) {
                    // Display different choices to the user
                    System.out.println("\nWhat do you want to do?");
                    System.out.println("1.Register files with indexing server.");
                    System.out.println("2.Lookup for a file at index server.");
                    System.out.println("3.Un-register all files of this peer from the indexing server.");
                    System.out.println("4.Print download log of this peer.");
                    System.out.println("5.Get files list.");
                    System.out.println("6.Exit.");
                    System.out.print("Enter choice and press ENTER:");
                    int option;

                    // Check if the user has entered only numbers.
                    try {
                        option = Integer.parseInt(input.readLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Wrong choice. Try again!!!");
                        continue;
                    }

                    // Handling all the choices
                    switch (option) {
                        // Register files with indexing server functionality
                        case 1:
                            System.out.println("\nEnter path of the files to sync with indexing server:");
                            String path = input.readLine();

                            // Checking if the user has entered something
                            if (path.trim().length() == 0) {
                                System.out.println("Invalid Path.");
                                continue;
                            }

                            // Retrieve all the files from the user's specified location
                            ArrayList<String> files = FileUtil.getFiles(path);

                            // Add the user's entered file/path to peer's indexed location's list
                            File file = new File(path);
                            if (file.isFile()) {
                                myIndexedLoc.add(path.substring(0, path.lastIndexOf("/")));
                                System.out.println(path.substring(0, path.lastIndexOf("/")));
                                files.add(0, path.substring(0, path.lastIndexOf("/")));
                            } else if (file.isDirectory()) {
                                myIndexedLoc.add(path);
                                files.add(0, path);
                            }

                            // 1 because path is always there
                            if (files.size() > 1) {
                                startTime = System.currentTimeMillis();

                                // Setup a Request object with Request Type = REGISTER and Request Data = files array list
                                peerRequest = new Request();
                                peerRequest.setRequestType("REGISTER");
                                peerRequest.setRequestData(files);
                                out.writeObject(peerRequest);

                                // Retrieve response from the server
                                serverResponse = (Response) in.readObject();
                                endTime = System.currentTimeMillis();
                                time = (double) Math.round(endTime - startTime) / 1000;

                                // If Response is success i.e. Response Code = 200, then print success message else error message
                                if (serverResponse.getResponseCode() == 200) {
								/*indexedLocations =  (ArrayList<String>) serverResponse.getResponseData();
								for (String x : indexedLocations) {
									if (!myIndexedLoc.contains(x)) {
										myIndexedLoc.add(x);
									}
								}*/
                                    System.out.println(
                                            (files.size() - 1) + " files registered with indexing server. Time taken:"
                                                    + time + " seconds.");
                                } else {
                                    System.out.println("Unable to register files with server. Please try again later.");
                                }
                            } else {
                                System.out.println(
                                        "0 files found at this location. Nothing registered with indexing server.");
                            }
                            break;

                        // Handling file lookup on indexing server functionality
                        case 2:
                            System.out.println("\nEnter name of the file you want to look for at indexing server:");
                            String fileName = input.readLine();
                            String hostAddress;

                            startTime = System.currentTimeMillis();
                            // Setup a Request object with Request Type = LOOKUP and Request Data = file to be searched
                            peerRequest = new Request();
                            peerRequest.setRequestType("LOOKUP");
                            peerRequest.setRequestData(fileName);
                            out.writeObject(peerRequest);

                            serverResponse = (Response) in.readObject();
                            endTime = System.currentTimeMillis();
                            time = (double) Math.round(endTime - startTime) / 1000;

                            // If Response is success i.e. Response Code = 200, then perform download operation else error message
                            if (serverResponse.getResponseCode() == 200) {
                                System.out.println("File Found. Lookup time: " + time + " seconds.");

                                // Response Data contains the List of Peers which contain the searched file
                                HashMap<Integer, String> lookupResults
                                        = (HashMap<Integer, String>) serverResponse.getResponseData();

                                // Printing all Peer details that contain the searched file
                                if (lookupResults != null) {
                                    for (Map.Entry e : lookupResults.entrySet()) {
                                        System.out.println("\nPeer ID:" + e.getKey().toString());
                                        System.out.println("Host Address:" + e.getValue().toString());
                                    }
                                }

                                // If the file is a Text file then we can print or else only download file
                                if (fileName.trim().endsWith(".txt")) {
                                    System.out.print(
                                            "\nDo you want to download (D) or print this file (P)? Enter (D/P):");
                                    String download = input.readLine();

                                    // In case there are more than 1 peer, then we user will select which peer to use for download
                                    if (lookupResults.size() > 1) {
                                        System.out.print("Enter Peer ID from which you want to download the file:");
                                        int peerId = Integer.parseInt(input.readLine());
                                        hostAddress = lookupResults.get(peerId);
                                    } else {
                                        Map.Entry<Integer, String> entry = lookupResults.entrySet().iterator().next();
                                        hostAddress = entry.getValue();
                                    }

                                    if (download.equalsIgnoreCase("D")) {
                                        System.out.println(
                                                "The file will be downloaded in the 'downloads' folder in the current location.");
                                        // Obtain the searched file from the specified Peer
                                        obtain(hostAddress, PEER_SERVER_PORT, fileName, out, in);
                                    } else if (download.equalsIgnoreCase("P")) {
                                        // Obtain the searched file from the specified Peer and print its contents
                                        obtain(hostAddress, PEER_SERVER_PORT, fileName, out, in);
                                        FileUtil.printFile(fileName);
                                    }
                                } else {
                                    System.out.print("\nDo you want to download this file?(Y/N):");
                                    String download = input.readLine();
                                    if (download.equalsIgnoreCase("Y")) {
                                        if (lookupResults.size() > 1) {
                                            System.out.print("Enter Peer ID from which you want to download the file:");
                                            int peerId = Integer.parseInt(input.readLine());
                                            hostAddress = lookupResults.get(peerId);
                                        } else {
                                            Map.Entry<Integer, String> entry = lookupResults.entrySet()
                                                    .iterator()
                                                    .next();
                                            hostAddress = entry.getValue();
                                        }
                                        // Obtain the searched file from the specified Peer
                                        obtain(hostAddress, PEER_SERVER_PORT, fileName, out, in);
                                    }
                                }
                            } else {
                                System.out.println((String) serverResponse.getResponseData());
                                System.out.println("Lookup time: " + time + " seconds.");
                            }
                            break;

                        // Handling de-registration of files from the indexing server
                        case 3:
                            // Confirming user's un-register request
                            System.out.print("\nAre you sure (Y/N)?:");
                            String confirm = input.readLine();

                            if (confirm.equalsIgnoreCase("Y")) {
                                startTime = System.currentTimeMillis();
                                // Setup a Request object with Request Type = UNREGISTER and Request Data = general message
                                peerRequest = new Request();
                                peerRequest.setRequestType("UNREGISTER");
                                peerRequest.setRequestData("Un-register all files from index server.");
                                out.writeObject(peerRequest);
                                endTime = System.currentTimeMillis();
                                time = (double) Math.round(endTime - startTime) / 1000;

                                serverResponse = (Response) in.readObject();
                                System.out.println((String) serverResponse.getResponseData());
                                System.out.println("Time taken:" + time + " seconds.");
                            }
                            break;

                        // Printing the download log
                        case 4:
                            (new LogUtil("peer")).print();
                            break;

                        // Handling Peer exit functionality
                        case 5:
                            System.out.println("\nAll file are as follows:");
                            peerRequest = new Request();
                            peerRequest.setRequestType("GET_FILES_LIST");
                            peerRequest.setRequestData(null);
                            out.writeObject(peerRequest);

                            // Retrieve response from the server
                            serverResponse = (Response) in.readObject();
                            if (serverResponse.getResponseCode() == 200) {
                                // Response Data contains the List of Peers which contain the searched file
                                ConcurrentHashMap<String, ArrayList<String>> indexDatabase = (ConcurrentHashMap<String, ArrayList<String>>) serverResponse.getResponseData();
                                if (indexDatabase.values().size() != 0) {
                                    for (Map.Entry<String, ArrayList<String>> entry : indexDatabase.entrySet()) {
                                        String[] split = entry.getKey().split("#");
                                        for (String name : entry.getValue()) {
                                            System.out.println(
                                                    "Client:" + split[0] + ", ip:" + split[1] + ", file name:" + name);
                                        }
                                    }
                                } else {
                                    System.out.println("No files on this client.");
                                }
                            }
                            break;
                        case 6:
                            // Setup a Request object with Request Type = DISCONNECT and Request Data = general message
                            peerRequest = new Request();
                            peerRequest.setRequestType("DISCONNECT");
                            peerRequest.setRequestData("Disconnecting from server.");
                            out.writeObject(peerRequest);
                            System.out.println("Thanks for using the system.");
                            System.exit(0);
                            break;
                        default:
                            System.out.println("Input is wrong. Please try again.");
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    // Closing all streams. Close the stream only if it is initialized
                    if (out != null) {
                        out.close();
                    }

                    if (in != null) {
                        in.close();
                    }

                    if (socket != null) {
                        socket.close();
                    }

                    if (input != null) {
                        input.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /***
         * This method is used to download the file from the requested Peer.
         * @param hostAddress    IP Address of the peer used to download the file
         * @param port            Port of the per used to download the file
         * @param fileName        Name of the file to be downloaded
         */
        private void obtain(String hostAddress, int port, String fileName, ObjectOutputStream out,
                ObjectInputStream in) {
            boolean isDownloaded = false;
            long startTime = System.currentTimeMillis();

            if (!FileUtil.downloadFile(hostAddress, port, fileName)) {
                try {
                    Request peerRequest = new Request();
                    peerRequest.setRequestType("GET_BACKUP_NODES");
                    peerRequest.setRequestData("Send list of backup nodes.");
                    out.writeObject(peerRequest);

                    Response serverResponse = (Response) in.readObject();
                    List<String> backupNodes = (List<String>) serverResponse.getResponseData();

                    //System.out.println(backupNodes);
                    for (String node : backupNodes) {
                        if (FileUtil.downloadFile(node, port, fileName)) {
                            isDownloaded = true;
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                isDownloaded = true;
            }

            long endTime = System.currentTimeMillis();
            double time = (double) Math.round(endTime - startTime) / 1000;

            if (isDownloaded) {
                System.out.println("File downloaded successfully in " + time + " seconds.");
            } else {
                System.out.println(
                        "Unable to connect to the host. Unable to  download file. Try using a different peer if available.");
            }
        }
    }

    /***
     * This class acts as a replicator. The main function of this class is to
     * grab files from all the peers and store it in its replication directory.
     * When the Peer responds to server that it is ready to act as the
     * replication node, a new thread is created which is the replication
     * service thread which performs this task. It uses the same request format
     * to request a file from the peer as the Peer Client does to request a file
     * from another peer. A different thread is created so that the replicator
     * service doesn't affect other operations.
     */
    private static class ReplicationService extends Thread {
        private static ConcurrentHashMap<String, ArrayList<String>> data
                = new ConcurrentHashMap<String, ArrayList<String>>();

        public ReplicationService(ConcurrentHashMap<String, ArrayList<String>> data) {
            ReplicationService.data = data;
        }

        public void run() {
            for (Map.Entry e : data.entrySet()) {
                String key = e.getKey().toString();
                ArrayList<String> value = (ArrayList<String>) e.getValue();
                String ip = key.split("#")[1].trim();
                for (String file : value) {
                    // Replicate file from the respective peer
                    replicate(ip, PEER_SERVER_PORT, file);
                }
            }
            this.interrupt();
        }

        /***
         * This method is used to download the file from the requested Peer.
         * @param ip    IP Address of the peer used to download the file
         * @param port            Port of the per used to download the file
         * @param fileName        Name of the file to be downloaded
         */
        private void replicate(String ip, int port, String fileName) {
            FileUtil.backupFile(ip, port, fileName);
        }
    }
}





package iit_550_self;

import java.io.Serializable;

public class Request implements Serializable {

	private String requestType;

	private Object requestData;

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public Object getRequestData() {
		return requestData;
	}

	public void setRequestData(Object requestData) {
		this.requestData = requestData;
	}

}





package iit_550_self;

import java.io.Serializable;

public class Response implements Serializable {

	private int responseCode;
	private Object responseData;

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public Object getResponseData() {
		return responseData;
	}

	public void setResponseData(Object responseData) {
		this.responseData = responseData;
	}

}






package iit_550_self;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Test {

	private final static int TEST_COUNT = 1000;

	public static void main(String[] args) {

		BufferedReader input = null;

		try {
			input = new BufferedReader(new InputStreamReader(System.in));
			String hostAddress, fileName;

			// Display different choices to the user
			System.out.println("\nWhat do you want to test?");
			System.out.println("1.Lookup");
			System.out.println("2.Download");
			System.out.println("3.Exit.");
			System.out.print("Enter choice and press ENTER:");
			int option = 0;

			// Check if the user has entered only numbers.
			try {
				option = Integer.parseInt(input.readLine());
			} catch (NumberFormatException e) {
				System.out.println("Wrong choice. Try again!!!");
				System.exit(0);
			}

			switch (option) {
			case 1:
				System.out.println("\nEnter server address and name of the file you want to search:");
				hostAddress = input.readLine();
				fileName = input.readLine();
				(new LookupTest(hostAddress, fileName)).start();
				(new LookupTest(hostAddress, fileName)).start();
				(new LookupTest(hostAddress, fileName)).start();
				break;

			case 2:
				System.out.println("\nEnter peer address and two file names you want to download:");
				hostAddress = input.readLine();
				String file1 = input.readLine();
				String file2 = input.readLine();

				(new DownloadTest(hostAddress, file1)).start();
				(new DownloadTest(hostAddress, file2)).start();
				break;

			case 3:
				System.out.println("Thanks for using this system.");
				System.exit(0);
				break;
			default:
				System.out.println("Wrong choice. Try again!!!");
				break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static class LookupTest extends Thread {
		private String serverAddress;
		private String fileName;

		public LookupTest(String host, String file) {
			this.serverAddress = host;
			this.fileName = file;
		}

		public void run() {
			Socket socket = null;
			ObjectInputStream in = null;
			ObjectOutputStream out = null;
			Request peerRequest = null;
			Response serverResponse	= null;
			long startTime, endTime, totalTime = 0;
			double avgTime;

			try {
				socket = new Socket(serverAddress, 12233);
				BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		        out = new ObjectOutputStream(socket.getOutputStream());
		        out.flush();
		        in = new ObjectInputStream(socket.getInputStream());

				for (int i = 0; i < TEST_COUNT; i++) {
					startTime = System.currentTimeMillis();

					peerRequest = new Request();
					peerRequest.setRequestType("LOOKUP");
					peerRequest.setRequestData(fileName);
					out.writeObject(peerRequest);

					serverResponse = (Response) in.readObject();
					endTime = System.currentTimeMillis();
					totalTime += (endTime - startTime);
				}
				avgTime = (double) Math.round(totalTime / (double) TEST_COUNT) / 1000;

				System.out.println("Average lookup time for " + TEST_COUNT + " lookup requests is " + avgTime + " seconds.");
				input.readLine();
				this.interrupt();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					// Closing all streams. Close the stream only if it is initialized
					if (out != null)
						out.close();

					if (in != null)
						in.close();

					if (socket != null)
						socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static class DownloadTest extends Thread {
		private String peerAddress;
		private String fileName;
		private static int counter = 1;

		public DownloadTest(String host, String file) {
			this.peerAddress = host;
			this.fileName = file;
		}

		public void run() {
			long startTime, endTime, totalTime = 0, totalFileSize = 0;
			double time, avgSpeed;
			System.out.println("Test Started...");
			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

			try {
				for (int i = 0; i < TEST_COUNT; i++) {
					startTime = System.currentTimeMillis();
					FileUtil.downloadFile(peerAddress, 22233, fileName);
					endTime = System.currentTimeMillis();
					totalTime += (endTime - startTime);
					File file = new File("downloads/" + fileName);
					totalFileSize += file.length();
					file.delete();
				}
				time = (double) Math.round(totalTime / 1000.0);
				avgSpeed = (totalFileSize / (1024 * 1024)) / time;

				System.out.println("Average speed for downloading " + TEST_COUNT + " files is " + avgSpeed + " MBps. \nPress ENETER.");
				input.readLine();
				this.interrupt();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}













