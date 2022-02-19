package ckcsc.asadfgglie.main;

import ckcsc.asadfgglie.Exception.StartInitException;
import ckcsc.asadfgglie.command.CommandData;
import ckcsc.asadfgglie.json.JSONConfig;
import ckcsc.asadfgglie.main.services.Register.ServiceArray;
import ckcsc.asadfgglie.main.services.Register.Services;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Objects;

public class Basic extends ListenerAdapter {
    public final static int MAX_MESSAGE_SEND_LENGTH = 2000;

    private final static Logger logger = LoggerFactory.getLogger(Basic.class.getSimpleName());

    private final static ArrayList<Services> REGISTER_LIST = new ArrayList<>();
    private final static ArrayList<User> ADMIN_USER_LIST = new ArrayList<>();

    /**
     * All about the path of OS must use this path.
     */
    public static String PATH;

    public static JSONConfig REGISTER_CONFIG;
    public static JSONConfig BOT_CONFIG;
    public static JSONConfig ADMIN_USER_CONFIG;

    public static JDA BUILDER;

    public static void main() throws IOException {
        Services.init();

        setAllConfig();

        registerServices();

        setAdminUser();

        try {
            startBot(BOT_CONFIG.getString("TOKEN"));
            BUILDER.updateCommands().queue();
        }
        catch (JSONException e){
            logger.error("BotConfig.json need key \"TOKEN\".", e);
        }
    }

    private static void setAllConfig() throws IOException {
        REGISTER_CONFIG = setConfig("RegisterConfig.json");

        BOT_CONFIG = setConfig("BotConfig.json");

        ADMIN_USER_CONFIG = setConfig("AdminConfig.json", false);
    }

    public static JSONConfig setConfig(String configName) throws IOException{
        return Objects.requireNonNull(setConfig(configName, true));
    }

    public static JSONConfig setConfig(String configName, boolean compulsory) throws IOException {
        BufferedReader configReader;

        if(compulsory) {
            try {
                configReader = new BufferedReader(new InputStreamReader(new FileInputStream(PATH + File.separator + configName)));
                logger.info("Success load \"" + configName + "\"");
            } catch (FileNotFoundException e) {
                logger.error("Couldn't find \"" + configName + "\" in \"" + PATH, e);
                throw new StartInitException("Couldn't find \"" + configName + "\" in \"" + PATH);
            }
        }
        else{
            try {
                configReader = new BufferedReader(new InputStreamReader(new FileInputStream(PATH + File.separator + configName)));
                logger.info("Success load \"" + configName + "\"");
            } catch (FileNotFoundException e) {
                logger.info("Couldn't find \"" + configName + "\" in \"" + PATH);
                logger.info("\"" + configName + "\" is an option to be a config.");
                return null;
            }
        }

        StringBuilder configStr = new StringBuilder();
        String str;

        while((str = configReader.readLine()) != null){
            str = str.trim();
            configStr.append(str);
        }

        configReader.close();

        return new JSONConfig(PATH + File.separator + configName, configStr.toString());
    }

    private static void registerServices(){
        for(Iterator<String> it = REGISTER_CONFIG.keys(); it.hasNext();){
            String serviceClass = it.next();

            JSONObject serviceValues = REGISTER_CONFIG.getJSONObject(serviceClass);

            if(Services.SERVICES_LIST.containsKey(serviceClass)) {
                int serviceNumber = serviceValues.length();

                ServiceArray serviceArray = Services.SERVICES_LIST.get(serviceClass);
                serviceArray.setArraySize(serviceNumber);
                serviceArray.initArray();

                Iterator<String> serviceValueIterator = serviceValues.keys();
                for(int i = 0; serviceValueIterator.hasNext(); i++) {
                    String serviceName = serviceValueIterator.next();

                    Services service = serviceArray.array[i];

                    REGISTER_LIST.add(service);

                    service.basicSetting(serviceValues.getJSONObject(serviceName), serviceName);
                    service.registerByEnvironment(serviceValues.getJSONObject(serviceName));

                    if(REGISTER_LIST.contains(service)) {
                        logger.info("Success register service: " + service);
                    }
                    else {
                        logger.error("Failed register service: " + service);
                    }
                }
            }
            else{
                logger.error("Service Class \"" + serviceClass + "\" isn't exist.");
            }
        }
    }

    private static void setAdminUser() {
        if(ADMIN_USER_CONFIG != null){
            for (Iterator<String> it = ADMIN_USER_CONFIG.keys(); it.hasNext(); ) {
                String user = it.next();
                ADMIN_USER_LIST.add(User.fromId(ADMIN_USER_CONFIG.getLong(user)));
            }
        }
    }

    private static void startBot(String token){

        // Discord music Bot example code
        // We only need 2 gateway intents enabled for this example:
        EnumSet<GatewayIntent> intents = EnumSet.of(
            // We need messages in guilds to accept commands from users
            GatewayIntent.GUILD_MESSAGES,
            // We need voice states to connect to the voice channel
            GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.GUILD_EMOJIS
        );

        try{
            BUILDER = JDABuilder.createDefault(token, intents)
                    .addEventListeners(new Basic())
                    .setActivity(Activity.watching("無情抓違建"))
                    .enableCache(CacheFlag.VOICE_STATE)
                    .build();

            for(Services s: REGISTER_LIST) {
                BUILDER.addEventListener(s);
            }

            BUILDER.awaitReady();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized static void saveConfig(JSONConfig config) {
        BufferedWriter configWriter;
        try {
            configWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(config.fullConfigName)));
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
            return;
        }

        try {
            String configStr = config.toString();

            configWriter.write(configStr, 0, configStr.length());

            configWriter.flush();
            configWriter.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event){
        CommandData cmdData = CommandData.getCmdData(event);
        User author = event.getAuthor();

        if(!cmdData.isCmd || !event.isFromGuild() || event.getAuthor().isBot()){
            return;
        }

        if (cmdData.cmdHeadEqual("info")) {
            info(cmdData.cmd, event);
        }

        if(!cmdData.isTargetSelf()){
            return;
        }
        else if(cmdData.cmdHeadEqual("stopBot")){
            if(!ADMIN_USER_LIST.contains(author)) {
                event.getChannel().sendMessage("Sorry, you are not the admin.").queue();
                return;
            }
            stopBot();
        }
        else if(cmdData.cmdHeadEqual("op")){
            if(!ADMIN_USER_LIST.contains(author)) {
                event.getChannel().sendMessage("Sorry, you are not the admin.").queue();
                return;
            }
            op(event, cmdData);
        }
    }
    private void op(MessageReceivedEvent event, CommandData cmdData){
        String newAdminID;
        try {
            newAdminID = CommandData.getUserID(cmdData.cmd[1]);
        }
        catch (StringIndexOutOfBoundsException e){
            event.getChannel().sendMessage("Usage:\n```\n!op <@someone> <@botName>\n```").queue();
            return;
        }
        if(newAdminID.equals(BUILDER.getSelfUser().getId())){
            event.getChannel().sendMessage("Usage:\n```\n!op <@someone> <@botName>\n```").queue();
            return;
        }
        User newAdmin = Objects.requireNonNull(BUILDER.getUserById(newAdminID));

        addAdmin(newAdmin);

        event.getChannel().sendMessage(newAdmin.getAsMention() + " now is a new admin.").queue();
    }

    private static void addAdmin (User newAdmin) {
        if(!ADMIN_USER_LIST.contains(newAdmin)) {
            ADMIN_USER_LIST.add(newAdmin);
        }

        ADMIN_USER_CONFIG.put(newAdmin.getName(), newAdmin.getIdLong());
        saveConfig(ADMIN_USER_CONFIG);
    }

    private void info(String[] command, MessageReceivedEvent event) {
        if(command.length == 2 && command[1].equals("list")){
            StringBuilder message = new StringBuilder("Services' list:```\n");
            for(String serviceClass :REGISTER_CONFIG.keySet()){
                message.append(serviceClass);
                for(String serviceName :REGISTER_CONFIG.getJSONObject(serviceClass).keySet()){
                    message.append("\n\t")
                           .append(serviceName);
                }
                message.append('\n');
            }

            event.getChannel().sendMessage(message.append("\n```")).queue();
            return;
        }
        else if(command.length != 3){
            event.getChannel().sendMessage("Usage:\n```\n!info <Service class> <Service name>\n```or```\n!info list\n```").queue();
            return;
        }

        try {
            JSONObject jsonObject = REGISTER_CONFIG.getJSONObject(command[1]).getJSONObject(command[2]);
            String json;
            if(jsonObject.getBoolean("isInfoVisible")) {
                jsonObject.remove("isInfoVisible");
                json = JSONConfig.toFormatString(jsonObject.toString());
            }
            else{
                try {
                    json = "{\n\t\"description\": " + jsonObject.getString("description") + "\n}";
                }
                catch (JSONException e){
                    event.getChannel().sendMessage("This service's information is invisible and doesn't have description.").queue();
                    return;
                }
            }
            event.getChannel().sendMessage("```json\n" + json + "\n```").queue();
        }
        catch (JSONException e){
            event.getChannel().sendMessage("Sorry, we don't have this service: `" + command[1] + "` `" + command[2] + "`").queue();
        }
    }

    private void stopBot () {
        logger.info("======================");
        logger.info("");
        logger.info("\tShut down.");
        logger.info("");
        logger.info("======================");
        BUILDER.shutdown();
        System.exit(0);
    }

    public static void removeService(Services service){
        REGISTER_LIST.remove(service);
    }
}
