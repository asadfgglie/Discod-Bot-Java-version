package ckcsc.asadfgglie.main;

import ckcsc.asadfgglie.main.json.JSONConfig;
import ckcsc.asadfgglie.main.services.Register.ServiceArray;
import ckcsc.asadfgglie.main.services.Register.Services;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.entities.UserById;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Objects;

public class Basic extends ListenerAdapter {
    public static ArrayList<Services> REGISTER_LIST = new ArrayList<>();
    public static ArrayList<User> ADMIN_USER_LIST = new ArrayList<>();

    public static ArrayList<CommandData> COMMAND_LIST = new ArrayList<>();

    public static String PATH;

    public static JSONConfig REGISTER_CONFIG;
    public static JSONConfig BOT_CONFIG;
    public static JSONConfig ADMIN_USER_CONFIG;

    public static JDA BUILDER;

    public static void main() throws IOException {
        Services.init();

        setAllConfig();

        setDefaultCommand();

        registerServices();

        setAdminUser();

        try {
            startBot(BOT_CONFIG.getString("TOKEN"));
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

    public static @NotNull JSONConfig setConfig(String configName) throws IOException{
        return Objects.requireNonNull(setConfig(configName, true));
    }

    public static @Nullable JSONConfig setConfig(String configName, boolean compulsory) throws IOException {
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

    private static void setDefaultCommand(){
        COMMAND_LIST.add(
            new CommandData("stop",
                            "Stop the bot.")
                    .addOptions(new OptionData(OptionType.STRING,
                                                "id",
                                                "The bot's ID",
                                                true)));
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

            // Discord slash Bot example code
            // These commands take up to an hour to be activated after creation/update/delete
            CommandListUpdateAction commands = BUILDER.updateCommands();

            commands.addCommands(COMMAND_LIST).queue();

            BUILDER.awaitReady();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized static void saveConfig(@NotNull JSONConfig config) {
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
    public void onMessageReceived(@NotNull MessageReceivedEvent event){}

    @Override
    public void onSlashCommand(@Nonnull SlashCommandEvent event) {
        System.out.println(event.getCommandString());

        if(!ADMIN_USER_LIST.contains(Objects.requireNonNull(event.getMember()).getUser())){
            InteractionHook hook = event.getHook();
            hook.sendMessage("Sorry, You are not an admin-user.").queue();
            return;
        }
        if ("stop".equals(event.getName())) {
            stopBot(Objects.requireNonNull(event.getOption("id")));
        }
    }

    private void stopBot(@NotNull OptionMapping idOfBot) {
        if(BUILDER.getSelfUser().getId().equals(idOfBot.getAsString())){
            BUILDER.shutdown();
        }
    }
}
