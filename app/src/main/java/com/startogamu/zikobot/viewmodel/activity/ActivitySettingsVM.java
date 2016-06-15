package com.startogamu.zikobot.viewmodel.activity;

/**
 * Created by josh on 25/03/16.
 */

import android.databinding.ObservableBoolean;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.joxad.android_easy_spotify.Scope;
import com.joxad.easydatabinding.activity.ActivityBaseVM;
import com.startogamu.zikobot.R;
import com.startogamu.zikobot.core.utils.AppPrefs;
import com.startogamu.zikobot.databinding.ActivitySettingsBinding;
import com.startogamu.zikobot.module.component.Injector;
import com.startogamu.zikobot.module.spotify_api.model.SpotifyUser;
import com.startogamu.zikobot.module.spotify_auth.model.SpotifyRequestToken;
import com.startogamu.zikobot.module.spotify_auth.model.SpotifyToken;
import com.startogamu.zikobot.view.activity.ActivitySettings;
import com.startogamu.zikobot.view.fragment.FragmentWebView;

import java.io.UnsupportedEncodingException;

import rx.Subscriber;

/***
 * {@link ActivitySettingsVM}  make the link between {@link ActivitySettings} and {@link com.startogamu.zikobot.module.spotify_api.manager.SpotifyApiManager}
 */
public class ActivitySettingsVM extends ActivityBaseVM<ActivitySettings, ActivitySettingsBinding> {

    public String TAG = ActivitySettingsVM.class.getSimpleName();

    String accessCode;
    String accessToken;

    public ObservableBoolean showSpotifyConnect;

    public ObservableBoolean showSoundCloudConnect;

    public ActivitySettingsVM(ActivitySettings activity, ActivitySettingsBinding binding) {
        super(activity, binding);
        Injector.INSTANCE.spotifyApi().inject(this);
        Injector.INSTANCE.spotifyAuth().inject(this);
    }


    @Override
    public void init() {
        showSpotifyConnect = new ObservableBoolean(false);
        showSoundCloudConnect = new ObservableBoolean(true);

        activity.setSupportActionBar(binding.toolbar);
        activity.getSupportActionBar().setTitle(R.string.action_settings);
        if (AppPrefs.getSpotifyAccessCode().equals("")) {
            showSpotifyConnect.set(true);
        } else {
            getMe();
        }
    }


    /***
     * @param view
     */
    public void onButtonConnectClick(View view) {
        String apiKey = activity.getString(R.string.api_spotify_id);
        String redirect_uri = activity.getString(R.string.api_spotify_callback_web_view);
        String baseUrl = activity.getString(R.string.spotify_web_view);
        FragmentWebView fragmentWebView = FragmentWebView.newInstance(String.format(baseUrl, apiKey, redirect_uri, Scope.USER_READ_PRIVATE+"%20"+Scope.STREAMING));

        fragmentWebView.show(activity.getSupportFragmentManager(), FragmentWebView.TAG);
       /* SpotifyManager.loginWithBrowser(activity, Type.CODE, R.string.api_spotify_callback_settings, new String[]{Scope.USER_READ_PRIVATE, Scope.STREAMING}, new SpotifyManager.OAuthListener() {
            @Override
            public void onReceived(String code) {
                accessCode = code;
                AppPrefs.saveAccessCode(accessCode);
                getTokenFromCode();
            }

            @Override
            public void onError(String error) {
                Log.d(TAG, error);
            }
        });*/
    }


    /***
     * @param view
     */
    public void onButtonSoundCloudConnect(View view) {

        String apiKey = activity.getString(R.string.soundcloud_id);
        String redirect_uri = activity.getString(R.string.api_soundcloud_callback);
        String baseUrl = activity.getString(R.string.soundclound_web_view);
        FragmentWebView fragmentWebView = FragmentWebView.newInstance(String.format(baseUrl, apiKey, redirect_uri));

        fragmentWebView.show(activity.getSupportFragmentManager(), FragmentWebView.TAG);
    }


    private void getTokenFromCode() {
        try {
            Injector.INSTANCE.spotifyAuth().manager().requestToken(
                    new SpotifyRequestToken("authorization_code", accessCode,
                            activity.getString(R.string.api_spotify_callback_settings),
                            activity.getString(R.string.api_spotify_id),
                            activity.getString(R.string.api_spotify_secret))).subscribe(
                    new Subscriber<SpotifyToken>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, e.getLocalizedMessage());

                        }

                        @Override
                        public void onNext(SpotifyToken spotifyToken) {
                            Log.d(TAG, spotifyToken.toString());
                            accessToken = spotifyToken.getAccessToken();
                            AppPrefs.saveAccessToken(accessToken);
                            AppPrefs.saveRefreshToken(spotifyToken.getRefreshToken());

                            getMe();
                        }
                    });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    public void getMe() {
        Injector.INSTANCE.spotifyApi().manager().getMe().subscribe(new Subscriber<SpotifyUser>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.getLocalizedMessage());
            }

            @Override
            public void onNext(SpotifyUser spotifyUser) {
                binding.tvUser.setText(spotifyUser.getDisplayName());
                Glide.with(activity).load(spotifyUser.getImages().get(0).getUrl()).into(binding.ivUser);
                AppPrefs.spotifyUser(spotifyUser);
                showSpotifyConnect.set(false);

            }
        });
    }


    @Override
    public void destroy() {

    }
}
