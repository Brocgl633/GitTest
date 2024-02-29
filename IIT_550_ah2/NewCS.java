package IIT_550_ah2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author : CGL
 * @Date : 2024/2/29 22:18
 * @Desc :
 */
public class NewCS {

    private static List<String> regDirPathList = Collections.synchronizedList(new ArrayList<>());

    private static final int CLIENT_SOCKET_PORT = 12233;

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

    private static class Client extends Thread {
        public void run() {
            Socket socket = null;
            ObjectInputStream in = null;
            BufferedReader input = null;
            ObjectOutputStream out = null;
            IIT_550_n.Request clientRequest = null;
            IIT_550_n.Response serverResponse = null;

            try {
                input = new BufferedReader(new InputStreamReader(System.in));
                System.out.println("Please input Server IP :");
                String ip = input.readLine();
                if (ip.trim().length() == 0 || !IPServer.validate(ip)) {
                    System.out.println("Invalid Server IP.");
                    System.exit(0);
                }

                long startTime, endTime, time;

                socket = new Socket(ip, CLIENT_SOCKET_PORT);
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());
                serverResponse = (IIT_550_n.Response) in.readObject();
                System.out.print((String) serverResponse.getResponseData());

                String isBackup = input.readLine();
                clientRequest = new IIT_550_n.Request();
                clientRequest.setRequestType("BACK_UP");
                clientRequest.setRequestData(isBackup);
                out.writeObject(clientRequest);

                if (isBackup.equalsIgnoreCase("Y")) {
                    regDirPathList.add(BACKUP_PATH);
                    serverResponse = (IIT_550_n.Response) in.readObject();
                    ConcurrentHashMap<String, ArrayList<String>> spellIpFileMap
                            = (ConcurrentHashMap<String, ArrayList<String>>) serverResponse.getResponseData();
                    new BackupService(spellIpFileMap, regDirPathList).start();
                }

                // handle previous map of ip to directory
                serverResponse = (IIT_550_n.Response) in.readObject();
                ArrayList<String> ipDirList = (ArrayList<String>) serverResponse.getResponseData();
                if (ipDirList != null) {
                    for (String x : ipDirList) {
                        if (!regDirPathList.contains(x)) {
                            regDirPathList.add(x);
                        }
                    }
                }

                while (true) {
                    System.out.println("\n****** Client-Server mode file downloading system ******");
                    System.out.println("1.Register files");
                    System.out.println("2.Get files list");
                    System.out.println("3.Search or download or print a file");
                    System.out.println("4.Unregister files of client [" + ip + "]");
                    System.out.println("5.Print client log of client [" + ip + "]");
                    System.out.println("6.Print server log of client [" + ip + "]");
                    System.out.println("7.Print backup log of client [" + ip + "]");
                    System.out.println("8.Exit");
                    System.out.print("Please input number : ");

                    int option;

                    try {
                        option = Integer.parseInt(input.readLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Input is wrong. Please try again.");
                        continue;
                    }

                    switch (option) {
                        case 1:
                            System.out.println("The path of the registered files : ");
                            String path = input.readLine();
                            if (path.trim().length() == 0) {
                                System.out.println("Invalid Path.");
                                continue;
                            }

                            ArrayList<String> files = IIT_550_n.FileUtil.getFiles(path);
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

                                clientRequest = new IIT_550_n.Request();
                                clientRequest.setRequestType("REGISTER");
                                Object[] data = new Object[2];
                                data[0] = files;
                                data[1] = regDirPathList;
                                clientRequest.setRequestData(data);
                                out.writeObject(clientRequest);

                                serverResponse = (IIT_550_n.Response) in.readObject();
                                endTime = System.currentTimeMillis();
                                time = endTime - startTime;

                                if (serverResponse.getResponseCode() == 200) {
                                    System.out.println(
                                            (files.size() - 1) + " files have registered. Take " + time + " ms.");
                                } else {
                                    System.out.println("Unable to register files.");
                                }
                            } else {
                                System.out.println("There are no more files in path [ " + path + " ].");
                            }
                            break;
                        case 2:
                            System.out.println("\nAll file are as follows:");
                            clientRequest = new IIT_550_n.Request();
                            clientRequest.setRequestType("GET_FILES_LIST");
                            clientRequest.setRequestData("Get all files list.");
                            out.writeObject(clientRequest);

                            serverResponse = (IIT_550_n.Response) in.readObject();
                            if (serverResponse.getResponseCode() == 200) {
                                List<String> allFileInfoList = (List<String>) serverResponse.getResponseData();
                                for (String fileInfo : allFileInfoList) {
                                    System.out.println(fileInfo);
                                }
                            }
                            break;
                        case 3:
                            System.out.println("The file name you want to search : ");
                            String fileName = input.readLine();
                            String clientIp;

                            startTime = System.currentTimeMillis();

                            clientRequest = new IIT_550_n.Request();
                            clientRequest.setRequestType("SEARCH");
                            clientRequest.setRequestData(fileName);
                            out.writeObject(clientRequest);

                            serverResponse = (IIT_550_n.Response) in.readObject();
                            endTime = System.currentTimeMillis();
                            time = endTime - startTime;
                            if (serverResponse.getResponseCode() == 200) {
                                System.out.println("File found. Searching takes " + time + " ms.");

                                HashMap<Integer, String> clientIdIpMap
                                        = (HashMap<Integer, String>) serverResponse.getResponseData();
                                if (clientIdIpMap != null) {
                                    for (Map.Entry<Integer, String> entry : clientIdIpMap.entrySet()) {
                                        System.out.println("Client id : " + entry.getKey());
                                        System.out.println("Client ip : " + entry.getValue());
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

                                    downloadFile(clientIp, fileName, out, in);
                                    System.out.println(
                                            "The file has downloaded in the current path's sub-directory '/downloads'.");
                                    if (dOrP.equalsIgnoreCase("P")) {
                                        IIT_550_n.FileUtil.printFile(fileName);
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
                                            Map.Entry<Integer, String> entry = clientIdIpMap.entrySet()
                                                    .iterator()
                                                    .next();
                                            clientIp = entry.getValue();
                                        }
                                        downloadFile(clientIp, fileName, out, in);
                                    }
                                }
                            } else {
                                System.out.println((String) serverResponse.getResponseData());
                            }
                            break;
                        case 4:
                            System.out.print("Please confirm whether to un-register? (Y/N): ");
                            String confirm = input.readLine();

                            if (confirm.equalsIgnoreCase("Y")) {
                                startTime = System.currentTimeMillis();
                                clientRequest = new IIT_550_n.Request();
                                clientRequest.setRequestType("UNREGISTER");
                                clientRequest.setRequestData("Unregister files of client [" + ip + "]");
                                out.writeObject(clientRequest);

                                endTime = System.currentTimeMillis();
                                time = endTime - startTime;
                                serverResponse = (IIT_550_n.Response) in.readObject();
                                System.out.println(
                                        (String) serverResponse.getResponseData() + " ,Take " + time + " ms.");
                            }
                            break;
                        case 5:
                            (new IIT_550_n.LogUtil("Client")).print();
                            break;
                        case 6:
                            (new IIT_550_n.LogUtil("Server")).print();
                            break;
                        case 7:
                            (new LogUtil("Backup")).print();
                            break;
                        case 8:
                            clientRequest = new IIT_550_n.Request();
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

            if (!IIT_550_n.FileUtil.downloadFile(ip, SERVER_SOCKET_PORT, fileName, regDirPathList)) {
                try {
                    Request peerRequest = new Request();
                    peerRequest.setRequestType("GET_BACKUP_NODES");
                    peerRequest.setRequestData("Send list of backup nodes.");
                    out.writeObject(peerRequest);

                    Response serverResponse = (Response) in.readObject();
                    List<String> backupClientIpList = (List<String>) serverResponse.getResponseData();

                    for (String backupIp : backupClientIpList) {
                        if (FileUtil.downloadFile(backupIp, SERVER_SOCKET_PORT, fileName, regDirPathList)) {
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
            long time = endTime - startTime;

            if (isDownloaded) {
                System.out.println("File downloaded successfully. Take " + time + " ms.");
            } else {
                System.out.println("Unable to download file. Try again later.");
            }
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
                    logPrint("Serving DOWNLOAD request for " + clientIp);
                    Object[] data = (Object[]) request.getRequestData();
                    String fileName = (String) data[0];
                    List<String> regDirPathList = (List<String>) data[1];
                    String fileDirPath = FileUtil.getFileLocation(fileName, regDirPathList);
                    File file = new File(fileDirPath + fileName);
                    logPrint("Downloading file: " + fileDirPath + fileName);
                    byte[] mybytearray = new byte[(int) file.length()];
                    fileInput = new BufferedInputStream(new FileInputStream(file));
                    fileInput.read(mybytearray, 0, mybytearray.length);
                    System.out.println("Downloading file byteArray: " + mybytearray.length + " bytes.");
                    out = socket.getOutputStream();
                    out.write(mybytearray, 0, mybytearray.length);
                    out.flush();
                    logPrint("File sent successfully.");
                    System.out.println();
                } else if (request.getRequestType().equalsIgnoreCase("UPDATE_BACKUP_DATA")) {
                    logPrint("Serving UPDATE_BACKUP_DATA request for " + clientIp);
                    logPrint("UPDATE_BACKUP_DATA start ...");
                    Object[] data = (Object[]) request.getRequestData();
                    ConcurrentHashMap<String, ArrayList<String>> spellIpFileMap = (ConcurrentHashMap<String, ArrayList<String>>) data[0];
                    List<String> regDirPathList = (List<String>) data[1];
                    System.out.println("UPDATE_BACKUP_DATA get spellIpFileMap: " + spellIpFileMap.toString());
                    new BackupService(spellIpFileMap, regDirPathList).start();
                } else if (request.getRequestType().equalsIgnoreCase("REMOVE_FILE")) {
                    logPrint("Serving REMOVE_FILE request for " + clientIp);
                    logPrint("REMOVE_FILE start ...");
                    ArrayList<String> deleteFileList = (ArrayList<String>) request.getRequestData();
                    System.out.println("REMOVE_FILE list: " + deleteFileList.toString());
                    for (String fileName : deleteFileList) {
                        File file = new File(BACKUP_PATH + fileName);
                        file.delete();
                    }
                }
            } catch (Exception e) {
                logPrint("ERROR: " + e);
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

        private void logPrint(String message) {
            log.write(message);
            System.out.println(message);
        }
    }
}
