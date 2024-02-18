package IIT_550_t;

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

	private static final int CLIENT_SOCKET_PORT = 12233;

	private static final int SERVER_SOCKET_PORT = 22233;

	private static final String BACKUP_PATH = "backupfiles/";

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
	private static List<String> backupClientIpList = Collections.synchronizedList(new ArrayList<>());

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

		ServerSocket listener = new ServerSocket(CLIENT_SOCKET_PORT);
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
			logPrint("\nNew connection ---> client id: " + clientNumber + ", ip: " + socket.getInetAddress());
			clientCount++;
			logPrint("\nThere are " + clientCount + " clients connected with the server.");
		}

		public void run() {
			try {
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				out.flush();
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				String clientIp = socket.getInetAddress().getHostAddress();

				Response response = new Response();
				response.setResponseCode(200);
				response.setResponseData("Set client id [ " + clientNumber + " ] as a backup node? (Y/N):");
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
						ipDirMap.get(clientIp).add(BACKUP_PATH);
					} else {
						ArrayList<String> paths = new ArrayList<>();
						paths.add(BACKUP_PATH);
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
						logPrint("\nStart registering a file ...");
						ArrayList<String> registerFileList = (ArrayList<String>) clientRequest.getRequestData();
						ArrayList<String> fileList = register(clientIp, registerFileList);
						response = new Response();
						response.setResponseCode(200);
						response.setResponseData(fileList);
						out.writeObject(response);
					} else if (requestType.equalsIgnoreCase("SEARCH")) {
						logPrint("\nStart searching a file ...");
						String fileName = (String) clientRequest.getRequestData();
						logPrint("\nRequest from client id [ " + clientNumber + " ] and ip [ " + clientIp + " ] to search file: " + fileName);

						HashMap<Integer, String> res = search(fileName);
						if (res.size() > 0) {
							response = new Response();
							response.setResponseCode(200);
							response.setResponseData(res);
							out.writeObject(response);
							logPrint("\nFile found.");
						} else {
							response = new Response();
							response.setResponseCode(404);
							response.setResponseData("File not found.");
							out.writeObject(response);
							logPrint("\nFile Not Found.");
						}
					} else if (requestType.equalsIgnoreCase("UNREGISTER")) {
						logPrint("\nStart unregister a file ...");
						response = new Response();
						if (unregister(clientIp)) {
							response.setResponseCode(200);
							response.setResponseData("Files in ip [ " + clientIp + " ] have been unregistered.");
							logPrint("\nClient id [ " + clientNumber + " ], ip [" + clientIp + "] has unregistered all files.");
						} else {
							response.setResponseCode(400);
							response.setResponseData("Error in unregistering files from ip [ " + clientIp + " ].");
						}
						out.writeObject(response);
					} else if (requestType.equalsIgnoreCase("GET_BACKUP_NODES")) {
						System.out.println("Start getting backup nodes information ...");
						response = new Response();
						response.setResponseCode(200);
						response.setResponseData(backupClientIpList);
						out.writeObject(response);
						System.out.println("Backup nodes information sent.");
					} else if (requestType.equalsIgnoreCase("GET_FILES_LIST")) {
						logPrint("\nStart getting files list ...");
						response = new Response();
						response.setResponseCode(200);
						response.setResponseData(spellIpFileMap);
						out.writeObject(response);
						logPrint("\nAll files information sent.");
						logPrint("\n" + spellIpFileMap.toString());
					} else if (requestType.equalsIgnoreCase("DISCONNECT")) {
						logPrint("\nStart disconnecting to client id [ " + clientNumber + " ] ...");
						try {
							socket.close();
						} catch (IOException e) {
							logPrint("\nDISCONNECT error: " + e);
						}
						Thread.currentThread().interrupt();
						break;
					}
				}
			} catch (EOFException e) {
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				logPrint("\nError handling client id [ " + clientNumber + " ]: " + e);
				Thread.currentThread().interrupt();
			}
		}
		
		public void interrupt() {
			logPrint("\nConnection in client id [ " + clientNumber + " ] closed.");
			clientCount--;
			logPrint("\nThere are " + clientCount + " clients connected with the server.");
			if (clientCount == 0) {
				logPrint("\nThere are no clients connected with the server.");
			}
		}
		
		private void logPrint(String message) {
			LogUtil log = new LogUtil("Server");
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
			logPrint("\nStart registering files from client: " + ip);

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

			logPrint("\n" + registerFileList.size() + " files have registered in client [ " + clientNumber + " ] and added into spellIpFileMap");

			System.out.println("------------------------");
			System.out.println(spellIpFileMap);
			System.out.println(ipDirMap);
			System.out.println("------------------------");

			ConcurrentHashMap<String, ArrayList<String>> curSpellIpFileMap = new ConcurrentHashMap<>();
			curSpellIpFileMap.put(sb.toString(), registerFileList);
			backupFiles(curSpellIpFileMap);

			return ipDirMap.get(ip);
		}

		/***
		 * Removes the files of the requested client from the spellIpFileMap
		 * @param ip    un-register ip
		 * @return true: successfully; false: unsuccessfully
		 */
		private boolean unregister(String ip) throws IOException {
			int oldSize = spellIpFileMap.size();
			ArrayList<String> deleteFileList = null;

			for (Map.Entry<String, ArrayList<String>> entry: spellIpFileMap.entrySet()) {
				String key = entry.getKey();
				if (key.contains(ip)) {
					deleteFileList = spellIpFileMap.get(key);
					spellIpFileMap.remove(key);
				}
			}
			int newSize = spellIpFileMap.size();

			if (newSize < oldSize) {
				Request request = new Request();
				Socket socket = null;
				try {
					request.setRequestType("REMOVE_FILE");
					for (String backupIp : backupClientIpList) {
						socket = new Socket(backupIp, SERVER_SOCKET_PORT);
						ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
						request.setRequestData(deleteFileList);
						out.writeObject(request);
						out.close();
						socket.close();
					}
					socket = null;
				} catch (Exception e) {
					logPrint("\nError remove in backup node : " + e);
				} finally {
					if (socket != null && socket.isConnected()) {
						socket.close();
					}
				}
			}

			return (newSize < oldSize);
		}

		/***
		 * Search the file in the spellIpFileMap
		 * @param fileName    search this file
		 * @return Map<client id, client ip>
		 */
		private HashMap<Integer, String> search(String fileName) {
			HashMap<Integer, String> res = new HashMap<>();
			for (Map.Entry<String, ArrayList<String>> entry : spellIpFileMap.entrySet()) {
				String key = entry.getKey();
				ArrayList<String> value = entry.getValue();

				for (String name : value) {
					if (name.equalsIgnoreCase(fileName)) {
						int clientId = Integer.parseInt(key.split("#")[0].trim());
						String clientIp = key.split("#")[1].trim();
						res.put(clientId, clientIp);
					}
				}
			}
			return res;
		}

		/***
		 * Update backup data.
		 * @param curSpellIpFileMap file map which are needed backup
		 */
		private void backupFiles(ConcurrentHashMap<String, ArrayList<String>> curSpellIpFileMap) throws IOException {
			Request request = new Request();
			Socket socket = null;
			try {
				request.setRequestType("UPDATE_BACKUP_DATA");
				for (String node : backupClientIpList) {
					socket = new Socket(node, SERVER_SOCKET_PORT);
					ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
					request.setRequestData(curSpellIpFileMap);
					out.writeObject(request);
					out.close();
					socket.close();
				}
				socket = null;
			} catch (Exception e) {
				logPrint("\nError in back up:" + e);
			} finally {
				if (socket != null && socket.isConnected()) {
					socket.close();
				}
			}
		}
	}
}




package IIT_550_t;

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

public class ClientAndServer {

	// the directory of register files belong 
	private static List<String> regDirPathList = Collections.synchronizedList(new ArrayList<>());

	private static final int SERVER_SOCKET_PORT = 22233;

	private static final String BACKUP_PATH = "backupfiles/";

	public static void main(String[] args) throws IOException {
		System.out.println("****** CLIENT STARTED ******");
		new Client().start();

		System.out.println("****** SERVER STARTED ******");
		ServerSocket serverSocket = new ServerSocket(SERVER_SOCKET_PORT);
		try {
			while (true) {
				new Server(serverSocket.accept()).start();
			}
		} finally {
			serverSocket.close();
		}
	}

	private static class Server extends Thread {
		private Socket socket;

		private LogUtil log = new LogUtil("Client");

		public Server(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			OutputStream out = null;
			ObjectInputStream in = null;
			BufferedInputStream fileInput = null;

			try {
				String clientIp = socket.getInetAddress().getHostAddress();
				in = new ObjectInputStream(socket.getInputStream());
				Request request = (Request) in.readObject();

				if (request.getRequestType().equalsIgnoreCase("DOWNLOAD")) {
					log.write("Serving DOWNLOAD request for " + clientIp);
					String fileName = (String) request.getRequestData();
					String fileDirPath = FileUtil.getFileLocation(fileName, regDirPathList);
					log.write("Downloading file " + fileName);

					File file = new File(fileDirPath + fileName);
					byte[] mybytearray = new byte[(int) file.length()];
					fileInput = new BufferedInputStream(new FileInputStream(file));
					fileInput.read(mybytearray, 0, mybytearray.length);
					out = socket.getOutputStream();
					out.write(mybytearray, 0, mybytearray.length);
					out.flush();
					log.write("File sent successfully.");
				} else if (request.getRequestType().equalsIgnoreCase("UPDATE_BACKUP_DATA")) {
					log.write("Serving UPDATE_BACKUP_DATA request for " + clientIp);
					log.write("UPDATE_BACKUP_DATA start ...");
					ConcurrentHashMap<String, ArrayList<String>> spellIpFileMap = (ConcurrentHashMap<String, ArrayList<String>>) request.getRequestData();
					new BackupService(spellIpFileMap).start();
				} else if (request.getRequestType().equalsIgnoreCase("REMOVE_FILE")) {
					log.write("Serving REMOVE_FILE request for " + clientIp);
					log.write("REMOVE_FILE start ...");
					ArrayList<String> deleteFileList = (ArrayList<String>) request.getRequestData();
					for (String fileName : deleteFileList) {
						File file = new File(BACKUP_PATH + fileName);
						file.delete();
					}
				}
			} catch (Exception e) {
				log.write("ERROR: " + e);
			} finally {
				try {
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

	private static class Client extends Thread {
		public void run() {
			Socket socket = null;
			ObjectInputStream in = null;
			BufferedReader input = null;
			ObjectOutputStream out = null;
			Request clientRequest = null;
			Response serverResponse = null;

			try {
				input = new BufferedReader(new InputStreamReader(System.in));
				System.out.println("Please input Server IP :");
				String ip = input.readLine();
				if (ip.trim().length() == 0 || !IPServer.validate(ip)) {
					System.out.println("Invalid Server IP.");
					System.exit(0);
				}

				long startTime, endTime;
				double time;

				socket = new Socket(ip, 12233);
				out = new ObjectOutputStream(socket.getOutputStream());
				out.flush();
				in = new ObjectInputStream(socket.getInputStream());
				serverResponse = (Response) in.readObject();
				System.out.print((String) serverResponse.getResponseData());

				String isBackup = input.readLine();
				clientRequest = new Request();
				clientRequest.setRequestType("BACK_UP");
				clientRequest.setRequestData(isBackup);
				out.writeObject(clientRequest);

				if (isBackup.equalsIgnoreCase("Y")) {
					regDirPathList.add(BACKUP_PATH);
					serverResponse = (Response) in.readObject();
					ConcurrentHashMap<String, ArrayList<String>> spellIpFileMap	= (ConcurrentHashMap<String, ArrayList<String>>) serverResponse.getResponseData();
					new BackupService(spellIpFileMap).start();
				}

				// handle previous map of ip to directory
				serverResponse = (Response) in.readObject();
				ArrayList<String> ipDirList = (ArrayList<String>) serverResponse.getResponseData();
				if (ipDirList != null) {
					for (String x : ipDirList) {
						if (!regDirPathList.contains(x)) {
							regDirPathList.add(x);
						}
					}
				}

				while (true) {
					System.out.println("****** Client-Server mode file downloading system ******");
					System.out.println("1.Register files.");
					System.out.println("2.Search or download or print a file");
					System.out.println("3.Unregister files of client [" + ip + "]");
					System.out.println("4.Print client log of client [" + ip + "]");
					System.out.println("5.Print server log of client [" + ip + "]");
					System.out.println("6.Print backup log of client [" + ip + "]");
					System.out.println("7.Get files list.");
					System.out.println("8.Exit.");
					System.out.print("Please input number : ");

					int option;

					try {
						option = Integer.parseInt(input.readLine());
					} catch (NumberFormatException e) {
						System.out.println("Input is wrong. Please try again.");
						continue;
					}

					switch (option) {
						// Register files with indexing server functionality
						case 1:
							System.out.println("The path of the registered files : ");
							String path = input.readLine();
							if (path.trim().length() == 0) {
								System.out.println("Invalid Path.");
								continue;
							}

							ArrayList<String> files = FileUtil.getFiles(path);
							File file = new File(path);
							if (file.isFile()) {
								String dirPath = path.substring(0, path.lastIndexOf("/"));
								regDirPathList.add(dirPath);
								files.add(0, dirPath);
							} else if (file.isDirectory()) {
								regDirPathList.add(path);
								files.add(0, path);
							}

							if (files.size() > 1) {
								startTime = System.currentTimeMillis();

								clientRequest = new Request();
								clientRequest.setRequestType("REGISTER");
								clientRequest.setRequestData(files);
								out.writeObject(clientRequest);

								serverResponse = (Response) in.readObject();
								endTime = System.currentTimeMillis();
								time = (double) Math.round(endTime - startTime) / 1000;

								if (serverResponse.getResponseCode() == 200) {
									System.out.println((files.size() - 1) + " files have registered. Take "	+ time + " seconds.");
								} else {
									System.out.println("Unable to register files.");
								}
							} else {
								System.out.println("There are no more files in path [ " + path + " ].");
							}
							break;
						// Handling file lookup on indexing server functionality
						case 2:
							System.out.println("\nThe file name you want to search : ");
							String fileName = input.readLine();
							String clientIp;

							startTime = System.currentTimeMillis();

							clientRequest = new Request();
							clientRequest.setRequestType("SEARCH");
							clientRequest.setRequestData(fileName);
							out.writeObject(clientRequest);

							serverResponse = (Response) in.readObject();
							endTime = System.currentTimeMillis();
							time = (double) Math.round(endTime - startTime) / 1000;
							if (serverResponse.getResponseCode() == 200) {
								System.out.println("File found. Searching takes " + time + " seconds.");

								HashMap<Integer, String> clientIdIpMap = (HashMap<Integer, String>) serverResponse.getResponseData();
								if (clientIdIpMap != null) {
									for (Map.Entry<Integer, String> entry : clientIdIpMap.entrySet()) {
										System.out.println("Client id : " + entry.getKey());
										System.out.println("Host Address :" + entry.getValue());
									}
								}

								if (fileName.trim().endsWith(".txt")) {
									System.out.print("Download(D) or print(P) this file? Input(D/P): ");
									String dOrP = input.readLine();

									if (clientIdIpMap.size() > 1) {
										System.out.print("Choose one client id : ");
										int clientId = Integer.parseInt(input.readLine());
										clientIp = clientIdIpMap.get(clientId);
									} else {
										Map.Entry<Integer, String> entry = clientIdIpMap.entrySet().iterator().next();
										clientIp = entry.getValue();
									}

									if (dOrP.equalsIgnoreCase("D")) {
										downloadFile(clientIp, fileName, out, in);
										System.out.println("The file has downloaded in the current path's sub-directory '/downloads'.");
									} else if (dOrP.equalsIgnoreCase("P")) {
										downloadFile(clientIp, fileName, out, in);
										FileUtil.printFile(fileName);
									}
								} else {
									System.out.print("Download or not? (Y/N): ");
									String download = input.readLine();
									if (download.equalsIgnoreCase("Y")) {
										if (clientIdIpMap.size() > 1) {
											System.out.print("Choose one client id : ");
											int clientId = Integer.parseInt(input.readLine());
											clientIp = clientIdIpMap.get(clientId);
										} else {
											Map.Entry<Integer, String> entry = clientIdIpMap.entrySet().iterator().next();
											clientIp = entry.getValue();
										}
										downloadFile(clientIp, fileName, out, in);
									}
								}
							} else {
								System.out.println((String) serverResponse.getResponseData());
							}
							break;
						case 3:
							System.out.print("\nPlease confirm whether to un-register? (Y/N): ");
							String confirm = input.readLine();

							if (confirm.equalsIgnoreCase("Y")) {
								startTime = System.currentTimeMillis();
								clientRequest = new Request();
								clientRequest.setRequestType("UNREGISTER");
								clientRequest.setRequestData("Unregister files of client [" + ip + "]");
								out.writeObject(clientRequest);

								endTime = System.currentTimeMillis();
								time = (double) Math.round(endTime - startTime) / 1000;
								serverResponse = (Response) in.readObject();
								System.out.println((String) serverResponse.getResponseData() + " Take" + time + " seconds.");
							}
							break;
						case 4:
							(new LogUtil("Client")).print();
							break;
						case 5:
							(new LogUtil("Server")).print();
							break;
						case 6:
							(new LogUtil("Backup")).print();
							break;
						case 7:
							System.out.println("\nAll file are as follows:");
							clientRequest = new Request();
							clientRequest.setRequestType("GET_FILES_LIST");
							clientRequest.setRequestData(null);
							out.writeObject(clientRequest);

							serverResponse = (Response) in.readObject();
							if (serverResponse.getResponseCode() == 200) {
								ConcurrentHashMap<String, ArrayList<String>> spellIpFileMap = (ConcurrentHashMap<String, ArrayList<String>>) serverResponse.getResponseData();
								if (spellIpFileMap.values().size() != 0) {
									for (Map.Entry<String, ArrayList<String>> entry : spellIpFileMap.entrySet()) {
										String[] split = entry.getKey().split("#");
										for (String name : entry.getValue()) {
											System.out.println("Client id: " + split[0] + ", ip: " + split[1] + ", file name: " + name);
										}
									}
								} else {
									System.out.println("No files on this client.");
								}
							}
							break;
						case 8:
							clientRequest = new Request();
							clientRequest.setRequestType("DISCONNECT");
							clientRequest.setRequestData("Disconnecting from server.");
							out.writeObject(clientRequest);
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

		private void downloadFile(String ip, String fileName, ObjectOutputStream out, ObjectInputStream in) {
			boolean isDownloaded = false;
			long startTime = System.currentTimeMillis();

			if (!FileUtil.downloadFile(ip, SERVER_SOCKET_PORT, fileName)) {
				try {
					Request peerRequest = new Request();
					peerRequest.setRequestType("GET_BACKUP_NODES");
					peerRequest.setRequestData("Send list of backup nodes.");
					out.writeObject(peerRequest);

					Response serverResponse = (Response) in.readObject();
					List<String> backupClientIpList = (List<String>) serverResponse.getResponseData();

					for (String backupIp : backupClientIpList) {
						if (FileUtil.downloadFile(backupIp, SERVER_SOCKET_PORT, fileName)) {
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
				System.out.println("File downloaded successfully. Take " + time + " seconds.");
			} else {
				System.out.println("Unable to download file. Try again later.");
			}
		}
	}

	private static class BackupService extends Thread {
		private static ConcurrentHashMap<String, ArrayList<String>> spellIpFileMap = new ConcurrentHashMap<>();

		public BackupService(ConcurrentHashMap<String, ArrayList<String>> spellIpFileMap) {
			BackupService.spellIpFileMap = spellIpFileMap;
		}

		public void run() {
			for (Map.Entry<String, ArrayList<String>> entry : spellIpFileMap.entrySet()) {
				String key = entry.getKey();
				ArrayList<String> value = entry.getValue();
				String ip = key.split("#")[1].trim();
				for (String file : value) {
					backup(ip, file);
				}
			}
			this.interrupt();
		}

		private void backup(String ip, String fileName) {
			FileUtil.backupFile(ip, SERVER_SOCKET_PORT, fileName);
		}
	}
}
