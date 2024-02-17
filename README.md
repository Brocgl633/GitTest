package IIT_550_t;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Peer {
	
	// myIndexedLoc stores the list of all the locations whose files are registered with the Indexing Server.
	private static List<String> myIndexedLoc = Collections.synchronizedList(new ArrayList<String>());
	private static final int PEER_SERVER_PORT = 20000;
	private static final String REPLICATION_PATH = "replica/";
	
	public static void main(String[] args) throws IOException {
		// Start a new Thread which acts as Client on Peer side
		System.out.println("********** PEER CLIENT STARTED **********");
		new PeerClient().start();
		
		/**
		 * Peer's server implementation. It runs in an infinite loop listening
		 * on port 20000. When a a file download is requested, it spawns a new
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
		private LogUtility log = new LogUtility("peer");
		
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
					String fileLocation = FileUtility.getFileLocation(fileName, myIndexedLoc);
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
					ConcurrentHashMap<String, ArrayList<String>> data = (ConcurrentHashMap<String, ArrayList<String>>) request.getRequestData();
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
					if (out != null)
						out.close();
					
					if (in != null)
						in.close();
					
					if (fileInput != null)
						fileInput.close();
					
					if (socket != null)
						socket.close();
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
			Response serverResponse	= null;
			
			try {
				input = new BufferedReader(new InputStreamReader(System.in));
				System.out.println("Enter Server IP Address:");
		        String serverAddress = input.readLine();
		        long startTime, endTime;
		        double time;
		        
		        if(serverAddress.trim().length() == 0 || !IPAddressValidator.validate(serverAddress)) {
					System.out.println("Invalid Server IP Address.");
					System.exit(0);
				}

		        // Make connection with server using the specified Host Address and Port 10010
		        socket = new Socket(serverAddress, 10010);
		        
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
					ConcurrentHashMap<String, ArrayList<String>> data = (ConcurrentHashMap<String, ArrayList<String>>) serverResponse.getResponseData();
					new ReplicationService(data).start();
				}
				
				// Previously indexed locations if any
				serverResponse = (Response) in.readObject();
				ArrayList<String> indexedLocations =  (ArrayList<String>) serverResponse.getResponseData();
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
						if(path.trim().length() == 0) {
							System.out.println("Invalid Path.");
							continue;
						}
						
						// Retrieve all the files from the user's specified location
						ArrayList<String> files = FileUtility.getFiles(path);
						
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
								System.out.println((files.size() - 1) + " files registered with indexing server. Time taken:" + time + " seconds.");
							} else {
								System.out.println("Unable to register files with server. Please try again later.");
							}
						} else {
							System.out.println("0 files found at this location. Nothing registered with indexing server.");
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
							HashMap<Integer, String> lookupResults = (HashMap<Integer, String>) serverResponse.getResponseData();
							
							// Printing all Peer details that contain the searched file
							if (lookupResults != null) {
								for (Map.Entry e : lookupResults.entrySet()) {
									System.out.println("\nPeer ID:" + e.getKey().toString());
									System.out.println("Host Address:" + e.getValue().toString());
								}
							}
							
							// If the file is a Text file then we can print or else only download file
							if (fileName.trim().endsWith(".txt")) {
								System.out.print("\nDo you want to download (D) or print this file (P)? Enter (D/P):");
								String download = input.readLine();
								
								// In case there are more than 1 peer, then we user will select which peer to use for download
								if(lookupResults.size() > 1) {
									System.out.print("Enter Peer ID from which you want to download the file:");
									int peerId = Integer.parseInt(input.readLine());
									hostAddress = lookupResults.get(peerId);
								} else {
									Map.Entry<Integer,String> entry = lookupResults.entrySet().iterator().next();
									hostAddress = entry.getValue();
								}
								
								if (download.equalsIgnoreCase("D")) {
									System.out.println("The file will be downloaded in the 'downloads' folder in the current location.");
									// Obtain the searched file from the specified Peer
									obtain(hostAddress, 20000, fileName, out, in);
								} else if (download.equalsIgnoreCase("P")) {
									// Obtain the searched file from the specified Peer and print its contents
									obtain(hostAddress, 20000, fileName, out, in);
									FileUtility.printFile(fileName);
								}
							} else {
								System.out.print("\nDo you want to download this file?(Y/N):");
								String download = input.readLine();
								if (download.equalsIgnoreCase("Y")) {
									if(lookupResults.size() > 1) {
										System.out.print("Enter Peer ID from which you want to download the file:");
										int peerId = Integer.parseInt(input.readLine());
										hostAddress = lookupResults.get(peerId);
									} else {
										Map.Entry<Integer,String> entry = lookupResults.entrySet().iterator().next();
										hostAddress = entry.getValue();
									}
									// Obtain the searched file from the specified Peer
									obtain(hostAddress, 20000, fileName, out, in);
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
						(new LogUtility("peer")).print();
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
							for (Map.Entry<String, ArrayList<String>> entry : indexDatabase.entrySet()) {
								String[] split = entry.getKey().split("#");
								for (String name : entry.getValue()) {
									System.out.println("Peer:" + split[0] + ", ip:" + split[1] + ", file name:" + name);
								}
							}
						}
						break;
					case 6:
						// Setup a Request object with Request Type = DISCONNECT and Request Data = general message
						peerRequest = new Request();
						peerRequest.setRequestType("DISCONNECT");
						peerRequest.setRequestData("Disconnecting from server.");
						out.writeObject(peerRequest);
						System.out.println("Thanks for using this system.");
						System.exit(0);
						break;
					default:
						System.out.println("Wrong choice. Try again!!!");
						break;
					}
		        }
			} catch(Exception e) {
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
					
					if (input != null)
						input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		/***
		 * This method is used to download the file from the requested Peer.
		 * @param hostAddress 	IP Address of the peer used to download the file
		 * @param port			Port of the per used to download the file
		 * @param fileName		Name of the file to be downloaded
		 */
		private void obtain(String hostAddress, int port, String fileName, ObjectOutputStream out, ObjectInputStream in) {
			boolean isDownloaded = false;
			long startTime = System.currentTimeMillis();
			
			if (!FileUtility.downloadFile(hostAddress, port, fileName)) {
				try {
					Request peerRequest = new Request();
					peerRequest.setRequestType("GET_BACKUP_NODES");
					peerRequest.setRequestData("Send list of backup nodes.");
					out.writeObject(peerRequest);
				
					Response serverResponse = (Response) in.readObject();
					List<String> backupNodes = (List<String>) serverResponse.getResponseData();
					
					//System.out.println(backupNodes);
					for (String node : backupNodes) {
						if(FileUtility.downloadFile(node, port, fileName)) {
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
				System.out.println("Unable to connect to the host. Unable to  download file. Try using a different peer if available.");
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
		private static ConcurrentHashMap<String, ArrayList<String>> data = new ConcurrentHashMap<String, ArrayList<String>>();
		
		public ReplicationService (ConcurrentHashMap<String, ArrayList<String>> data) {
			ReplicationService.data = data;
		}
		
		public void run () {
			for (Map.Entry e : data.entrySet()) {
				String key = e.getKey().toString();
				ArrayList<String> value = (ArrayList<String>) e.getValue();
				String hostAddress = key.split("#")[1].trim();
				for (String file : value) {
					// Replicate file from the respective peer
					replicate(hostAddress, 20000, file);
				}
			}
			this.interrupt();
		}
		
		/***
		 * This method is used to download the file from the requested Peer.
		 * @param hostAddress 	IP Address of the peer used to download the file
		 * @param port			Port of the per used to download the file
		 * @param fileName		Name of the file to be downloaded
		 */
		private void replicate(String hostAddress, int port, String fileName) {
			FileUtility.replicateFile(hostAddress, port, fileName);
		}
	}
}







package IIT_550_t;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The indexing server program accepts request from peers to register/unregister
 * files and search a file int its indexing database.
 */
public class IndexingServer {

	/***
	 * indexDatabase is a concurrent hash map used to store the records which contains all the files registered from various Peers.
	 * We are using ConcurrentHasMap because it is serializable as well as thread safe.
	 * The key of the indexDB contains Peer ID and Peer IP Address separated by # (Example: 1#127.0.0.1)
	 * The value of the indexDB contains an ArrayList of String which contains the list of files.
	 */
	private static ConcurrentHashMap<String, ArrayList<String>> indexDatabase = new ConcurrentHashMap<String, ArrayList<String>>();
	private static ConcurrentHashMap<String, ArrayList<String>> peerIndexedLocations = new ConcurrentHashMap<String, ArrayList<String>>();
	private static List<String> replicationNodes = Collections.synchronizedList(new ArrayList<String>());
	private static final int SERVER_SOCKET_PORT = 10010;
	private static final int PEER_SERVER_PORT = 20000;
	private static final String REPLICA_LOCATION = "replica/";
	
	// totalPeers stores the count of peers connected to the indexing server
	private static int totalPeers = 0;
	
	/**
	 * Indexing Server's main method to run the server. It runs in an infinite
	 * loop listening on port 10010. When a connection is requested, it spawns a
	 * new thread to do the servicing and immediately returns to listening. The
	 * server keeps a unique peer id for each peer that connects to the server
	 * for file sharing.
	 */
    public static void main(String[] args) throws Exception {
        System.out.println("********** INDEXING SERVER STARTED **********");
        int peerId = 1;
        
        ServerSocket listener = new ServerSocket(SERVER_SOCKET_PORT);
        try {
            while (true) {
                new Indexer(listener.accept(), peerId++).start();
            }
        } finally {
            listener.close();
        }
    }

    // A private thread to handle peer's file sharing requests on a particular socket.
    private static class Indexer extends Thread {
        private Socket socket;
        private int clientNumber;
        
        public Indexer(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            print("\nNew connection with Peer # " + clientNumber + " at " + socket.getInetAddress());
            totalPeers++;
            print("Total number of peers connected:" + totalPeers);
        }

		/**
		 * Services this thread's client by first sending the client a welcome
		 * message then repeatedly reading requests from the peer.
		 */
        public void run() {
            try {
            	// Initializing output stream using the socket's output stream
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                
                // Initializing input stream using the socket's input stream
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                String clientIp = socket.getInetAddress().getHostAddress();

                // Send a welcome message to the client
                Response response = new Response();
                response.setResponseCode(200);
                response.setResponseData("Hello, you are Peer #" + clientNumber + ".\nDo you want your node to act as a replication node? This requires your disk space to be large. (Y/N):");
                out.writeObject(response);
                
                Request peerRequest = (Request) in.readObject();
                String requestType = peerRequest.getRequestType();
                String replicaChoice = (String) peerRequest.getRequestData();
                
                if (replicaChoice.equalsIgnoreCase("Y")) {
                	System.out.println("Replication with this node accepted.");
                	
                	if (!replicationNodes.contains(clientIp)) {
                		replicationNodes.add(clientIp);
					}
                	
                	// Just to remind peer if he is acting as a replication node
                	if (peerIndexedLocations.containsKey(clientIp)) {
        				peerIndexedLocations.get(clientIp).add(REPLICA_LOCATION);
        			} else {
        				ArrayList<String> paths = new ArrayList<String>();
        	        	paths.add(REPLICA_LOCATION);
        	        	peerIndexedLocations.put(clientIp, paths);
        			}
                	
                	response = new Response();
                	response.setResponseCode(200);
                    response.setResponseData(indexDatabase);
                    out.writeObject(response);
				}
                
                response = new Response();
                response.setResponseCode(200);
                response.setResponseData(peerIndexedLocations.get(clientIp));
                out.writeObject(response);
                
                while(true) {
                	// Read the request object received from the Peer
                	peerRequest = (Request) in.readObject();
                    requestType = peerRequest.getRequestType();
                    
                    if (requestType.equalsIgnoreCase("REGISTER")) {
                    	// If Request Type = REGISTER, then call register(...) method to register the peer's files
                    	ArrayList<String> indexedLocations = register(clientNumber, clientIp, (ArrayList<String>) peerRequest.getRequestData(), out);
                    	response = new Response();
                    	response.setResponseCode(200);
                        response.setResponseData(indexedLocations);
                        out.writeObject(response);
    				} else if (requestType.equalsIgnoreCase("LOOKUP")) {
    					print("\nLooking up a file.");
    					String fileName = (String) peerRequest.getRequestData();
    					
    					// If Request Type = LOOKUP, then call search(...) method to search for the specified file
    					print("Request from Peer # " + clientNumber + " (" + clientIp + ") to look for file " + fileName);
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
    				} else if(requestType.equalsIgnoreCase("UNREGISTER")) {
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
    				} else if(requestType.equalsIgnoreCase("GET_BACKUP_NODES")) {
    					// Sends replication peers/nodes to the peer who is not able to download a file from its original peer.
    					System.out.println("\n" + clientIp + " requested backup nodes info. Sending backup nodes info.");
    					response = new Response();
    					response.setResponseCode(200);
						response.setResponseData(replicationNodes);
						out.writeObject(response);
						System.out.println("Backup nodoes information sent.");
    				} else if(requestType.equalsIgnoreCase("DISCONNECT")) {
    					print("\nPeer # " + clientNumber + " disconnecting...");
    					try {
    						// Close the connection and then stop the thread.
    	                    socket.close();
    	                } catch (IOException e) {
    	                    print("Couldn't close a socket.");
    	                }
    	                Thread.currentThread().interrupt();
    	                break;
    				} else if (requestType.equalsIgnoreCase("GET_FILES_LIST")) {
						response = new Response();
						response.setResponseCode(200);
						response.setResponseData(indexDatabase);
						out.writeObject(response);
						System.out.println("All files information sent.");
					}
                }
            } catch(EOFException e) {
            	Thread.currentThread().interrupt();
            } catch (Exception e) {
                print("Error handling Peer # " + clientNumber + ": " + e);
                Thread.currentThread().interrupt();
            }
        }
        
        // Stop thread once the peer has disconnected or some error has occurred in serving the peer.
        public void interrupt() {
        	print("\nConnection with Peer # " + clientNumber + " closed");
        	totalPeers--;
        	print("Total number of peers connected:" + totalPeers);
        	if (totalPeers == 0) {
        		print("No more peers connected.");
			}
        }

        /***
         * This method prints the message.
         * @param message Message to be printed on the console screen
         */
        private void print(String message) {
        	LogUtility log = new LogUtility("server");
        	log.write(message);
        	log.close();
            System.out.println(message);
        }
        
        /***
         * This method registers the files sent by the peer.
         * @param peerId		ID of the Peer who wants to register its files with the indexing server
         * @param peerAddress	IP Address of the Peer who wants to register its files with the indexing server
         * @param files			List of files to be registered with the indexing server
         */
        private ArrayList<String> register(int peerId, String peerAddress, ArrayList<String> files, ObjectOutputStream out) throws IOException {
        	print("\nRegistering files from Peer " + peerAddress);
        	
        	// Appending HHmmss just to make the key unique because a single peer may register multiple times. We aren't using the last appended data.
        	String time = new SimpleDateFormat("HHmmss").format(Calendar.getInstance().getTime());
        	
        	// Retrieving path and storing them separately
        	if (peerIndexedLocations.containsKey(peerAddress)) {
				peerIndexedLocations.get(peerAddress).add(files.get(0));
			} else {
				ArrayList<String> paths = new ArrayList<String>();
	        	paths.add(files.get(0));
	        	peerIndexedLocations.put(peerAddress, paths);
			}
        	files.remove(0);
        	
        	// Using StringBuffer to avoid creation of multiple string objects while appending
        	StringBuffer sb = new StringBuffer();
        	sb.append(clientNumber).append("#").append(peerAddress).append("#").append(time);
            indexDatabase.put(sb.toString(), files);
            
            print(files.size() + " files synced with Peer " + clientNumber + " and added to index database");

			System.out.println("------------------------");
			System.out.println(indexDatabase);
			System.out.println(peerIndexedLocations);
			System.out.println("------------------------");

            ConcurrentHashMap<String, ArrayList<String>> newFiles = new ConcurrentHashMap<String, ArrayList<String>>();
            newFiles.put(sb.toString(), files);
            sendReplicateCommand(newFiles);
            
            return peerIndexedLocations.get(peerAddress);
        }
        
        /***
         * This methods removes the file entries of the requested peer from the indeexDB
         * @param peerAddress	IP Address of the peer whose files are to be removed from the indexing server's database
         * @return				Returns true if operation is successful else false
         */
        private boolean unregister(String peerAddress) throws IOException {
        	int oldSize = indexDatabase.size();
        	ArrayList<String> deleteFiles = null;
        	
        	for (Map.Entry e : indexDatabase.entrySet()) {
				String key = e.getKey().toString();
				ArrayList<String> value = (ArrayList<String>) e.getValue();
				
				if (key.contains(peerAddress)) {
					deleteFiles = indexDatabase.get(key);
					indexDatabase.remove(key);
				}
			}
        	int newSize = indexDatabase.size();
        	
        	
        	// Send request to delete the unregistered files from the replication node
        	if (newSize < oldSize) {
        		Request serverRequest = new Request();
            	Socket socket = null;
            	try {
            		serverRequest.setRequestType("DELETE_DATA");
                	for (String node : replicationNodes) {
                		socket = new Socket(node, PEER_SERVER_PORT);
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
         * @param fileName	Name of the file to be searched in the indexing server's database
         * @return			Returns a hashmap which contains <Peer ID, Peer IP Address> of all the peers that contain the searched file
         */
        private HashMap<Integer, String> search(String fileName) {
        	HashMap<Integer, String> searchResults = new HashMap<Integer, String>();
			for (Map.Entry e : indexDatabase.entrySet()) {
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
         * @param newFiles	HashMap contaning peer address and list of new files which has been registered by the Peer
         */
        private void sendReplicateCommand(ConcurrentHashMap<String, ArrayList<String>> newFiles) throws IOException {
        	Request serverRequest = new Request();
        	Socket socket = null;
        	try {
        		serverRequest.setRequestType("REPLICATE_DATA");
            	for (String node : replicationNodes) {
            		socket = new Socket(node, PEER_SERVER_PORT);
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










