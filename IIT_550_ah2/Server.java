package IIT_550_ah2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static final int SERVER_SOCKET_PORT = 22233;

    private static final String BACKUP_PATH = "backupfiles/";

    public static void main(String[] args) throws IOException {
        System.out.println("****** SERVER STARTED ******");
        ServerSocket serverSocket = new ServerSocket(SERVER_SOCKET_PORT);
        try {
            while (true) {
                new ServerIn(serverSocket.accept()).start();
            }
        } finally {
            serverSocket.close();
        }
    }

    private static class ServerIn extends Thread {
        private Socket socket;

        private LogUtil log = new LogUtil("Client");

        public ServerIn(Socket socket) {
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