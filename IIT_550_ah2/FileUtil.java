package IIT_550_ah2;

import java.io.*;
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
     * Find the standard path of file directory in the directory list.
     * @param fileName	search file name
     * @param dirList	list of all the directory
     * @return			the standard path of the input parameter file's directory
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
     * @param ip				download file in this ip
     * @param port				download file in this ip's port
     * @param fileName			name of the file
     * @param regDirPathList	registered files' directory
     * @return			true: download successfully; false: download unsuccessfully
     */
    public static boolean downloadFile(String ip, int port, String fileName, List<String> regDirPathList) {
        InputStream in = null;
        BufferedOutputStream fileOutput = null;
        ObjectOutputStream out = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        FileOutputStream fileOutputStream = null;
        Socket socket = null;
        boolean isDownloaded = false;

        try {
            System.out.println("Downloading file: " + fileName);

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
            Object[] data = new Object[2];
            data[0] = fileName;
            data[1] = regDirPathList;
            request.setRequestData(data);
            out.writeObject(request);

            System.out.println("Downloading file ...");
            byte[] mybytearray = new byte[BUFFER_SIZE];
            in = socket.getInputStream();
            byteArrayOutputStream = new ByteArrayOutputStream();
            int bytesRead;
            while ((bytesRead = in.read(mybytearray, 0, mybytearray.length)) > 0) {
                byteArrayOutputStream.write(mybytearray, 0, bytesRead);
            }
            byteArrayOutputStream.flush();
            mybytearray = byteArrayOutputStream.toByteArray();

            fileOutputStream = new FileOutputStream(DOWNLOAD_PATH + fileName);
            fileOutputStream.write(mybytearray);
            File curFile = new File(DOWNLOAD_PATH + fileName);
            if(curFile.length() == 0) {
                isDownloaded = false;
                curFile.delete();
            } else {
                isDownloaded = true;
            }
        } catch(SocketException e) {
            isDownloaded = false;
        } catch (Exception e) {
            isDownloaded = false;
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                if (out != null) {
                    out.close();
                }
                if (fileOutput != null) {
                    fileOutput.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return isDownloaded;
    }

    /***
     * Backup the file from the client(ip). (like downloadFile())
     * @param ip		        backup file in this ip
     * @param port		        backup file in this ip's port
     * @param fileName	        name of the file
     * @param regDirPathList	registered files' directory
     * @return		    true: backup successfully; false: backup unsuccessfully
     */
    public static boolean backupFile(String ip, int port, String fileName, List<String> regDirPathList) {
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
            Object[] data = new Object[2];
            data[0] = fileName;
            data[1] = regDirPathList;
            request.setRequestData(data);
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
