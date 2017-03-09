package com.joxad.zikobot.app.player.player;

import android.content.Context;
import android.util.Log;

import com.joxad.zikobot.app.R;
import com.joxad.zikobot.app.player.event.EventNextTrack;
import com.joxad.zikobot.data.AppPrefs;
import com.joxad.zikobot.data.module.spotify_auth.manager.SpotifyAuthManager;
import com.orhanobut.logger.Logger;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;

import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by josh on 28/08/16.
 */
public class SpotifyPlayer implements IMusicPlayer {

    private final Context context;

    protected Player player;
    private final Player.OperationCallback mOperationCallback = new Player.OperationCallback() {
        @Override
        public void onSuccess() {
            //logStatus("OK!");
            Logger.d("OK");
        }

        @Override
        public void onError(Error error) {
            Logger.e("OK");
        }
    };

    Player.NotificationCallback notificationCallback = new Player.NotificationCallback() {
        @Override
        public void onPlaybackEvent(PlayerEvent playerEvent) {
            if (playerEvent == PlayerEvent.kSpPlaybackNotifyAudioDeliveryDone)
                EventBus.getDefault().post(new EventNextTrack());
        }

        @Override
        public void onPlaybackError(Error error) {

        }
    };
    //    PlayerNotificationCallback playerNotificationCallback = new PlayerNotificationCallback() {
//        @Override
//        public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
//            if (playerState.positionInMs >= playerState.durationInMs && playerState.durationInMs > 0)
//                EventBus.getDefault().post(new EventNextTrack());
//            Logger.d(String.format("Player state %s - activeDevice %s : current duration %d total duration %s", playerState.trackUri, playerState.activeDevice, playerState.positionInMs, playerState.durationInMs));
//        }
//
//        @Override
//        public void onPlaybackError(ErrorType errorType, String s) {
//            Logger.e(errorType.name() + " " + s);
//        }
//    };
    private ConnectionStateCallback connexionStateCallback = new ConnectionStateCallback() {
        @Override
        public void onLoggedIn() {
            Logger.d("LoggedIn");
        }

        @Override
        public void onLoggedOut() {
            Logger.d("LoggedOut");
        }

        @Override
        public void onLoginFailed(Error error) {
            Logger.d("onLoginFailed %s", error.toString());

        }


        @Override
        public void onTemporaryError() {
            Logger.d("onTemporaryError");
        }

        @Override
        public void onConnectionMessage(String s) {
            Logger.d("onConnectionMessage %s", s);
        }
    };


    public SpotifyPlayer(Context context) {
        this.context = context;
    }

    @Override
    public void init() {
        Config playerConfig = new Config(context, AppPrefs.getSpotifyAccessToken(), context.getString(R.string.api_spotify_id));
        Spotify.getPlayer(playerConfig, context, new com.spotify.sdk.android.player.SpotifyPlayer.InitializationObserver() {
            @Override
            public void onInitialized(com.spotify.sdk.android.player.SpotifyPlayer pl) {
                player = pl;
                player.addNotificationCallback(notificationCallback);
                player.addConnectionStateCallback(connexionStateCallback);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(SpotifyPlayer.class.getSimpleName(), "Could not initialize player: " + throwable.getMessage());

            }
        });

    }

    @Override
    public void play(String ref) {
        player.playUri(mOperationCallback, ref, 0, 0);
    }

    @Override
    public void pause() {
        player.pause(mOperationCallback);
    }

    @Override
    public void resume() {
        player.resume(mOperationCallback);
    }

    @Override
    public void stop() {
        player.pause(mOperationCallback);

    }

    @Override
    public void seekTo(float percent) {

    }

    @Override
    public void seekTo(int position) {
        player.seekToPosition(mOperationCallback, position);
    }

    public Observable<Boolean> updateToken() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    SpotifyAuthManager.getInstance().refreshToken(context, (newToken, tokenIdentical) -> {
                        if (!tokenIdentical) {
                            Config playerConfig = new Config(context, newToken, context.getString(R.string.api_spotify_id));
                            Spotify.getPlayer(playerConfig, context, new com.spotify.sdk.android.player.SpotifyPlayer.InitializationObserver() {
                                @Override
                                public void onInitialized(com.spotify.sdk.android.player.SpotifyPlayer pl) {
                                    player = pl;
                                    player.addNotificationCallback(notificationCallback);
                                    player.addConnectionStateCallback(connexionStateCallback);
                                    subscriber.onNext(true);

                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    Log.e(SpotifyPlayer.class.getSimpleName(), "Could not initialize player: " + throwable.getMessage());
                                    subscriber.onError(throwable);

                                }
                            });
                        } else {
                            subscriber.onNext(true);
                        }
                    });
                } catch (UnsupportedEncodingException e) {
                    subscriber.onError(new Throwable("UnsupportedEncodingException"));
                }
            }
        });
    }
}
