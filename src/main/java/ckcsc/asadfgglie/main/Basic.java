package ckcsc.asadfgglie.main;

import ckcsc.asadfgglie.main.services.Register.Register;
import ckcsc.asadfgglie.main.services.Register.ServiceArray;
import ckcsc.asadfgglie.main.services.Register.Services;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

public class Basic extends ListenerAdapter {
    public static ArrayList<Register> REGISTER_LIST = new ArrayList<>();
    public static String PATH;
    public static JSONObject REGISTER_CONFIG;
    public static JSONObject BOT_CONFIG;

    public static void main() throws IOException {
        Services.initialization();

        setAllConfig();

        registerServices();

        try {
            startBot(BOT_CONFIG.getString("TOKEN"));
        }
        catch (JSONException e){
            System.err.println("BotConfig.json need key \"TOKEN\".");
        }
    }

    private static void startBot(String token){
        try{
            JDA builder = JDABuilder.createDefault(token).addEventListeners(new Basic()).build();

            builder.awaitReady();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void registerServices(){
        for(Iterator<String> it = REGISTER_CONFIG.keys(); it.hasNext();){
            String serviceClass = it.next();

            JSONObject serviceValues = REGISTER_CONFIG.getJSONObject(serviceClass);

            if(Services.SERVICESLIST.containsKey(serviceClass)) {
                int serviceNumber = serviceValues.length();

                ServiceArray serviceArray = Services.SERVICESLIST.get(serviceClass);
                serviceArray.setArraySize(serviceNumber);
                serviceArray.initializeArray();

                Iterator<String> serviceValueIterator = serviceValues.keySet().iterator();
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

    private static void setAllConfig() throws IOException {
        REGISTER_CONFIG = setConfig("RegisterConfig.json");

        BOT_CONFIG = setConfig("BotConfig.json");
    }

    private static @NotNull JSONObject setConfig(String configName) throws IOException {
        BufferedReader configReader;

        try {
            configReader = new BufferedReader(new InputStreamReader(new FileInputStream(PATH + File.separator + configName)));
            System.out.println("Success load \"" + configName + "\"");
        }
        catch (FileNotFoundException e){
            throw new FileNotFoundException("Couldn't find \"" + configName + "\" in \"" + PATH);
        }

        StringBuilder configStr = new StringBuilder();
        String str;

        while((str = configReader.readLine()) != null){
            str = str.trim();
            configStr.append(str);
        }

        configReader.close();

        return new JSONObject(configStr.toString());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event){
        callRegister(event);
    }

    private void callRegister(Event event){
        for (Register register: REGISTER_LIST){
            register.call(event);
        }
    }
}
