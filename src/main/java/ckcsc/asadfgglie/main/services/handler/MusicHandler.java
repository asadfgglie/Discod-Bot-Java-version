package ckcsc.asadfgglie.main.services.handler;

import ckcsc.asadfgglie.main.services.MusicPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.LinkedList;

public class MusicHandler extends AudioEventAdapter implements AudioSendHandler{
    private final AudioPlayer audioPlayer;
    private AudioFrame lastFrame;
    private final MusicPlayer musicPlayer;
    public boolean isPlaying = false;

    private final LinkedList<AudioTrack> playerList = new LinkedList<>();

    public MusicHandler(AudioPlayer audioPlayer, MusicPlayer musicPlayer) {
        this.audioPlayer = audioPlayer;
        this.musicPlayer = musicPlayer;
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public void addTrack(@NotNull AudioTrack track){
        if(playerList.add(track)){
            musicPlayer.printlnInfo("Success load the music!");
            musicPlayer.messageChannel.sendMessage("Success load the music!").queue();
        }
        else {
            musicPlayer.printlnInfo("Failed load the music!");
            musicPlayer.messageChannel.sendMessage("Failed load the music!").queue();
        }
    }

    public void addAllTracks(@NotNull AudioPlaylist tracks){
        if(playerList.addAll(tracks.getTracks())){
            musicPlayer.printlnInfo("Success load musics!\nTotal " + tracks.getTracks().size() + " of musics.");
            musicPlayer.messageChannel.sendMessage("Success load musics!\nTotal " + tracks.getTracks().size() + " of musics.").queue();
        }
        else {
            musicPlayer.printlnInfo("Failed load musics!");
            musicPlayer.messageChannel.sendMessage("Failed load musics!").queue();
        }
    }

    public void playList(){
        audioPlayer.playTrack(playerList.poll());
    }

    public void onTrackStart(AudioPlayer player, @NotNull AudioTrack track) {
        musicPlayer.printlnInfo("Now playing: " + track.getInfo().title);
        musicPlayer.messageChannel.sendMessage("Now playing: " + track.getInfo().title).queue();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if((endReason == AudioTrackEndReason.STOPPED || endReason.mayStartNext) && isPlaying) {
            AudioTrack nextTrack = playerList.poll();
            if (nextTrack != null) {
                audioPlayer.playTrack(nextTrack);
            }
        }
        else {
            musicPlayer.printlnInfo("Play finished!");
            musicPlayer.messageChannel.sendMessage("Play finished!").queue();
            isPlaying = false;
        }
    }

    @Override
    public void onPlayerPause(AudioPlayer player) {
        musicPlayer.messageChannel.sendMessage("Pause " + audioPlayer.getPlayingTrack().getInfo().title).queue();
        musicPlayer.printlnInfo("Pause " + audioPlayer.getPlayingTrack().getInfo().title);
        this.isPlaying = true;
    }

    @Override
    public boolean canProvide() {
        lastFrame = audioPlayer.provide();
        return lastFrame != null;
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        isPlaying = true;
        return ByteBuffer.wrap(lastFrame.getData());
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}
