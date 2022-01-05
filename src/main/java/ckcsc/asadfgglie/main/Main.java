package ckcsc.asadfgglie.main;

import javax.security.auth.login.LoginException;

import ckcsc.asadfgglie.main.services.Register.Register;
import ckcsc.asadfgglie.main.services.Register.ServiceArray;
import ckcsc.asadfgglie.main.services.Register.Services;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class Main extends ListenerAdapter {
    public static ArrayList<Register> REGISTETLIST = new ArrayList<>();
    private static JSONObject ENVIRONMENT;

    public static void main(String @NotNull [] args) throws IOException {
        Services.initialization();

        setEnvironment();

        registerServices();

        startBot(args[0]);
    }

    private static void startBot(String token){
        try{
            JDA builder = JDABuilder.createDefault(token).addEventListeners(new Main()).build();

            builder.awaitReady();
        }
        catch (LoginException | InterruptedException | IllegalStateException e){
            System.err.println(e.getMessage());
        }
    }

    private static void registerServices(){
        for(Iterator<String> it = ENVIRONMENT.keys(); it.hasNext();){
            String serviceClass = it.next();

            JSONObject serviceValues = ENVIRONMENT.getJSONObject(serviceClass);

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
                    REGISTETLIST.add(service);
                    System.out.println("Success register service: " + service);
                }
            }
        }
    }

    private static void setEnvironment() throws IOException {
        BufferedReader bufferedReader;

        try{
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream("src/main/resources/RegisterEnvironment.json")));
        }
        catch (FileNotFoundException e){
            throw new FileNotFoundException("Need RegisterEnvironment.json");
        }

        StringBuilder environmentStr = new StringBuilder();
        String str;

        while((str = bufferedReader.readLine()) != null){
            str = str.trim();
            environmentStr.append(str);
        }
        
        bufferedReader.close();
        
        ENVIRONMENT = new JSONObject(environmentStr.toString());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event){
        callRegister(event);
    }

    private void callRegister(Event event){
        for (Register register: REGISTETLIST){
            register.call(event);
        }
    }
}
