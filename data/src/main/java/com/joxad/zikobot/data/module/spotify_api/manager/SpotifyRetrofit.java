package com.joxad.zikobot.data.module.spotify_api.manager;

import android.support.annotation.Nullable;

import com.joxad.zikobot.data.ws.RetrofitBase;

import okhttp3.Interceptor;

/**
 * Created by Jocelyn on 31/10/2016.
 */

public class SpotifyRetrofit extends RetrofitBase {
    public SpotifyRetrofit(String url, @Nullable Interceptor interceptor) {
        super(url, interceptor);
    }
}
