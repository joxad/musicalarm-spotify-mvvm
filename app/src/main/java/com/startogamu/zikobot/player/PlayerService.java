package com.startogamu.zikobot.player;

import android.app.Service;
import android.content.Intent;
import android.databinding.ObservableArrayList;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.startogamu.zikobot.core.event.player.EventPlayTrack;
import com.startogamu.zikobot.core.module.music.player.IMusicPlayer;
import com.startogamu.zikobot.localtracks.TrackVM;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

/**
 * Created by Jocelyn on 12/12/2016.
 */

public class PlayerService extends Service implements IMusicPlayer {

    public ObservableArrayList<TrackVM> trackVMs;
    public TrackVM currentTrackVM;
    private MediaPlayer vlcPlayer;



    LibVLC libVLC;
    private final IBinder musicBind = new PlayerService.PlayerBinder();

    public class PlayerBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }


    @Override
    public void init() {
        libVLC = new LibVLC();
        vlcPlayer = new MediaPlayer(libVLC);
    }

    @Subscribe
    public void onEvent(EventPlayTrack eventPlayTrack) {
        play(eventPlayTrack.getTrack().getRef());
    }

    @Override
    public void play(String ref) {
        vlcPlayer.setMedia(new Media(libVLC, ref));
        vlcPlayer.play();
    }

    @Override
    public void pause() {
        vlcPlayer.pause();
    }

    @Override
    public void resume() {
        vlcPlayer.play();
    }

    @Override
    public void stop() {
        vlcPlayer.stop();
    }

    @Override
    public void seekTo(int position) {
        vlcPlayer.setPosition(position);
    }

    @Override
    public float position() {
        return vlcPlayer.getPosition();
    }

    @Override
    public void next() {
        vlcPlayer.nextChapter();
    }


    @Override
    public boolean onUnbind(Intent intent) {
        vlcPlayer.release();
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
