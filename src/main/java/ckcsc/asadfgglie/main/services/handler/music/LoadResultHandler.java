package ckcsc.asadfgglie.main.services.handler.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadResultHandler implements AudioLoadResultHandler {
    private final Handler handler;
    private final String url;
    private final Logger logger = LoggerFactory.getLogger(LoadResultHandler.class.getSimpleName());
    private final MapData mapData;

    public LoadResultHandler(Handler handler, String url, MapData mapData){
        this.handler = handler;
        this.url = url;
        this.mapData = mapData;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        handler.addTrack(track);

        if(!handler.isPlaying) {
            handler.playList();
        }
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        handler.addAllTracks(playlist);

        if(!handler.isPlaying) {
            handler.playList();
        }
    }

    @Override
    public void noMatches() {
        mapData.getMessageChannel(handler).sendMessage("Can't play the music from \"" + url + "\".").queue();
        logger.error("Can't play the music from \"" + url + "\".");
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        mapData.getMessageChannel(handler).sendMessage("Some thing is go in wrong.\nPlease contact the administrator.").queue();
        logger.error(exception.getMessage(), exception);
    }
}
