package ckcsc.asadfgglie.main.services.handler.music;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.HashMap;

public class MapData {
    private final HashMap<Handler, AudioChannel> handlerAudioChannelMap = new HashMap<>();
    private final HashMap<Handler, LeaveHandler> handlerLeaveHandlerMap = new HashMap<>();

    private final HashMap<Guild, Handler> guildHandlerMap = new HashMap<>();
    private final HashMap<Guild, MessageChannel> guildMessageChannelMap = new HashMap<>();

    public Guild getGuild(Handler handler){
        return handlerAudioChannelMap.get(handler).getGuild();
    }

    public Handler getHandler(Guild guild){
        return guildHandlerMap.get(guild);
    }
    public Handler getHandler(AudioChannel audioChannel){
        return guildHandlerMap.get(audioChannel.getGuild());
    }

    public MessageChannel getMessageChannel(Guild guild){
        return guildMessageChannelMap.get(guild);
    }
    public MessageChannel getMessageChannel(Handler handler){
        return getMessageChannel(getGuild(handler));
    }
    public MessageChannel getMessageChannel(AudioChannel audioChannel){
        return getMessageChannel(audioChannel.getGuild());
    }

    public AudioChannel getAudioChannel(Handler handler){
        return handlerAudioChannelMap.get(handler);
    }
    public AudioChannel getAudioChannel(Guild guild){
        return getAudioChannel(getHandler(guild));
    }

    public LeaveHandler getLeaveHandler(Handler handler){
        return handlerLeaveHandlerMap.get(handler);
    }
    public LeaveHandler getLeaveHandler(Guild guild){
        return getLeaveHandler(getHandler(guild));
    }
    public LeaveHandler getLeaveHandler(AudioChannel audioChannel){
        return getLeaveHandler(audioChannel.getGuild());
    }


    public void put(AudioChannel audioChannel, MessageChannel messageChannel, Handler handler, LeaveHandler leaveHandler){
        Guild guild = audioChannel.getGuild();

        handlerAudioChannelMap.put(handler, audioChannel);
        handlerLeaveHandlerMap.put(handler, leaveHandler);
        guildHandlerMap.put(guild, handler);
        guildMessageChannelMap.put(guild, messageChannel);
    }

    public void remove (Handler handler) {
        Guild guild = getGuild(handler);

        handlerAudioChannelMap.remove(handler);
        handlerLeaveHandlerMap.remove(handler);
        guildHandlerMap.remove(guild);
        guildMessageChannelMap.remove(guild);
    }
}
