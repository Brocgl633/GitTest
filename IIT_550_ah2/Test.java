package IIT_550_ah2;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

public class Test {

    private static final int SERVER_SOCKET_PORT = 22233;

    private static final String DOWNLOAD_PATH = "downloads/";

    private final static int CLIENT_THREAD_COUNT = 10;

    private final static int DOWNLOADING_COUNT = 10;

    private final static CountDownLatch latch = new CountDownLatch(CLIENT_THREAD_COUNT);

    public static void main(String[] args) {
        BufferedReader input = null;
        try {
            input = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("****** Client-Server mode file downloading system test program ******");
            System.out.println("Please input server ip: ");
            String ip = input.readLine();
            System.out.println("Please input the absolute path file: ");
            String absPath = input.readLine();

            int idx = absPath.lastIndexOf("/");
            String dir = absPath.substring(0, idx + 1);
            String fileName = absPath.substring(idx + 1);

            long startTime = System.currentTimeMillis();
            for (int i = 0; i < CLIENT_THREAD_COUNT; i++) {
                (new DownloadTest(ip, dir, fileName)).start();
            }
            latch.await();
            long endTime = System.currentTimeMillis();
            long time = endTime - startTime;

            File file = new File(DOWNLOAD_PATH + fileName);
            long fileSize = file.length();
            System.out.println("File size: " + fileSize +
                    " bytes, thread count: " + CLIENT_THREAD_COUNT +
                    ", download times: " + DOWNLOADING_COUNT +
                    ", download speed: " + (double) fileSize / time + " byte/ms.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class DownloadTest extends Thread {

        private String ip;

        private String fileDirPath;

        private String fileName;

        public DownloadTest(String ip, String fileDirPath, String fileName) {
            this.ip = ip;
            this.fileDirPath = fileDirPath;
            this.fileName = fileName;
        }

        public void run() {
            System.out.println("Download test starting ...");
            for (int i = 0; i < DOWNLOADING_COUNT; i++) {
                FileUtil.downloadFile(ip, SERVER_SOCKET_PORT, fileName, new ArrayList<>(Collections.singleton(fileDirPath)));
            }
            latch.countDown();
        }
    }
}