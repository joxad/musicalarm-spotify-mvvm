package com.joxad.zikobot.app;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.joxad.android_easy_spotify.SpotifyManager;
import com.joxad.zikobot.data.module.deezer.DeezerManager;
import com.joxad.zikobot.data.module.spotify_auth.manager.SpotifyAuthManager;
import com.joxad.zikobot.app.player.PlayerService;
import com.orhanobut.logger.Logger;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.joxad.zikobot.app.alarm.AlarmManager;
import com.joxad.zikobot.app.core.fragmentmanager.IntentManager;
import com.joxad.zikobot.data.module.localmusic.manager.LocalMusicManager;
import com.joxad.zikobot.data.module.lyrics.manager.LyricsManager;
import com.joxad.zikobot.data.module.soundcloud.manager.SoundCloudApiManager;
import com.joxad.zikobot.data.module.spotify_api.manager.SpotifyApiManager;
import com.joxad.zikobot.data.AppPrefs;

import io.fabric.sdk.android.Fabric;

/**
 * {@link ZikobotApp} is used to initiate our Dagger classes + {@link FlowManager} + {@link AppPrefs} + {@link SpotifyManager}
 */
public class ZikobotApp extends Application {

    private ServiceConnection musicConnection;
    private PlayerService playerService;

    @Override
    public void onCreate() {
        super.onCreate();
        FlowManager.init(new FlowConfig.Builder(this).build());
        AppPrefs.init(this);
        IntentManager.init(this);
        DeezerManager.getInstance().init(this, getString(R.string.deezer_id));
        SpotifyAuthManager.getInstance().init(this, getString(R.string.api_spotify_id), getString(R.string.api_spotify_secret));
        SoundCloudApiManager.getInstance().init(this);
        SpotifyApiManager.getInstance().init(this);
        LocalMusicManager.getInstance().init(this);
        LyricsManager.getInstance().init(this);
        AlarmManager.init(this);

        Logger.init(ZikobotApp.class.getSimpleName());

        Fabric.with(this, new Crashlytics());
        initPlayerService();
    }

    private void initPlayerService() {
        musicConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) iBinder;
                playerService = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        bindService(new Intent(this, PlayerService.class), musicConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void attachBaseContext(Context base) {
        MultiDex.install(base);
        super.attachBaseContext(base);
    }

    public static ZikobotApp get(Context context) {
        return (ZikobotApp) context.getApplicationContext();
    }


}