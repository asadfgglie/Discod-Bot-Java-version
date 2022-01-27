package ckcsc.asadfgglie.main.services.Register;

import ckcsc.asadfgglie.main.services.GFloor;
import ckcsc.asadfgglie.main.services.MusicPlayer;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.HashMap;

public abstract class Services extends ListenerAdapter {
    protected String serviceName = null;
    protected long CHANNEL_ID = 0;

    public static HashMap<String, ServiceArray> SERVICES_LIST = new HashMap<>();

    public static void init() {
        loginService(GFloor.class.getSimpleName(), new GFloor());
        loginService(MusicPlayer.class.getSimpleName(), new MusicPlayer());
    }

    private static void loginService(String className, Services service){
        SERVICES_LIST.put(className, new ServiceArray(service));
    }

    protected Services(){}

    public abstract void registerByEnvironment(JSONObject values, String name);

    public abstract Services copy();

    public void printlnInfo(String msg){
        if (msg != null)
            System.out.println(msg);
        System.out.println(this + "\n");
    }

    protected void printMsg(@NotNull MessageReceivedEvent event){
        if(event.isFromGuild()) {
            System.out.println("<<" + event.getGuild().getName() + ">>" +
                    " #<" + event.getChannel().getName() + "> " +
                    event.getAuthor().getName() + ":\n" +
                    event.getMessage().getContentDisplay() + "\n");
        }
        else {
            System.out.println("<<" + event.getChannel().getName() + ">>" +
                    event.getAuthor().getName() + ": " +
                    event.getMessage().getContentDisplay());
        }
    }

    protected void printlnErr(String msg){
        if (msg != null)
            System.err.println(msg);
        System.err.println(this);
    }
}
