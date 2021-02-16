package com.tim07.thrawnbot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.audio.AudioSource;

public class Music {
    AudioPlayerManager playerManager;
    AudioPlayer player;
    AudioSource source;
    AudioConnection audioConnection;

    public Music(DiscordApi api){
        // Create a player manager
        playerManager = new DefaultAudioPlayerManager();
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        player = playerManager.createPlayer();
        player.setVolume(30);


        // Create an audio source and add it to the audio connection's queue
        source = new LavaPlayerAudioSource(api, player);
    }
    private void setAudioConnection(AudioConnection audioConnection){
        audioConnection.setAudioSource(source);
    }

    private void loadTrack(String url){
        playerManager.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                player.playTrack(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks()) {
                    player.playTrack(track);
                }
            }

            @Override
            public void noMatches() {
                // Notify the user that we've got nothing
            }

            @Override
            public void loadFailed(FriendlyException throwable) {
                // Notify the user that everything exploded
            }
        });
    }
    public void setAudioConnectionStandard(AudioConnection audioconnection){
        this.audioConnection = audioconnection;
    }
    public void disconnect(){
        if (audioConnection != null){
            audioConnection.close();
        }
    }
    public void play(String url){
        setAudioConnection(audioConnection);
        loadTrack(url);
    }
}
