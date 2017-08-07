package com.startogamu.zikobot.home.playlists;

import android.content.Context;
import android.databinding.Bindable;

import com.joxad.easydatabinding.base.BaseVM;
import com.joxad.zikobot.data.db.model.ZikoTrack;

/**
 * Generated by generator-android-template
 * <!!!!!> You have to replace the Object to your POJO class that will be handled in the VM <!!!!!>
 */
public class TrackVM extends BaseVM<ZikoTrack> {
    private static final String TAG = TrackVM.class.getSimpleName();

    /***
     * @param context
     * @param model
     */
    public TrackVM(Context context, ZikoTrack model) {
        super(context, model);
    }

    @Override
    public void onCreate() {

    }

    @Bindable
    public String getTitle() {
        return model.getName();
    }

}