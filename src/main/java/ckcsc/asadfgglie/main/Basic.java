package ckcsc.asadfgglie.main;

import ckcsc.asadfgglie.main.services.Register.ServiceArray;
import ckcsc.asadfgglie.main.services.Register.Services;
import ckcsc.asadfgglie.util.Exception.StartInitException;
import ckcsc.asadfgglie.util.command.CommandData;
import ckcsc.asadfgglie.util.json.JSONConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    public final static ArrayList<Long> ADMIN_USER_LIST = new ArrayList<>();

    /**
     * All config path.
     */
    public static String CONFIG_PATH;

    public static String SCRIPT_PATH;

    public static JSONConfig REGISTER_CONFIG;
    public static JSONConfig BOT_CONFIG;
    public static @Nullable JSONConfig ADMIN_USER_CONFIG;

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

    public static @Nullable JSONConfig setConfig(String configName, boolean compulsory) throws IOException {
        BufferedReader configReader;

        if(compulsory) {
            try {
                configReader = new BufferedReader(new InputStreamReader(new FileInputStream(CONFIG_PATH + File.separator + configName)));
                logger.info("Success load \"" + configName + "\"");
            } catch (FileNotFoundException e) {
                logger.error("Couldn't find \"" + configName + "\" in \"" + CONFIG_PATH, e);
                throw new StartInitException("Couldn't find \"" + configName + "\" in \"" + CONFIG_PATH);
            }
        }
        else{
            try {
                configReader = new BufferedReader(new InputStreamReader(new FileInputStream(CONFIG_PATH + File.separator + configName)));
                logger.info("Success load \"" + configName + "\"");
            } catch (FileNotFoundException e) {
                logger.info("Couldn't find \"" + configName + "\" in \"" + CONFIG_PATH);
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

        return new JSONConfig(CONFIG_PATH + File.separator + configName, configStr.toString());
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
                ADMIN_USER_LIST.add(ADMIN_USER_CONFIG.getLong(user));
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
            GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.DIRECT_MESSAGES,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_MESSAGE_REACTIONS
        );

        try{
            BUILDER = JDABuilder.createDefault(token, intents)
                    .addEventListeners(new Basic())
                    .setActivity(Activity.of(Activity.ActivityType.valueOf(BOT_CONFIG.getString("Activity_type")), BOT_CONFIG.getString("Activity")))
                    .enableCache(CacheFlag.VOICE_STATE)
                    .disableCache(CacheFlag.SCHEDULED_EVENTS)
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
            logger.error(e.getMessage(), e);
            return;
        }

        try {
            String configStr = config.toString();

            configWriter.write(configStr, 0, configStr.length());

            configWriter.flush();
            configWriter.close();
        }
        catch (IOException e){
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event){
        CommandData cmdData = CommandData.getCmdData(event);
        User author = event.getAuthor();

        if(author.isBot()){
            return;
        }
        if(!cmdData.isCmd){
            return;
        }
        if(!event.isFromGuild() && !ADMIN_USER_LIST.contains(author.getIdLong())){
            return;
        }

        if (cmdData.cmdHeadEqual("info")) {
            assert cmdData.cmd != null;
            info(cmdData.cmd, event);
        }

        if(cmdData.isTargetSelf()){
            return;
        }
        else if(cmdData.cmdHeadEqual("stopBot")){
            if(!ADMIN_USER_LIST.contains(author.getIdLong())) {
                event.getChannel().sendMessage("Sorry, you are not the admin.").queue();
                return;
            }
            stopBot();
        }
        else if(cmdData.cmdHeadEqual("op")){
            if(!ADMIN_USER_LIST.contains(author.getIdLong())) {
                event.getChannel().sendMessage("Sorry, you are not the admin.").queue();
                return;
            }
            op(event, cmdData);
        }
    }

    private void op(MessageReceivedEvent event, CommandData cmdData){
        String newAdminID;
        try {
            assert cmdData.cmd != null;
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
        if(!ADMIN_USER_LIST.contains(newAdmin.getIdLong())) {
            ADMIN_USER_LIST.add(newAdmin.getIdLong());
        }

        assert ADMIN_USER_CONFIG != null;
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
                json = jsonObject.toString(1);
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

    private synchronized void stopBot () {
        logger.info("======================");
        logger.info("");
        logger.info("\tShut down.");
        logger.info("");
        logger.info("======================");
        BUILDER.shutdown();

        try {
            wait(5000);
        }
        catch (InterruptedException ignore) {}
        System.exit(0);
    }

    public static void removeService(Services service){
        REGISTER_LIST.remove(service);
    }
}
