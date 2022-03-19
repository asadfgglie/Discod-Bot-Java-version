package ckcsc.asadfgglie.main.services;

import ckcsc.asadfgglie.main.Basic;
import ckcsc.asadfgglie.main.services.Register.Services;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class GFloor extends Services {
    private long nowFloor = 0;
    private long maxFloor = 0;
    private long lastFloorBreakerID;
    private long lastFloorBuilderID = -1;
    private boolean isNeedPin = false;

    private long CHANNEL_ID;

    public GFloor(){}

    @Override
    public Services copy() {
        return new GFloor();
    }

    @Override
    public void registerByEnvironment(@NotNull JSONObject values) {
        try {
            this.CHANNEL_ID = values.getLong("CHANNEL_ID");
        }
        catch (JSONException e) {
            logger.error("Need \"CHANNEL_ID\" to register service.", e);
        }

        try {
            this.nowFloor = values.getLong("nowFloor");
        }
        catch (JSONException e) {
            logger.warn("\"nowFloor\" is a option to register service. Set to the default value: 0");
        }

        try {
           this.maxFloor = values.getLong("maxFloor");
        }
        catch (JSONException e) {
            logger.warn("\"maxFloor\" is a option to register service. Set to the default value: 0");
        }

        try {
            this.lastFloorBuilderID = values.getLong("lastFloorBuilderID");
        }
        catch (JSONException e) {
            logger.warn("\"lastFloorBuilderID\" is a option to register service. Set to the default value: -1");
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
        JSONObject selfService = getSelfConfig();

        selfService.put("maxFloor", this.maxFloor);
        selfService.put("nowFloor", this.nowFloor);
        selfService.put("lastFloorBuilderID", this.lastFloorBuilderID);
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
        lastFloorBreakerID = event.getMessage().getAuthor().getIdLong();

        MessageChannel channel = event.getChannel();
        channel.sendMessage(gMessage()).queue();

        lastFloorBuilderID = -1;
        nowFloor = 0;
    }

    private String gMessage(){
        String breaker;
        try {
            breaker = Basic.BUILDER.getUserById(lastFloorBreakerID).getAsMention();
        }
        catch (NullPointerException e){
            breaker = "取得違建仔時發生錯誤!";
            logger.error(breaker, e);
        }
        return "----------------------------\n" +
               "上次紀錄：\t" + nowFloor + " 樓\n" +
               "破壞者：\t\t" + breaker + "\n" +
               "最高紀錄：\t" + maxFloor + " 樓\n" +
               "----------------------------";
    }

    @Override
    public String toString(){
        return GFloor.class.getSimpleName() + "(serviceName: " + this.serviceName + ", CHANNEL_ID: " + this.CHANNEL_ID + ", nowFloor: " + this.nowFloor + ", maxFloor: " + this.maxFloor + ", lastFloorBuilder" + this.lastFloorBuilderID + ")";
    }
}
