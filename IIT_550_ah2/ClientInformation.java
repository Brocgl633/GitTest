package IIT_550_ah2;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClientInformation {

    private static final int CLIENT_SOCKET_PORT = 12233;

    private static final int SERVER_SOCKET_PORT = 22233;

    private static final String BACKUP_PATH = "backupfiles/";

    /***
     * key: client number # ip # timestamp
     * value: [the list of file registered at timestamp on client number and ip]
     * eg. {1#192.168.1.11#1708353067375=[graph1.jpg, graph2.jpg, sub_files.rar, text1.txt, text2.txt]}
     */
    private static ConcurrentHashMap<String, ArrayList<String>> spellIpFileMap = new ConcurrentHashMap<>();

    /**
     * key: ip
     * value: [the list of the directories of files stored under this client(ip)]
     * eg. {192.168.1.11=[C:/Users/Administrator/Downloads/iit_550_test_files/]}
     */
    private static ConcurrentHashMap<String, ArrayList<String>> ipDirMap = new ConcurrentHashMap<>();

    /**
     * list of ip of backup's client
     */
    private static List<String> backupClientIpList = Collections.synchronizedList(new ArrayList<>());

    private static int clientCount = 0;

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
            logPrint("New connection ---> client id: " + clientNumber + ", ip: " + socket.getInetAddress().getHostAddress());
            clientCount++;
            logPrint("There are " + clientCount + " clients connected with the server.");
            System.out.println();
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
                    System.out.println("Client [ " + clientNumber + " ] set to be a backup node.\n");

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
                        logPrint("Start registering a file ...");
                        Object[] data = (Object[]) clientRequest.getRequestData();
                        ArrayList<String> registerFileList = (ArrayList<String>) data[0];
                        List<String> regDirPathList = (List<String>) data[1];
                        ArrayList<String> fileList = register(clientIp, registerFileList, regDirPathList);
                        response = new Response();
                        response.setResponseCode(200);
                        response.setResponseData(fileList);
                        out.writeObject(response);
                    } else if (requestType.equalsIgnoreCase("SEARCH")) {
                        logPrint("Start searching a file ...");
                        String fileName = (String) clientRequest.getRequestData();
                        logPrint("Request from client id [ " + clientNumber + " ] and ip [ " + clientIp + " ] to search file: " + fileName);
                        HashMap<Integer, String> res = search(fileName);
                        if (res.size() > 0) {
                            response = new Response();
                            response.setResponseCode(200);
                            response.setResponseData(res);
                            out.writeObject(response);
                            logPrint("File found.");
                        } else {
                            response = new Response();
                            response.setResponseCode(404);
                            response.setResponseData("File not found.");
                            out.writeObject(response);
                            logPrint("File Not Found.");
                        }
                        System.out.println();
                    } else if (requestType.equalsIgnoreCase("UNREGISTER")) {
                        logPrint("Start unregister a file ...");
                        response = new Response();
                        if (unregister(clientIp)) {
                            response.setResponseCode(200);
                            response.setResponseData("Files in ip [ " + clientIp + " ] have been unregistered.");
                            logPrint("Client id [ " + clientNumber + " ], ip [" + clientIp + "] has unregistered all files.");
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
                        logPrint("Start getting files list ...");
                        List<String> allFileInfoList = getAllFileInfo();
                        response = new Response();
                        response.setResponseCode(200);
                        response.setResponseData(allFileInfoList);
                        out.writeObject(response);
                        logPrint("All files information sent.");
                        logPrint(spellIpFileMap.toString());
                        System.out.println();
                    } else if (requestType.equalsIgnoreCase("DISCONNECT")) {
                        logPrint("Start disconnecting to client id [ " + clientNumber + " ] ...");
                        try {
                            socket.close();
                        } catch (IOException e) {
                            logPrint("DISCONNECT error: " + e);
                        }
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (EOFException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logPrint("Error handling client id [ " + clientNumber + " ]: " + e);
                Thread.currentThread().interrupt();
            }
        }

        private List<String> getAllFileInfo() {
            List<String> res = new ArrayList<>();
            if (spellIpFileMap.values().size() != 0) {
                for (Map.Entry<String, ArrayList<String>> entry : spellIpFileMap.entrySet()) {
                    String[] split = entry.getKey().split("#");
                    for (String name : entry.getValue()) {
                        res.add("Client id: " + split[0] + ", ip: " + split[1] + ", file name: " + name);
                    }
                }
            } else {
                res.add("No files on this client.");
            }
            return res;
        }

        public void interrupt() {
            logPrint("Connection in client id [ " + clientNumber + " ] closed.");
            clientCount--;
            logPrint("There are " + clientCount + " clients connected with the server.");
            if (clientCount == 0) {
                logPrint("There are no clients connected with the server.");
            }
            System.out.println();
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
         * @param regDirPathList    registered files' directory
         * @return list of registered files name
         * @throws IOException
         */
        private ArrayList<String> register(String ip, ArrayList<String> registerFileList, List<String> regDirPathList) throws IOException {
            logPrint("Start registering files from client: " + ip);

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

            logPrint("" + registerFileList.size() + " files have registered in client [ " + clientNumber + " ] and added into spellIpFileMap.\n");

            ConcurrentHashMap<String, ArrayList<String>> curSpellIpFileMap = new ConcurrentHashMap<>();
            curSpellIpFileMap.put(sb.toString(), registerFileList);
            backupFiles(curSpellIpFileMap, regDirPathList);

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
                    logPrint("Error remove in backup node : " + e);
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
         * @param regDirPathList    registered files' directory
         */
        private void backupFiles(ConcurrentHashMap<String, ArrayList<String>> curSpellIpFileMap, List<String> regDirPathList) throws IOException {
            Request request = new Request();
            Socket socket = null;
            try {
                request.setRequestType("UPDATE_BACKUP_DATA");
                for (String node : backupClientIpList) {
                    socket = new Socket(node, SERVER_SOCKET_PORT);
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    Object[] data = new Object[2];
                    data[0] = curSpellIpFileMap;
                    data[1] = regDirPathList;
                    request.setRequestData(data);
                    out.writeObject(request);
                    out.close();
                    socket.close();
                }
                socket = null;
            } catch (Exception e) {
                logPrint("Error in back up:" + e);
            } finally {
                if (socket != null && socket.isConnected()) {
                    socket.close();
                }
            }
        }
    }
}