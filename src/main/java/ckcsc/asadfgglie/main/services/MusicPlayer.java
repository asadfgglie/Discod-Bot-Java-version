package ckcsc.asadfgglie.main.services;

import ckcsc.asadfgglie.main.command.CommandData;
import ckcsc.asadfgglie.main.services.Register.Services;
import ckcsc.asadfgglie.main.services.handler.MusicHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Objects;

public class MusicPlayer extends Services {
    public MessageChannel messageChannel = null;
    private AudioChannel audioChannel = null;
    private MusicHandler musicHandler = null;

    public MusicPlayer(){}

    protected MusicPlayer(@NotNull MusicPlayer musicPlayer) {
        this.serviceName = musicPlayer.serviceName;
        this.CHANNEL_ID = musicPlayer.CHANNEL_ID;
    }

    @Override
    public void registerByEnvironment(JSONObject values, String name) {
        this.serviceName = name;
    }

    @Override
    public MusicPlayer copy() {
        return new MusicPlayer(this);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        CommandData cmdData = CommandData.getCmdData(event);

        if(!cmdData.isCmd|| event.getAuthor().isBot() || !event.isFromGuild()){
            return;
        }

        if(cmdData.cmdHeadEqual("play")){
            playMusic(cmdData, event);
            printMsg(event);
            messageChannel = event.getChannel();
        }
        else if(cmdData.cmdHeadEqual("pause")){
            pauseMusic(event);
            printMsg(event);
            messageChannel = event.getChannel();
        }
        else if(cmdData.cmdHeadEqual("skip")){
            skipMusic(event);
            printMsg(event);
            messageChannel = event.getChannel();
        }
        else if(cmdData.cmdHeadEqual("volume")){
            if(cmdData.cmd.length == 1){
                showVolume(event);
                printMsg(event);
                messageChannel = event.getChannel();
            }
            else {
                setVolume(cmdData, event);
                printMsg(event);
                messageChannel = event.getChannel();
            }
        }
        else if (cmdData.cmdHeadEqual("stop")){
            stopPlay(event);
            printMsg(event);
            messageChannel = event.getChannel();
        }
    }

    private void showVolume(MessageReceivedEvent event) {
        if(checkCantUseCmd(event)){
            return;
        }
        messageChannel.sendMessage("Now volume: " + musicHandler.getAudioPlayer().getVolume()).queue();
        printlnInfo("Now volume: " + musicHandler.getAudioPlayer().getVolume());
    }

    private void setVolume(CommandData commandData, MessageReceivedEvent event) {
        if(checkCantUseCmd(event)){
            return;
        }
        try {
            musicHandler.getAudioPlayer().setVolume(Integer.parseInt(commandData.cmd[1]));
            showVolume(event);
        }
        catch (Exception e){
            messageChannel.sendMessage("Usage:```\n!volume <int>\n```or```\n!volume\n```").queue();
            printlnInfo("Usage:```\n!volume <int>\n```or```\n!volume\n```");
        }
    }

    /**
     * Check whether it can't use this command now.
     * <br>
     * If can't, return ture, else return false.
     */
    private boolean checkCantUseCmd(@NotNull MessageReceivedEvent event) {
        if(event.getGuild().getIdLong() == audioChannel.getGuild().getIdLong()) {
            try {
                if (Objects.requireNonNull(
                                Objects.requireNonNull(
                                                Objects.requireNonNull(event.getMember())
                                                        .getVoiceState())
                                        .getChannel())
                        .getIdLong() != audioChannel.getIdLong()) {
                    messageChannel.sendMessage("You are not the listener!").queue();
                    return true;
                }
                else if(audioChannel == null || musicHandler.isPlaying){
                    return false;
                }
                else {
                    messageChannel.sendMessage("The bot is not playing music!").queue();
                    return true;
                }
            } catch (NullPointerException e) {
                messageChannel.sendMessage("You are not the listener!").queue();
                return true;
            }
        }
        return true;
    }

    private void skipMusic(MessageReceivedEvent event) {
        if(checkCantUseCmd(event)){
            return;
        }
        printlnInfo("Skip " + musicHandler.getAudioPlayer().getPlayingTrack().getInfo().title);
        messageChannel.sendMessage("Skip " + musicHandler.getAudioPlayer().getPlayingTrack().getInfo().title).queue();
        musicHandler.getAudioPlayer().stopTrack();
    }

    private void pauseMusic(@NotNull MessageReceivedEvent event) {
        if(checkCantUseCmd(event)){
            return;
        }
        musicHandler.getAudioPlayer().setPaused(!musicHandler.getAudioPlayer().isPaused());
    }

    private void playMusic(@NotNull CommandData commandData, MessageReceivedEvent event) {
        if(commandData.cmd.length < 2){
            messageChannel.sendMessage("Usage:```\n!play <url>\n```or```\n!play <url> <volume>\n````url`: The music url.\n``volume`: Set the initial volume. Need Integer.").queue();
        }
        String url = commandData.cmd[1];

        try {
            audioChannel = Objects.requireNonNull(
                                Objects.requireNonNull(
                                    Objects.requireNonNull(
                                            event.getMember()
                                    )
                                    .getVoiceState()
                                )
                           .getChannel()
            );
        }
        catch (Exception e){
            event.getChannel().sendMessage("You must need in the Voice Channel to let bot know where to play music!").queue();
            return;
        }

        AudioManager audioManager = event.getGuild().getAudioManager();

        // Here we finally connect to the target voice channel,
        // and it will automatically start pulling the audio from the MySendHandler instance
        audioManager.openAudioConnection(audioChannel);

        AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);

        AudioPlayer audioPlayer = audioPlayerManager.createPlayer();
        musicHandler = new MusicHandler(audioPlayer, this);
        audioPlayer.addListener(musicHandler);


        if(commandData.cmd.length == 3) {
            try {
                audioPlayer.setVolume(Integer.parseInt(commandData.cmd[2]));
                printlnInfo("Set volume: " + commandData.cmd[2]);
            }
            catch (Exception ignore){}
        }
        else{
            audioPlayer.setVolume(15);
        }


        try {
            audioManager.setSendingHandler(musicHandler);
        }
        catch (Exception e){
            event.getChannel().sendMessage("The url must start with `https://` or `http://`.").queue();
        }

        audioPlayerManager.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicHandler.addTrack(track);
                musicHandler.playList();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                musicHandler.addAllTracks(playlist);
                musicHandler.playList();
            }

            @Override
            public void noMatches() {
                event.getChannel().sendMessage("Can't play the music from `" + url + "`.").queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                event.getChannel().sendMessage("Some thing is go in wrong.\nPlease contact the administrator.").queue();
                exception.printStackTrace();
            }
        });
    }

    private void stopPlay(@NotNull MessageReceivedEvent event){
        if(checkCantUseCmd(event)){
            return;
        }
        musicHandler.isPlaying = false;
        musicHandler.getAudioPlayer().destroy();
        event.getGuild().getAudioManager().closeAudioConnection();
    }

    public String toString(){
        try {
            return MusicPlayer.class.getSimpleName() + "(serviceName: " + this.serviceName + ", playInChannel: " + this.audioChannel.getName() + ", playInGuild: " + this.audioChannel.getGuild().getName() + ")";
        }
        catch (NullPointerException e){
            return MusicPlayer.class.getSimpleName() + "(serviceName: " + this.serviceName + ")";
        }
    }
}
