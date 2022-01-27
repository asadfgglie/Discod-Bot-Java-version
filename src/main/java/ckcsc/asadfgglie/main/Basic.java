package ckcsc.asadfgglie.main;

import ckcsc.asadfgglie.main.command.CommandData;
import ckcsc.asadfgglie.main.json.JSONConfig;
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
import net.dv8tion.jda.internal.entities.UserById;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Objects;

public class Basic extends ListenerAdapter {
    public static ArrayList<Services> REGISTER_LIST = new ArrayList<>();
    public static ArrayList<User> ADMIN_USER_LIST = new ArrayList<>();

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
            System.err.println("BotConfig.json need key \"TOKEN\".");
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
                System.out.println("Success load \"" + configName + "\"");
            } catch (FileNotFoundException e) {
                throw new FileNotFoundException("Couldn't find \"" + configName + "\" in \"" + PATH);
            }
        }
        else{
            try {
                configReader = new BufferedReader(new InputStreamReader(new FileInputStream(PATH + File.separator + configName)));
                System.out.println("Success load \"" + configName + "\"");
            } catch (FileNotFoundException e) {
                System.out.println("Couldn't find \"" + configName + "\" in \"" + PATH);
                System.out.println("\"" + configName + "\" is an option to be a config.");
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

                    service.registerByEnvironment(serviceValues.getJSONObject(serviceName), serviceName);

                    REGISTER_LIST.add(service);

                    System.out.println("Success register service: " + service);
                }
            }
            else{
                System.err.println("Service Class \"" + serviceClass + "\" isn't exist.");
            }
        }
    }

    private static void setAdminUser() {
        if(ADMIN_USER_CONFIG != null){
            for (Iterator<String> it = ADMIN_USER_CONFIG.keys(); it.hasNext(); ) {
                String user = it.next();
                ADMIN_USER_LIST.add(new UserById(ADMIN_USER_CONFIG.getLong(user)));
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
            GatewayIntent.GUILD_VOICE_STATES
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
        if(cmdData.cmdHeadEqual("stopBot")){
            if(ADMIN_USER_LIST.contains(author)) {
                stopBot(cmdData);
            }
            else {
                event.getChannel().sendMessage("Sorry, you are not the admin.").queue();
            }
        }
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
            }

            event.getChannel().sendMessage(message.append("\n```")).queue();
            return;
        }
        else if(command.length != 3){
            event.getChannel().sendMessage("\nUsage:\n```\n!info <Service class> <Service name>\n```or```\n!info list\n```").queue();
            return;
        }

        try {
            String json = JSONConfig.toFormatString(REGISTER_CONFIG.getJSONObject(command[1]).getJSONObject(command[2]).toString());
            event.getChannel().sendMessage("```json\n" + json + "\n```").queue();
        }
        catch (JSONException e){
            event.getChannel().sendMessage("Sorry, we don't have this service: `" + command[1] + "` `" + command[2] + "`").queue();
        }
    }

    private void stopBot(CommandData cmdData) {
        if(cmdData.cmd[1].equals(BUILDER.getSelfUser().getAsMention().replace("<@", "<@!"))) {
            System.out.println("======================\n\n\tShut down.\n\n======================");
            BUILDER.shutdown();
            System.exit(0);
        }
    }
}
