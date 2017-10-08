package com.startogamu.zikobot

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.support.multidex.MultiDex
import com.facebook.stetho.Stetho
import com.joxad.androidtemplate.core.log.AppLog
import com.joxad.androidtemplate.core.network.NetworkStatusManager
import com.joxad.zikobot.data.AppPrefs
import com.startogamu.zikobot.player.alarm.AlarmManager
import com.joxad.zikobot.data.db.CurrentPlaylistManager
import com.joxad.zikobot.data.db.PlaylistManager
import com.joxad.zikobot.data.db.ZikoDB
import com.joxad.zikobot.data.module.accounts.AccountManager
import com.joxad.zikobot.data.module.lastfm.LastFmManager
import com.joxad.zikobot.data.module.localmusic.manager.LocalMusicManager
import com.joxad.zikobot.data.module.spotify_api.manager.SpotifyApiManager
import com.joxad.zikobot.data.module.spotify_auth.manager.SpotifyAuthManager
import com.raizlabs.android.dbflow.config.DatabaseConfig
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.runtime.DirectModelNotifier
import com.startogamu.zikobot.player.PlayerService


/**
 * Created by Jocelyn on 27/07/2017.
 */

class ZikobotApp : Application() {

    private lateinit var musicConnection: ServiceConnection

    private var playerService: PlayerService? = null

    override fun onCreate() {
        super.onCreate()
        NetworkStatusManager.INSTANCE.register(this)
        AppPrefs.init(this)
        val databaseConfig = DatabaseConfig.Builder(ZikoDB::class.java).modelNotifier(DirectModelNotifier.get()).build()
        FlowManager.init(FlowConfig.Builder(this).addDatabaseConfig(databaseConfig).build())
        LocalMusicManager.INSTANCE.init(this)
        AppLog.INSTANCE.init("Zikobot")
        Stetho.initializeWithDefaults(this)
        SpotifyAuthManager.INSTANCE.init(this, R.string.api_spotify_id, R.string.api_spotify_secret)
        SpotifyApiManager.INSTANCE.init(this)
        LastFmManager.INSTANCE.init(this)
        AccountManager.INSTANCE.init()
        PlaylistManager.INSTANCE.init()
        AlarmManager.INSTANCE.init(this)
        CurrentPlaylistManager.INSTANCE.init()


        musicConnection = object : ServiceConnection {
            override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
                val binder = iBinder as PlayerService.PlayerBinder
                playerService = binder.service
            }

            override fun onServiceDisconnected(componentName: ComponentName) {
            }
        }
        bindService(NavigationManager.intentPlayerService(this), musicConnection, Context.BIND_AUTO_CREATE)

    }


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

}
