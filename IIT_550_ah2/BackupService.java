package IIT_550_ah2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class BackupService extends Thread {

    private static final int SERVER_SOCKET_PORT = 22233;

    private static ConcurrentHashMap<String, ArrayList<String>> spellIpFileMap = new ConcurrentHashMap<>();

    private static List<String> regDirPathList = new ArrayList<>();

    public BackupService(ConcurrentHashMap<String, ArrayList<String>> spellIpFileMap, List<String> regDirPathList) {
        BackupService.spellIpFileMap = spellIpFileMap;
        BackupService.regDirPathList = regDirPathList;
    }

    public void run() {
        for (Map.Entry<String, ArrayList<String>> entry : spellIpFileMap.entrySet()) {
            String key = entry.getKey();
            ArrayList<String> value = entry.getValue();
            String ip = key.split("#")[1].trim();
            for (String file : value) {
                backup(ip, file, regDirPathList);
            }
        }
        this.interrupt();
    }

    private void backup(String ip, String fileName, List<String> regDirPathList) {
        FileUtil.backupFile(ip, SERVER_SOCKET_PORT, fileName, regDirPathList);
    }
}
