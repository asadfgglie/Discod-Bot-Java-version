package ckcsc.asadfgglie.main.services.handler.music;

import ckcsc.asadfgglie.main.services.MusicPlayer;
import ckcsc.asadfgglie.util.Time;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;

public class LeaveHandler extends Thread {
    private final AudioChannel audioChannel;
    private final Handler handler;
    private final MusicPlayer musicPlayer;
    public volatile boolean check = true;

    private final long leaveTime = Time.second * 5;

    public LeaveHandler(AudioChannel audioChannel, Handler handler, MusicPlayer musicPlayer) {
        this.audioChannel = audioChannel;
        this.handler = handler;
        this.musicPlayer = musicPlayer;
    }

    @Override
    public void run() {
        while(true){
            if(handler.isPlaying){
                break;
            }
        }
        while(check){
            waitTime(Time.second);
            if(audioChannel.getMembers().size() <= 1 && check){
                leaveByListener();
            }
            if(handler.isPlaying){
                leaveByNoPlaying();
            }
        }
    }

    private void waitTime(long milliseconds){
        try {
            sleep(milliseconds);
        } catch (InterruptedException ignore) {}
    }

    private void leaveByNoPlaying() {
        waitTime(leaveTime);

        if(!handler.isPlaying){
            leave("Here has not needed to play musics!");
        }
    }

    private void leaveByListener(){
        waitTime(leaveTime);

        if(audioChannel.getMembers().size() <= 1){
            leave("Here (" + audioChannel.getAsMention() + ") is no listener!");
        }
    }

    public void leave(String reason) {
        if(musicPlayer.mapData.contain(handler)) {
            musicPlayer.printAndSend(reason + "\nSo I will leave to sleep. (.w.)", musicPlayer.mapData.getMessageChannel(handler));

            musicPlayer.leave(musicPlayer.mapData.getAudioChannel(handler));
            handler.getAudioPlayer().destroy();

            musicPlayer.mapData.remove(handler);
        }

        check = false;
    }
}
