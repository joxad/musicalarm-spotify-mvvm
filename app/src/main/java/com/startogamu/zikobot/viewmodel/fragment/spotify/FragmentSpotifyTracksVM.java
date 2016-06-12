package com.startogamu.zikobot.viewmodel.fragment.spotify;

import android.databinding.ObservableArrayList;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.joxad.easydatabinding.fragment.FragmentBaseVM;
import com.startogamu.zikobot.core.event.SelectAllTracks;
import com.startogamu.zikobot.core.event.navigation_manager.EventCollapseToolbar;
import com.startogamu.zikobot.core.event.navigation_manager.EventTabBars;
import com.startogamu.zikobot.core.utils.EXTRA;
import com.startogamu.zikobot.databinding.FragmentSpotifyTracksBinding;
import com.startogamu.zikobot.module.alarm.model.AlarmTrack;
import com.startogamu.zikobot.module.component.Injector;
import com.startogamu.zikobot.module.mock.Mock;
import com.startogamu.zikobot.module.spotify_api.model.Item;
import com.startogamu.zikobot.module.spotify_api.model.SpotifyPlaylistItem;
import com.startogamu.zikobot.view.fragment.spotify.FragmentSpotifyTracks;
import com.startogamu.zikobot.viewmodel.base.TrackVM;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import me.tatarka.bindingcollectionadapter.ItemView;


/***
 * {@link FragmentSpotifyTracksVM} will call the apimanager to get the tracks of the playlist
 */
public abstract class FragmentSpotifyTracksVM extends FragmentBaseVM<FragmentSpotifyTracks, FragmentSpotifyTracksBinding> {

    private static final String TAG = FragmentSpotifyTracksVM.class.getSimpleName();
    public ObservableArrayList<TrackVM> items;


    public abstract ItemView getItemView();


    @Nullable
    @InjectExtra(EXTRA.PLAYLIST)
    Item playlist;

    public FragmentSpotifyTracksVM(FragmentSpotifyTracks fragment, FragmentSpotifyTracksBinding binding) {
        super(fragment, binding);
        Dart.inject(this, fragment.getArguments());
        this.items = new ObservableArrayList<>();
        this.fragment = fragment;
        this.binding = binding;
        EventBus.getDefault().register(this);
        Injector.INSTANCE.spotifyApi().inject(this);

        loadTracks(playlist);
    }

    @Override
    public void init() {
    }

    /***
     * FInd the list of track from the playlist
     *
     * @param playlist
     */
    private void loadTracks(Item playlist) {
        items.clear();
        items.addAll(Mock.tracks(fragment.getContext(), playlist.tracks.getTotal()));
        Injector.INSTANCE.spotifyApi().manager().getPlaylistTracks(playlist.getId()).subscribe(spotifyPlaylistWithTrack -> {
            items.clear();
            for (SpotifyPlaylistItem playlistItem : spotifyPlaylistWithTrack.getItems()) {
                items.add(new TrackVM(fragment.getContext(), AlarmTrack.from(playlistItem.track)));
            }
        }, throwable -> {
            Snackbar.make(binding.getRoot(), throwable.getLocalizedMessage(), Snackbar.LENGTH_SHORT).show();
        });
    }

    @Subscribe
    public void onEvent(SelectAllTracks selectAllTracks) {
        for (TrackVM trackVM : items) {
            trackVM.select();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (playlist != null) {
            EventBus.getDefault().post(new EventCollapseToolbar(playlist.getName(), playlist.getImages().get(0).getUrl()));
            EventBus.getDefault().post(new EventTabBars(false, TAG));
        } else {
            EventBus.getDefault().post(new EventCollapseToolbar(null, null));
            EventBus.getDefault().post(new EventTabBars(true, TAG));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void destroy() {
    }
}
