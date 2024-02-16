import java.io.*;
import java.net.*;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileServer {
    private ServerSocket serverSocket;
    private Map<String, Long> fileLastModifiedMap;

    public FileServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        fileLastModifiedMap = new HashMap<>();
    }

    // 启动服务器
    public void start() {
        System.out.println("Server started. Listening on port " + serverSocket.getLocalPort());

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 处理客户端请求
    private void handleClient(Socket clientSocket) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

            // 读取客户端请求类型
            String requestType = (String) in.readObject();

            switch (requestType) {
                case "get_files_list":
                    sendFilesList(out);
                    break;
                case "download_file":
                    downloadFile(in, out);
                    break;
                default:
                    System.err.println("Unknown request type from client: " + requestType);
            }

            clientSocket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // 发送文件列表给客户端
    private void sendFilesList(ObjectOutputStream out) throws IOException {
        File folder = new File("server_directory");
        File[] files = folder.listFiles();

        List<String> fileList = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                fileList.add(file.getName());
                fileLastModifiedMap.put(file.getName(), file.lastModified());
            }
        }

        out.writeObject(fileList);
    }

    // 下载文件给客户端
    private void downloadFile(ObjectInputStream in, ObjectOutputStream out) throws IOException {
        try {
            String fileName = (String) in.readObject();

            if (fileLastModifiedMap.containsKey(fileName)) {
                long lastModified = fileLastModifiedMap.get(fileName);

                File file = new File("server_directory", fileName);

                if (file.exists() && file.lastModified() == lastModified) {
                    sendFileContent(file, out);
                } else {
                    out.writeObject("FileNotExist");
                }
            } else {
                out.writeObject("FileNotExist");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // 发送文件内容给客户端
    private void sendFileContent(File file, ObjectOutputStream out) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            out.flush();
        }
    }

    // 计算文件的MD5值
    private String calculateMD5(File file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(file);

            byte[] dataBytes = new byte[1024];

            int bytesRead;

            while ((bytesRead = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, bytesRead);
            }

            byte[] mdBytes = md.digest();

            StringBuilder sb = new StringBuilder();

            for (byte mdByte : mdBytes) {
                sb.append(Integer.toString((mdByte & 0xff) + 0x100, 16).substring(1));
            }

            fis.close();

            return sb.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        int port = 8888; // 默认端口

        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        try {
            FileServer server = new FileServer(port);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}






import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class FileClient {
    private Socket socket;
    private String serverIP;
    private int serverPort;

    public FileClient(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    // Connect to the server
    public void connect() {
        try {
            socket = new Socket(serverIP, serverPort);
            System.out.println("Connected to the server.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Request the list of files from the server
    public List<String> getFilesList() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            out.writeObject("get_files_list");

            return (List<String>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Download a file from the server
    public void downloadFile(String fileName, String localPath) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            out.writeObject("download_file");
            out.writeObject(fileName);

            String response = (String) in.readObject();

            if (!response.equals("FileNotExist")) {
                receiveFileContent(localPath, in);
                verifyFileIntegrity(fileName, localPath);
            } else {
                System.out.println("File does not exist on the server.");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Receive file content from the server
    private void receiveFileContent(String localPath, ObjectInputStream in) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(localPath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }

            fileOutputStream.flush();
            System.out.println("File downloaded successfully.");
        }
    }

    // Verify file integrity using MD5 checksums
private void verifyFileIntegrity(String fileName, String localPath) {
    File serverFile = new File("server_directory", fileName);
    File localFile = new File(localPath);

    String serverFileMD5 = calculateMD5(serverFile);
    String localFileMD5 = calculateMD5(localFile);

    System.out.println("Verifying file integrity...");

    if (serverFileMD5 != null && localFileMD5 != null) {
        System.out.println("Server MD5: " + serverFileMD5);

        if (serverFileMD5.equals(localFileMD5)) {
            System.out.println("File integrity verified. Download successful.");
        } else {
            System.out.println("File integrity check failed. Downloaded file may be corrupted.");
        }
    } else {
        System.out.println("Unable to calculate MD5 for the server or downloaded file.");
    }
}


    // Calculate MD5 checksum of a file
    private String calculateMD5(File file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(file);

            byte[] dataBytes = new byte[1024];
            int bytesRead;

            while ((bytesRead = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, bytesRead);
            }

            byte[] mdBytes = md.digest();

            StringBuilder sb = new StringBuilder();

            for (byte mdByte : mdBytes) {
                sb.append(Integer.toString((mdByte & 0xff) + 0x100, 16).substring(1));
            }

            fis.close();

            return sb.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java FileClient <serverIP> <serverPort>");
            System.exit(1);
        }

        String serverIP = args[0];
        int serverPort = Integer.parseInt(args[1]);

        FileClient client = new FileClient(serverIP, serverPort);
        client.connect();

        List<String> filesList = client.getFilesList();

        if (filesList != null && !filesList.isEmpty()) {
            System.out.println("Files available on the server:");
            for (String fileName : filesList) {
                System.out.println(fileName);
            }

            // Assuming the user wants to download the first file from the list
            String fileToDownload = filesList.get(0);

            // Specify the local path where the file will be saved
            String localPath = "downloaded_files/" + fileToDownload;

            client.downloadFile(fileToDownload, localPath);
        } else {
            System.out.println("No files available on the server.");
        }
    }
}
