package ckcsc.asadfgglie.main.services;

import ckcsc.asadfgglie.main.Basic;
import ckcsc.asadfgglie.main.services.Register.Services;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;


public class GFloor extends Services {
    private long nowFloor = 0;
    private long maxFloor = 0;
    private User lastFloorBreaker = null;
    private long lastFloorBuilderID = -1;
    private boolean isNeedPin = false;

    public GFloor(){}

    protected GFloor(@NotNull GFloor g){
        this.nowFloor = g.nowFloor;
        this.maxFloor = g.maxFloor;
        this.lastFloorBreaker = null;
        this.lastFloorBuilderID = g.lastFloorBuilderID;
        this.isNeedPin = false;
    }

    @Override
    public Services copy() {
        return new GFloor(this);
    }

    @Override
    public void registerByEnvironment(@NotNull JSONObject values, String name) {
        this.serviceName = name;

        try {
            this.CHANNEL_ID = values.getLong("CHANNEL_ID");
        }
        catch (JSONException e) {
            System.err.println("Need \"CHANNEL_ID\" to register service.");
        }

        try {
            this.nowFloor = values.getLong("nowFloor");
        }
        catch (JSONException e) {
            System.out.println("\"nowFloor\" is a option to register service. Set to the default value: 0");
            this.nowFloor = 0;
        }

        try {
           this.maxFloor = values.getLong("maxFloor");
        }
        catch (JSONException e) {
            System.out.println("\"maxFloor\" is a option to register service. Set to the default value: 0");
            this.maxFloor = 0;
        }

        try {
            this.lastFloorBuilderID = values.getLong("lastFloorBuilder");
        }
        catch (JSONException e) {
            System.out.println("\"lastFloorBuilder\" is a option to register service. Set to the default value: -1");
            this.lastFloorBuilderID = -1;
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event){
        if(event.getChannel().getIdLong() == CHANNEL_ID){
            printMsg(event);

            MessageType messageType = event.getMessage().getType();

            if(messageType == MessageType.DEFAULT) {
                if (event.getAuthor() == event.getJDA().getSelfUser()) {
                    if(isNeedPin) {
                        event.getChannel().pinMessageById(event.getMessageId()).queue();
                        isNeedPin = false;
                    }
                } else {
                    gCheck(event);

                    gUpdateConfig();

                    Basic.saveConfig(Basic.REGISTER_CONFIG);
                }
            }
        }
    }

    private void gCheck(@NotNull MessageReceivedEvent event){
        String message = event.getMessage().getContentDisplay();
        String[] messageArray = message.split("\n");

        String msg = messageArray[0];
        msg = msg.split("\\s+")[0];

        gCheckImplement(msg, event);

        printlnInfo(null);
    }

    private void gUpdateConfig() {
        JSONObject selfService = Basic.REGISTER_CONFIG.getJSONObject(GFloor.class.getSimpleName()).getJSONObject(this.serviceName);

        selfService.put("maxFloor", this.maxFloor);
        selfService.put("nowFloor", this.nowFloor);
        selfService.put("lastFloorBuilder", this.lastFloorBuilderID);
    }

    private void gCheckImplement(String msg, @NotNull MessageReceivedEvent event) {
        long floor;

        try
        {
            floor = Long.parseLong(msg.split("g")[1]);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            breakFloor(event);
            return;
        }

        if (!msg.matches("g\\d+"))
        {
            breakFloor(event);
        }
        else if (floor - nowFloor != 1)
        {
            breakFloor(event);
        }
        else if(lastFloorBuilderID == event.getAuthor().getIdLong()){
            breakFloor(event);
        }
        else
        {
            lastFloorBuilderID = event.getAuthor().getIdLong();
            nowFloor++;

            if (nowFloor > maxFloor)
            {
                maxFloor = nowFloor;
                isNeedPin = true;
            }
        }
    }

    private void breakFloor(@NotNull MessageReceivedEvent event){
        lastFloorBreaker = event.getMessage().getAuthor();

        MessageChannel channel = event.getChannel();
        channel.sendMessage(gMessage()).queue();

        lastFloorBuilderID = -1;
        nowFloor = 0;
    }

    private String gMessage(){
        return "----------------------------\n" +
               "上次紀錄：\t" + nowFloor + " 樓\n" +
               "破壞者：\t\t" + lastFloorBreaker.getAsMention() + "\n" +
               "最高紀錄：\t" + maxFloor + " 樓\n" +
               "----------------------------";
    }

    @Override
    public String toString(){
        return GFloor.class.getSimpleName() + "(serviceName: " + this.serviceName + ", CHANNEL_ID: " + this.CHANNEL_ID + ", nowFloor: " + this.nowFloor + ", maxFloor: " + this.maxFloor + ", lastFloorBuilder" + this.lastFloorBuilderID + ")";
    }
}
