package ckcsc.asadfgglie.util;

import ckcsc.asadfgglie.setup.SetUp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class Path {
    private static final Logger logger = LoggerFactory.getLogger(Path.class.getSimpleName());

    private Path(){}

    public static String getPath() {
        URL url = SetUp.class.getProtectionDomain().getCodeSource().getLocation();
        String filePath = null;
        try {
            filePath = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8);// 轉化為utf-8編碼
        } catch (Exception e) {
            logger.error("Couldn't get this jar-file local path.", e);
            System.exit(1);
        }
        filePath = filePath.substring(1);
        if (filePath.endsWith(".jar")) {// 可執行jar包執行的結果裡包含".jar"
            // 擷取路徑中的jar包名
            filePath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
        }
        if(filePath.lastIndexOf("/") != -1) {
            filePath = filePath.substring(0, filePath.lastIndexOf("/"));
        }
        return Path.transferPath(filePath);
    }

    public static String transferPath(String path){
        path = path.replace("\\", File.separator);
        path = path.replace("/", File.separator);

        String osName = System.getProperty("os.name").toLowerCase();
        if(osName.contains("linux") && !path.startsWith("/")){
            path = File.separator + path;
        }

        return path;
    }
}
