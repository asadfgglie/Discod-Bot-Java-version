package ckcsc.asadfgglie.main.services;

import ckcsc.asadfgglie.main.services.Register.Services;
import ckcsc.asadfgglie.main.services.handler.music.Handler;
import ckcsc.asadfgglie.main.services.handler.music.LeaveHandler;
import ckcsc.asadfgglie.main.services.handler.music.LoadResultHandler;
import ckcsc.asadfgglie.main.services.handler.music.MapData;
import ckcsc.asadfgglie.util.command.CommandData;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MusicPlayer extends Services {
    public volatile MapData mapData = new MapData();
    private AudioPlayerManager audioPlayerManager;

    private final ArrayList<String> COMMAND_LIST = new ArrayList<>();

    private void addCommandSet(String... commandSet){
        COMMAND_LIST.addAll(List.of(commandSet));
    }

    public MusicPlayer(){}

    @Override
    public void registerByEnvironment (JSONObject values) {
        addCommandSet("play", "pl", "p");
        addCommandSet("pause", "pa");
        addCommandSet("skip", "sk");
        addCommandSet("volume", "v");
        addCommandSet("stop", "st");
        addCommandSet("list", "ls");
        addCommandSet("loop", "lp");
        addCommandSet("shuffle", "sh");

        audioPlayerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerLocalSource(audioPlayerManager);
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
    }

    public void printAndSend(String str, MessageChannel messageChannel){
        messageChannel.sendMessage(str).queue();
        printlnInfo(str);
    }

    @Override
    public MusicPlayer copy() {
        return new MusicPlayer();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        CommandData cmdData = CommandData.getCmdData(event);

        if(!cmdData.isCmd ||
           event.getAuthor().isBot() ||
           !event.isFromGuild()){
            return;
        }
        if(cmdData.hasTarget() && cmdData.isTargetSelf()){
            return;
        }

        if(cmdData.cmdHeadEqual("play", "pl", "p")){
            printMsg(event);
            playMusic(cmdData, event);
            return;
        }

        if(checkCantUseCmd(event, cmdData)){
            return;
        }

        else if(cmdData.cmdHeadEqual("pause", "pa")){
            printMsg(event);
            pauseMusic(mapData.getHandler(event.getGuild()));
        }
        else if(cmdData.cmdHeadEqual("skip", "sk")){
            printMsg(event);
            skipMusic(event.getGuild());
        }
        else if(cmdData.cmdHeadEqual("volume", "v")){
            assert cmdData.cmd != null;
            if(cmdData.cmd.length == 1){
                printMsg(event);
                showVolume(event.getGuild());
            }
            else {
                printMsg(event);
                setVolume(cmdData, event.getGuild());
            }
        }
        else if (cmdData.cmdHeadEqual("stop", "st")){
            printMsg(event);
            stopPlay(event.getGuild());
        }
        else if (cmdData.cmdHeadEqual("list", "ls")){
            printMsg(event);
            showPlayList(mapData.getHandler(event.getGuild()));
        }
        else if(cmdData.cmdHeadEqual("loop", "lp")){
            printMsg(event);
            mapData.getHandler(event.getGuild()).setLoop();
            printAndSend("Loop is " + ((mapData.getHandler(event.getGuild()).isLoop) ? "on." : "off."), event.getChannel());
        }
        else if(cmdData.cmdHeadEqual("shuffle", "sh")){
            printMsg(event);
            mapData.getHandler(event.getGuild()).shufflePlayList();
            printAndSend("Play-list is shuffled.", event.getChannel());
        }
    }

    private void showVolume (Guild guild) {
        mapData.getMessageChannel(guild).sendMessage("Now volume: " + mapData.getHandler(guild).getAudioPlayer().getVolume()).queue();
        printlnInfo("Now volume: " + mapData.getHandler(guild).getAudioPlayer().getVolume());
    }

    private void setVolume(CommandData commandData, Guild guild) {
        try {
            assert commandData.cmd != null;
            mapData.getHandler(guild).getAudioPlayer().setVolume(Integer.parseInt(commandData.cmd[1]));
            showVolume(guild);
        }
        catch (Exception e){
            mapData.getMessageChannel(guild).sendMessage("Usage:```\n!volume <int>\n```or```\n!volume\n```").queue();
            printlnInfo("Usage:```\n!volume <int>\n```or```\n!volume\n```");
        }
    }

    /**
     * Check whether it <b>can't</b> use this command now.
     * <br>
     * If <b>can't</b>, return ture, else return false.
     */
    private boolean checkCantUseCmd(@NotNull MessageReceivedEvent event, CommandData cmd) {
        assert cmd.cmd != null;
        if (!COMMAND_LIST.contains(cmd.cmd[0])){
            return true;
        }

        AudioChannel audioChannel = mapData.getAudioChannel(event.getGuild());
        Handler handler = mapData.getHandler(event.getGuild());

        if(audioChannel == null) {
            event.getChannel().sendMessage("The bot is not playing music!").queue();
            return true;
        }
        try {
            if (event.getMember().getVoiceState().getChannel().getIdLong() != audioChannel.getIdLong()) {
                event.getChannel().sendMessage("You are not the listener!").queue();
                return true;
            }
            else if(handler.isPlaying){
                return false;
            }
        } catch (NullPointerException e) {
            event.getChannel().sendMessage("You are not the listener!").queue();
            return true;
        }
        return false;
    }

    /**
     * Skip the current music by using <b>stopTrack()</b>.
     */
    private void skipMusic(Guild guild) {
        printlnInfo("Skip " + mapData.getHandler(guild).getAudioPlayer().getPlayingTrack().getInfo().title);
        mapData.getMessageChannel(guild).sendMessage("Skip " + mapData.getHandler(guild).getAudioPlayer().getPlayingTrack().getInfo().title).queue();
        mapData.getHandler(guild).getAudioPlayer().stopTrack();
    }

    private void pauseMusic(Handler handler) {
        handler.getAudioPlayer().setPaused(!handler.getAudioPlayer().isPaused());
    }

    private void showPlayList(Handler handler){
        handler.showPlayList();
    }

    private void playMusic(@NotNull CommandData commandData, MessageReceivedEvent event) {
        String usage = "Usage:```\n!play <url>\n```or```\n!play <url> <volume>\n````or```\n!play local\n````url`: The music url.\n``volume`: Set the initial volume. Need Integer.\n`!play local` will play the bot's local musics.";
        assert commandData.cmd != null;
        if (commandData.cmd.length < 2) {
            event.getChannel().sendMessage(usage).queue();
            return;
        }

        AudioChannel audioChannel;
        try {
            audioChannel = Objects.requireNonNull(event.getMember().getVoiceState().getChannel());
        }
        catch (Exception e) {
            event.getChannel().sendMessage("You must need in the Voice Channel to let bot know where to play music!").queue();
            return;
        }

        String url = commandData.cmd[1];

        if(mapData.getHandler(event.getGuild()) == null) {
            AudioPlayer audioPlayer = audioPlayerManager.createPlayer();
            Handler handler = new Handler(audioPlayer, this);
            audioPlayer.addListener(handler);

            try {
                handler.getAudioPlayer().setVolume(Integer.parseInt(commandData.cmd[2]));
                printlnInfo("Set volume: " + commandData.cmd[2]);
            }
            catch (Exception ignore) {
                if (!handler.isPlaying) {
                    handler.getAudioPlayer().setVolume(25);
                }
            }

            LeaveHandler leaveHandler = new LeaveHandler(audioChannel, handler, this);
            leaveHandler.setName(LeaveHandler.class.getSimpleName() + " - " + audioChannel.getName());
            leaveHandler.start();

            mapData.put(audioChannel, event.getChannel(), handler, leaveHandler);

            connectChannel(audioChannel);

            LoadResultHandler loadResultHandler = new LoadResultHandler(handler, url, mapData);

            audioPlayerManager.loadItem(url, loadResultHandler);
        }
        else {
            audioPlayerManager.loadItem(url, new LoadResultHandler(mapData.getHandler(event.getGuild()), url, mapData));
        }
    }

    private void stopPlay(Guild guild){
        printAndSend("Bye! See you next time!", mapData.getMessageChannel(guild));
        mapData.getHandler(guild).isPlaying = false;
        mapData.getHandler(guild).getAudioPlayer().destroy();
        guild.getAudioManager().closeAudioConnection();

        mapData.remove(mapData.getHandler(guild));
    }

    private void connectChannel(@NotNull AudioChannel audioChannel) {
        // Here we finally connect to the target voice channel,
        // and it will automatically start pulling the audio from the Handler instance

        AudioManager audioManager = audioChannel.getGuild().getAudioManager();
        audioManager.openAudioConnection(audioChannel);
        audioManager.setSendingHandler(mapData.getHandler(audioChannel));

        printAndSend("Connect to " + audioChannel.getAsMention(), mapData.getMessageChannel(audioChannel.getGuild()));
    }

    public String toString(){
        return MusicPlayer.class.getSimpleName() + "(serviceName: " + this.serviceName + ")";
    }

    public void leave (AudioChannel audioChannel) {
        audioChannel.getGuild().getAudioManager().closeAudioConnection();
    }
}
