package ckcsc.asadfgglie.main.services.handler.music;

import ckcsc.asadfgglie.main.Basic;
import ckcsc.asadfgglie.main.services.MusicPlayer;
import ckcsc.asadfgglie.util.Time;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

/**
 * If you want to stop this player, use <b>destroy()</b> instead <b>stopTrack()</b>.
 * @see MusicPlayer#skipMusic(Guild) 
 */
public class Handler extends AudioEventAdapter implements AudioSendHandler{
    private final AudioPlayer audioPlayer;
    private AudioFrame lastFrame;
    private final MusicPlayer musicPlayer;
    /**
     * Only <code>true</code> when {@link #waitingList <i>waitingList</i>} is empty and {@link #isLoop <i>isLoop</i>} is <code>false</code>.
     */
    public volatile boolean isPlaying = false;
    public volatile boolean isLoop = false;

    private LinkedList<AudioTrack> waitingList = new LinkedList<>();

    public Handler (AudioPlayer audioPlayer, MusicPlayer musicPlayer) {
        this.audioPlayer = audioPlayer;
        this.musicPlayer = musicPlayer;
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public void addTrack(@NotNull AudioTrack track){
        if(waitingList.offerLast(track)){
            musicPlayer.printAndSend("Success load the music!", musicPlayer.mapData.getMessageChannel(this));
        }
        else {
            musicPlayer.printAndSend("Failed load the music!", musicPlayer.mapData.getMessageChannel(this));
        }
    }

    public void addAllTracks(@NotNull AudioPlaylist tracks){
        if(waitingList.addAll(tracks.getTracks())){
            musicPlayer.printAndSend("Success load musics!\nTotal " + tracks.getTracks().size() + " of musics.", musicPlayer.mapData.getMessageChannel(this));
        }
        else {
            musicPlayer.printAndSend("Failed load musics!", musicPlayer.mapData.getMessageChannel(this));
        }
    }

    public void playList(){
        audioPlayer.playTrack(waitingList.pollFirst());
    }

    public void onTrackStart(AudioPlayer player, @NotNull AudioTrack track) {
        musicPlayer.printAndSend("Now playing: " + stdTitle(track), musicPlayer.mapData.getMessageChannel(this));
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(isLoop){
            waitingList.offerLast(track.makeClone());
        }

        boolean playContinue = (endReason == AudioTrackEndReason.STOPPED || endReason.mayStartNext) && isPlaying;
        AudioTrack nextTrack = waitingList.pollFirst();

        if(playContinue && nextTrack != null) {
            audioPlayer.playTrack(nextTrack);
        }
        else {
            musicPlayer.printAndSend("Play finished!", musicPlayer.mapData.getMessageChannel(this));

            isPlaying = false;
            waitingList = new LinkedList<>();
        }
    }

    @Override
    public void onPlayerPause(AudioPlayer player) {
        musicPlayer.printAndSend("Pause " + audioPlayer.getPlayingTrack().getInfo().title, musicPlayer.mapData.getMessageChannel(this));
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

    private String stdTitle (@NotNull AudioTrack track){
        return track.getInfo().title + " - " + Time.toTimeString(track.getInfo().length);
    }

    public void showPlayList() {
        musicPlayer.printAndSend("Now playing: " + stdTitle(audioPlayer.getPlayingTrack()) + "\nWaiting List:\n", musicPlayer.mapData.getMessageChannel(this));

        int index = 1;
        StringBuilder str = new StringBuilder();
        for (AudioTrack track : waitingList) {
            str.append(String.format("%d.\t", index)).append(stdTitle(track)).append("\n");
            index++;
        }
        if(str.length() < Basic.MAX_MESSAGE_SEND_LENGTH){
            musicPlayer.mapData.getMessageChannel(this).sendMessage(str.toString()).queue();
        }
        else {
            String[] tmpArray = str.toString().split("\n");
            int i = 0;
            while(i < tmpArray.length) {
                StringBuilder tmpStr = new StringBuilder();
                for (; i < tmpArray.length; i++) {
                    tmpStr.append(tmpArray[i]).append("\n");
                    if (tmpStr.length() >= Basic.MAX_MESSAGE_SEND_LENGTH) {
                        tmpStr.delete(tmpStr.length() - (tmpArray[i] + "\n").length(), tmpStr.length());
                        break;
                    }
                }
                musicPlayer.mapData.getMessageChannel(this).sendMessage(tmpStr.toString()).queue();
            }
        }
        musicPlayer.printlnInfo(str.toString());
    }

    public void setLoop() {
        isLoop = !isLoop;
    }

    public synchronized void shufflePlayList() {
        Collections.shuffle(waitingList, new Random());
    }
}
