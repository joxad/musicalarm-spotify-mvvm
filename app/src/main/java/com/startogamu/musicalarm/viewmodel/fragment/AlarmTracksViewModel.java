package com.startogamu.musicalarm.viewmodel.fragment;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.util.Log;
import android.view.View;

import com.android.databinding.library.baseAdapters.BR;
import com.joxad.android_easy_spotify.SpotifyManager;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Spotify;
import com.startogamu.musicalarm.MusicAlarmApplication;
import com.startogamu.musicalarm.R;
import com.startogamu.musicalarm.databinding.FragmentAlarmTracksBinding;
import com.startogamu.musicalarm.di.manager.AlarmManager;
import com.startogamu.musicalarm.di.manager.spotify_api.SpotifyAPIManager;
import com.startogamu.musicalarm.model.Alarm;
import com.startogamu.musicalarm.model.AlarmTrack;
import com.startogamu.musicalarm.utils.SpotifyPrefs;
import com.startogamu.musicalarm.view.fragment.AlarmTracksFragment;
import com.startogamu.musicalarm.viewmodel.ViewModel;
import com.startogamu.musicalarm.viewmodel.items.ItemTrackViewModel;

import net.droidlabs.mvvm.recyclerview.adapter.binder.ItemBinder;
import net.droidlabs.mvvm.recyclerview.adapter.binder.ItemBinderBase;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * Created by josh on 31/03/16.
 */

/***
 * {@link AlarmTracksViewModel}  make the link between {@link AlarmTracksFragment} and {@link AlarmManager}
 */
public class AlarmTracksViewModel extends BaseObservable implements ViewModel {

    private final FragmentAlarmTracksBinding binding;
    public String TAG = AlarmTracksViewModel.class.getSimpleName();
    AlarmTracksFragment context;
    @Inject
    SpotifyAPIManager spotifyAPIManager;
    /***
     * The observable list of tracks selected for this alarm
     */
    private ObservableArrayList<ItemTrackViewModel> tracks = new ObservableArrayList<>();

    /***
     * @param context
     * @param binding
     */
    public AlarmTracksViewModel(AlarmTracksFragment context, FragmentAlarmTracksBinding binding) {
        MusicAlarmApplication.get(context.getActivity()).netComponent.inject(this);
        this.context = context;

        this.binding = binding;

    }

    /***
     * Init the viewmodel with its alarm
     *
     * @param alarm
     */
    public void setAlarm(Alarm alarm) {
        for (AlarmTrack alarmTrack : alarm.getTracks()) {
            ItemTrackViewModel itemTrackViewModel = new ItemTrackViewModel(context, binding);
            itemTrackViewModel.setAlarmTrack(alarmTrack);
            tracks.add(itemTrackViewModel);
        }

    }


    public void onPlayClick(View view) {
        Config playerConfig = new Config(context.getActivity(), SpotifyPrefs.getAcccesToken(), context.getString(R.string.api_spotify_id));

        Spotify.getPlayer(playerConfig, context, new Player.InitializationObserver() {
            @Override
            public void onInitialized(Player pl) {
                ArrayList<String> uris = new ArrayList<String>();
                for (AlarmTrack alarmTrack : getTracks()) {
                    uris.add(alarmTrack.getRef());
                }
                pl.play(uris);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(SpotifyManager.class.getSimpleName(), "Could not initialize player: " + throwable.getMessage());

            }
        });
    }

    public void onDestroy() {

    }

    @Bindable
    public ObservableArrayList<ItemTrackViewModel> getAlarmTracks() {
        return tracks;
    }

    public ItemBinder<ItemTrackViewModel> itemTracksBinder() {
        return new ItemBinderBase<>(BR.itemTrackViewModel, R.layout.item_track);
    }

    /**
     * @param alarmTrack
     */
    public void add(AlarmTrack alarmTrack) {
        ItemTrackViewModel itemTrackViewModel = new ItemTrackViewModel(context, binding);
        itemTrackViewModel.setAlarmTrack(alarmTrack);
        tracks.add(itemTrackViewModel);
        notifyPropertyChanged(BR.alarmTracksViewModel);
    }

    public ArrayList<AlarmTrack> getTracks() {
        ArrayList<AlarmTrack> alarmTracks = new ArrayList<>();
        for (ItemTrackViewModel trackViewModel : tracks) {
            alarmTracks.add(trackViewModel.getAlarmTrack());
        }
        return alarmTracks;
    }
}
