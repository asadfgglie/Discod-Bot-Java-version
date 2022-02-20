package ckcsc.asadfgglie.setup;

import ckcsc.asadfgglie.main.Basic;
import ckcsc.asadfgglie.util.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class SetUp {

    public static void main(String[] argv) throws IOException {
        if(argv.length == 0){
            callBasicMain("");
        }
        else if(argv.length == 2 && argv[0].equals(Option.configpath.getOption())) {
            callBasicMain(argv[1]);
        }
        else{
            System.out.println(Option.configpath.getInfo());
        }
    }

    private static void callBasicMain(String path) throws IOException {
        if(path.startsWith(".")) {
            Basic.PATH = Path.transferPath(Path.getPath() + path.substring(1));
        }
        else if(path.equals("")){
            Basic.PATH = Path.transferPath(Path.getPath());
        }
        else {
            Basic.PATH = Path.transferPath(path);
        }
        Basic.main();
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