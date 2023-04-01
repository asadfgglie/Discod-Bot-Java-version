package ckcsc.asadfgglie.main.services.Register;

import ckcsc.asadfgglie.main.Basic;
import ckcsc.asadfgglie.main.services.GFloor;
import ckcsc.asadfgglie.main.services.ai.AutoReply;
import ckcsc.asadfgglie.main.services.ai.MNIST;
import ckcsc.asadfgglie.main.services.MusicPlayer;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public abstract class Services extends ListenerAdapter {
    protected String serviceName;
    protected Logger logger;
    protected String description;
    protected boolean isInfoVisible = true;

    public static HashMap<String, ServiceArray> SERVICES_LIST = new HashMap<>();

    /**
     * Initialize registered service.
     */
    public static void init() {
        loginService(GFloor.class.getSimpleName(), new GFloor());
        loginService(MusicPlayer.class.getSimpleName(), new MusicPlayer());
        loginService(MNIST.class.getSimpleName(), new MNIST());
        loginService(AutoReply.class.getSimpleName(), new AutoReply());
    }

    private static void loginService(String className, Services service){
        SERVICES_LIST.put(className, new ServiceArray(service));
    }

    protected Services(){}

    protected JSONObject getSelfConfig(){
        return Basic.REGISTER_CONFIG.getJSONObject(this.getClass().getSimpleName()).getJSONObject(this.serviceName);
    }

    public abstract void registerByEnvironment(JSONObject values);

    public void setLogger(String name){
        this.logger = LoggerFactory.getLogger(this.getClass().getSimpleName() + " - " + name);
    }

    private void setServiceName(String name){
        this.serviceName = name;
    }

    public abstract Services copy();

    public void printlnInfo(@Nullable String msg){
        if (msg != null) {
            for(String str : msg.split("\n")) {
                logger.info(str);
            }
        }
        logger.info(this.toString());
    }

    private void setDescription(JSONObject values){
        try {
            this.description = values.getString("description");
        }
        catch (JSONException e){
            this.logger.warn(this + " dose not have description.");
        }
    }

    private void setInfoVisible (JSONObject values){
        try {
            this.isInfoVisible = values.getBoolean("isInfoVisible");
        }
        catch (JSONException e){
            this.logger.warn("\"isInfoVisible\" doesn't set.");
            this.logger.warn("\"isInfoVisible\" will be set true by default.");
            this.logger.warn("If \"isInfoVisible\" set to false, other discord server member still can look this service description.");
        }
    }

    public void basicSetting(JSONObject values, String name){
        setServiceName(name);
        setLogger(name);
        setInfoVisible(values);
        setDescription(values);
    }

    /**
     * Print an MessageEvent's msg.
     * @param e the MessageEvent
     */
    protected void printMsg(@NotNull GenericMessageEvent e){
        if(e instanceof MessageReceivedEvent) {
            MessageReceivedEvent event = (MessageReceivedEvent) e;
            if (event.isFromGuild()) {
                logger.info("<<" + event.getGuild().getName() + ">>" + " #<" + event.getChannel().getName() + "> " + event.getAuthor().getName() + ": " + event.getMessage().getContentDisplay());
            }
            else {
                logger.info("<<" + event.getChannel().getName() + ">>" + event.getAuthor().getName() + ": " + event.getMessage().getContentDisplay());
            }
        }
        else if(e instanceof MessageUpdateEvent){
            MessageUpdateEvent event = (MessageUpdateEvent) e;
            if (event.isFromGuild()) {
                logger.info("<<" + event.getGuild().getName() + ">>" + " #<" + event.getChannel().getName() + "> " + event.getAuthor().getName() + ": " + event.getMessage().getContentDisplay());
            }
            else {
                logger.info("<<" + event.getChannel().getName() + ">>" + event.getAuthor().getName() + ": " + event.getMessage().getContentDisplay());
            }
        }
    }
}
