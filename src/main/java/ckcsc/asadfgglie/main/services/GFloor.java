package ckcsc.asadfgglie.main.services;

import ckcsc.asadfgglie.main.services.Register.Services;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;


public class GFloor extends Services {
    private long nowFloor = 0;
    private long maxFloor = 0;
    private User lastFloorBreaker = null;
    private User lastFloorBuilder = null;

    public GFloor(){}

    protected GFloor(@NotNull GFloor g){
        this.nowFloor = g.nowFloor;
        this.maxFloor = g.maxFloor;
        this.lastFloorBreaker = null;
        this.lastFloorBuilder = null;
    }

    @Override
    public void registerByEnvironment(JSONObject values, String name) {
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
    }

    @Override
    public Services copy() {
        return new GFloor(this);
    }

    private void gCheck(@NotNull MessageReceivedEvent event){
        printlnInfo("Before check");
        String message = event.getMessage().getContentDisplay();
        String[] messageArray = message.split("\n");

        String msg = messageArray[0];
        msg = msg.split("\\s+")[0];

        gCheckImplement(msg, event);
        printlnInfo("After check");
    }

    private void gCheckImplement(String msg, @NotNull MessageReceivedEvent event) {
        long floor;
        boolean isAuthorSame;

        try{
            isAuthorSame = lastFloorBuilder.getIdLong() == event.getAuthor().getIdLong();
        }
        catch (Exception e){
            isAuthorSame = false;
        }

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
        else if(isAuthorSame){
            breakFloor(event);
        }
        else
        {
            lastFloorBuilder = event.getAuthor();
            nowFloor++;

            if (nowFloor > maxFloor)
            {
                maxFloor = nowFloor;
            }
        }
    }

    private void breakFloor(@NotNull MessageReceivedEvent event){
        printlnInfo("Before break");
        lastFloorBreaker = event.getMessage().getAuthor();

        MessageChannel channel = event.getChannel();
        channel.sendMessage(gMessage()).queue();

        lastFloorBuilder = null;
        nowFloor = 0;
        printlnInfo("After break");
    }

    private @NotNull String gMessage(){
        return "----------------------------\n" +
               "上次紀錄：\t" + nowFloor + " 樓\n" +
               "破壞者：\t\t" + lastFloorBreaker.getAsMention() + "\n" +
               "最高紀錄：\t" + maxFloor + " 樓\n" +
               "----------------------------";
    }

    @Override
    public String toString(){
        return GFloor.class.getSimpleName() + "(serviceName: " + this.serviceName + ", CHANNEL_ID: " + this.CHANNEL_ID + ", nowFloor: " + this.nowFloor + ", maxFloor: " + this.maxFloor + ")";
    }

    @Override
    public void run() {
        if(!(e instanceof MessageReceivedEvent)){
            return;
        }

        MessageReceivedEvent event = (MessageReceivedEvent) this.e;

        if(event.getChannel().getIdLong() == CHANNEL_ID){
            printMsg(event);

            MessageType messageType = event.getMessage().getType();

            if(messageType == MessageType.DEFAULT) {
                if (event.getAuthor() == event.getJDA().getSelfUser()) {
                    event.getChannel().pinMessageById(event.getMessageId()).queue();
                } else {
                    gCheck(event);
                }
            }
        }
    }

    @Override
    public void printlnInfo(String msg){
        if (!msg.equals(""))
            System.out.println(msg);
        System.out.println(this);
    }

    private void printMsg(MessageReceivedEvent event){
        System.out.println( "<<" + event.getGuild().getName() + ">>" +
                            " #<" + event.getChannel().getName() + "> " +
                            event.getAuthor().getName() + ": " +
                            event.getMessage().getContentDisplay());
    }
}
