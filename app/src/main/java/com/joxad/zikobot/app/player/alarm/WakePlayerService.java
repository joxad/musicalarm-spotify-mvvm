package com.joxad.zikobot.app.player.alarm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.joxad.zikobot.app.core.notification.PlayerNotification;
import com.joxad.zikobot.app.core.receiver.ZikoMediaCallback;
import com.joxad.zikobot.app.localtracks.TrackVM;
import com.joxad.zikobot.app.player.event.EventAccountConnect;
import com.joxad.zikobot.app.player.event.EventNextTrack;
import com.joxad.zikobot.app.player.event.EventPauseMediaButton;
import com.joxad.zikobot.app.player.event.EventPlayMediaButton;
import com.joxad.zikobot.app.player.event.EventPlayTrack;
import com.joxad.zikobot.app.player.event.EventStopPlayer;
import com.joxad.zikobot.app.player.event.TrackChangeEvent;
import com.joxad.zikobot.app.player.player.AndroidPlayer;
import com.joxad.zikobot.app.player.player.IMusicPlayer;
import com.joxad.zikobot.app.player.player.SpotifyPlayer;
import com.joxad.zikobot.app.player.player.VLCPlayer;
import com.joxad.zikobot.data.AppPrefs;
import com.joxad.zikobot.data.model.TYPE;
import com.joxad.zikobot.data.model.Track;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

/**
 * Created by Jocelyn on 12/12/2016.
 */

public class WakePlayerService extends Service implements IMusicPlayer {

    public static final int DELAY = 200;
    private final IBinder musicBind = new WakePlayerService.WakePlayerBinder();
    public ObservableArrayList<TrackVM> trackVMs = new ObservableArrayList<>();
    public TrackVM currentTrackVM;
    public ObservableBoolean playing = new ObservableBoolean(false);
    PlayerNotification playerNotification;
    VLCPlayer vlcPlayer;
    AndroidPlayer androidPlayer;
    SpotifyPlayer spotifyPlayer;
    private int currentIndex = 0;
    private int currentProgress = 0;
    private MediaSessionCompat mediaSession;
    private Handler timeHandler;
    private IMusicPlayer currentPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        vlcPlayer = new VLCPlayer(this);
        androidPlayer = new AndroidPlayer(this);
        spotifyPlayer = new SpotifyPlayer(this);
        mediaSession = new MediaSessionCompat(this, "WakePlayerService");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(new ZikoMediaCallback());

        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
                .build());

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(focusChange -> {
            // Ignore
        }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        mediaSession.setActive(true);
        EventBus.getDefault().register(this);
        init();
        runHandler();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public void init() {

        playerNotification = new PlayerNotification(this);
        timeHandler = new Handler();
        vlcPlayer.init();
        if (AppPrefs.spotifyUser() != null) {

            spotifyPlayer.init();
        }
        if (AppPrefs.soundCloudUser() != null) {
            androidPlayer.init();
        }
        currentPlayer = vlcPlayer;


    }

    @Subscribe
    public void onEvent(EventAccountConnect event) {
        switch (event.getType()) {
            case TYPE.SOUNDCLOUD:
                androidPlayer.init();
                break;
            case TYPE.SPOTIFY:
                spotifyPlayer.init();
                break;
        }
    }


    @Subscribe(priority = 10)
    public void onEvent(EventNextTrack eventNextTrack) {
        EventBus.getDefault().cancelEventDelivery(eventNextTrack);
        next();
    }

    public void next() {
        if (currentIndex < trackVMs.size() - 1) {
            stopPreviousTrack();
            currentIndex++;
            currentTrackVM = trackVMs.get(currentIndex);
            play(currentTrackVM);
            EventBus.getDefault().post(new TrackChangeEvent());
        } else {
            stop();
        }
    }

    @Subscribe
    public void onEvent(EventPlayTrack eventPlayTrack) {
        stopPreviousTrack();

        boolean foundInCurrentList = false;
        if (!trackVMs.isEmpty()) {
            for (int i = 0; i < trackVMs.size(); i++) {
                TrackVM trackVM = trackVMs.get(i);
                if (trackVM.getModel().getRef().equals(eventPlayTrack.getTrack().getModel().getRef()) && trackVM.getType() == eventPlayTrack.getTrack().getType()) {
                    currentTrackVM = trackVM;
                    currentIndex = i;
                    foundInCurrentList = true;
                }
            }
        }
        if (!foundInCurrentList) {
            currentTrackVM = eventPlayTrack.getTrack();
            trackVMs.add(currentTrackVM);
        }
        play(currentTrackVM);
    }

    public void startAlarm(List<Track> trackList) {
        stopPreviousTrack();
        for (Track track : trackList) {
            trackVMs.add(new TrackVM(this, track));
        }
        currentIndex = 0;
        currentTrackVM = trackVMs.get(currentIndex);
        play(currentTrackVM);
    }


    @Subscribe
    public void onEvent(EventStopPlayer eventStopPlayer) {
        stopForeground(true);
        stop();
    }

    @Subscribe
    public void onEvent(EventPauseMediaButton eventPauseMediaButton) {
        pause();
    }

    @Subscribe
    public void onEvent(EventPlayMediaButton eventPlayMediaButton) {
        if (currentTrackVM != null)
            resume();
        else {

        }
    }

    private void stopPreviousTrack() {
        if (currentTrackVM != null)
            currentTrackVM.isPlaying.set(false);
    }

    private void play(TrackVM currentTrackVM) {
        currentTrackVM.isPlaying.set(true);
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentTrackVM.getArtistName())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentTrackVM.getName())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, currentTrackVM.getDuration())
                .build());
        updatePlayer(currentTrackVM);
    }

    private void updatePlayer(TrackVM currentTrackVM) {
        currentPlayer.stop();
        switch (currentTrackVM.getType()) {
            case TYPE.LOCAL:
                currentPlayer = vlcPlayer;
                break;
            case TYPE.SOUNDCLOUD:
                currentPlayer = androidPlayer;
                break;
            case TYPE.SPOTIFY:
                currentPlayer = spotifyPlayer;
                break;
        }
        play(currentTrackVM.getRef());
    }

    @Override
    public void play(String ref) {
        currentPlayer.play(ref);
        playing.set(true);
        if (currentTrackVM != null) {
            playerNotification.prepareNotification(currentTrackVM.getModel());
        }
        currentProgress = 0;
        EventBus.getDefault().post(new WakeTrackChangeEvent());

    }

    private void runHandler() {
        timeHandler.postDelayed(() -> {
            if (playing.get())
                currentProgress += DELAY;
            runHandler();
        }, DELAY);
    }

    @Override
    public void pause() {
        currentPlayer.pause();
        currentTrackVM.isPlaying.set(false);
        playing.set(false);

    }

    @Override
    public void resume() {
        currentPlayer.resume();
        currentTrackVM.isPlaying.set(true);
        playing.set(true);
    }

    @Override
    public void stop() {
        stopPreviousTrack();
        currentPlayer.stop();
        playing.set(false);
        currentProgress = 0;
        playerNotification.cancel();

    }

    @Override
    public void seekTo(float position) {
        vlcPlayer.seekTo(position);
    }

    @Override
    public void seekTo(int position) {
        currentProgress = position;
        if (currentPlayer instanceof VLCPlayer) seekTo((float) position / (float) positionMax());
        else currentPlayer.seekTo(position);

    }

    public int position() {
        return currentProgress;
    }

    public int positionMax() {
        if (currentTrackVM == null) return 0;
        return (int) currentTrackVM.getModel().getDuration();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopForeground(true);
        return false;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    public boolean isEmpty() {
        return trackVMs.isEmpty();
    }

    public class WakePlayerBinder extends Binder {
        public WakePlayerService getService() {
            return WakePlayerService.this;
        }
    }


}
