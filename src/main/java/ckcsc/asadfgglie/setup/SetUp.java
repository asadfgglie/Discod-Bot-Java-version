package ckcsc.asadfgglie.setup;

import ckcsc.asadfgglie.main.Basic;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class SetUp {
    public static void main(String[] argv) throws IOException {
        if(argv.length == 0){
            callBasicMain("");
        }
        else if(argv.length == 2){
            if(argv[0].equals(Option.configpath.getOption())){
                callBasicMain(argv[1]);
            }
            else{
                System.err.println(Option.configpath.getInfo());
            }
        }
        else{
            System.err.println(Option.configpath.getInfo());
        }
    }

    private static void callBasicMain(String path) throws IOException {
        if(path.startsWith(".")) {
            Basic.PATH = transferPath(getPath() + path.substring(1));
        }
        else if(path.equals("")){
            Basic.PATH = transferPath(getPath());
        }
        else {
            Basic.PATH = transferPath(path);
        }
        Basic.main();
    }

    private static String getPath() {
        URL url = SetUp.class.getProtectionDomain().getCodeSource().getLocation();
        String filePath = null;
        try {
            filePath = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8);// 轉化為utf-8編碼
        } catch (Exception e) {
            System.err.println("Couldn't get this jar-file local path.");
            e.printStackTrace();
            System.exit(1);
        }
        filePath = filePath.substring(1);
        if (filePath.endsWith(".jar")) {// 可執行jar包執行的結果裡包含".jar"
            // 擷取路徑中的jar包名
            filePath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
        }
        filePath = filePath.substring(0, filePath.lastIndexOf("/"));
        return filePath;
    }
    private static String transferPath(String path){
        path = path.replace("\\", File.separator);
        path = path.replace("/", File.separator);

        String osName = System.getProperty("os.name").toLowerCase();
        if(osName.contains("linux")){
            path = File.separator + path;
        }

        return path;
    }
}

enum Option{
    configpath("--configpath",

"Usage:\n" +
    "java -jar <BotJar>.jar --configpath <config-folder path>\n\n" +

    "Your config-folder must contain the json file to set up the bot.\n\n" +

    "If you don't set --configpath, Bot will use the Bot's current directory on DEFAULT.)\n\n" +

    "Only can use two path representation:\n" +
    "    Absolute path\n" +
    "    ./<dictionary>\n\n" +

    "./ means the current directory.");

    private final String option, info;

    Option(String option, String info){
        this.option = option;
        this.info = info;
    }

    public String getOption() {
        return option;
    }

    public String getInfo() {
        return info;
    }
}