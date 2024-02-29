package IIT_550_ah2;

import java.io.*;
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