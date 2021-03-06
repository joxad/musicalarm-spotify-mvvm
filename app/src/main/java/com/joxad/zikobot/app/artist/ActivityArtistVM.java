package com.joxad.zikobot.app.artist;

import android.databinding.ObservableArrayList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.joxad.easydatabinding.activity.ActivityBaseVM;
import com.joxad.zikobot.app.BR;
import com.joxad.zikobot.app.R;
import com.joxad.zikobot.app.album.AlbumVM;
import com.joxad.zikobot.app.core.fragmentmanager.NavigationManager;
import com.joxad.zikobot.app.core.utils.Constants;
import com.joxad.zikobot.app.core.utils.EXTRA;
import com.joxad.zikobot.app.core.utils.ZikoUtils;
import com.joxad.zikobot.app.databinding.ActivityArtistBinding;
import com.joxad.zikobot.app.localtracks.TrackVM;
import com.joxad.zikobot.app.player.PlayerVM;
import com.joxad.zikobot.app.player.event.EventAddList;
import com.joxad.zikobot.app.soundcloud.SoundCloudPlaylistVM;
import com.joxad.zikobot.data.model.Album;
import com.joxad.zikobot.data.model.Artist;
import com.joxad.zikobot.data.model.TYPE;
import com.joxad.zikobot.data.model.Track;
import com.joxad.zikobot.data.module.localmusic.manager.LocalMusicManager;
import com.joxad.zikobot.data.module.localmusic.model.LocalAlbum;
import com.joxad.zikobot.data.module.localmusic.model.LocalTrack;
import com.joxad.zikobot.data.module.soundcloud.manager.SoundCloudApiManager;
import com.joxad.zikobot.data.module.soundcloud.model.SoundCloudPlaylist;
import com.joxad.zikobot.data.module.soundcloud.model.SoundCloudTrack;
import com.joxad.zikobot.data.module.spotify_api.manager.SpotifyApiManager;
import com.joxad.zikobot.data.module.spotify_api.model.SpotifyAlbum;
import com.joxad.zikobot.data.module.spotify_api.model.SpotifyTrack;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.parceler.Parcels;

import me.tatarka.bindingcollectionadapter2.ItemBinding;

/**
 * Created by josh on 08/08/16.
 */
public class ActivityArtistVM extends ActivityBaseVM<ActivityArtist, ActivityArtistBinding> {

    public ItemBinding itemViewAlbum = ItemBinding.of(BR.albumVM, R.layout.item_album);
    public ItemBinding itemViewTrack = ItemBinding.of(BR.trackVM, R.layout.item_track);
    public ItemBinding itemViewSCPlaylist = ItemBinding.of(BR.playlistVM, R.layout.item_soundcloud_playlist);

    public PlayerVM playerVM;
    public ObservableArrayList<AlbumVM> albums;
    public ObservableArrayList<SoundCloudPlaylistVM> soundCloudPlaylistVMs;
    public ObservableArrayList<TrackVM> tracks;

    Artist artist;
    boolean loaded = false;
    NavigationManager navigationManager;

    /***
     * @param activity
     * @param binding
     */
    public ActivityArtistVM(ActivityArtist activity, ActivityArtistBinding binding,@Nullable Bundle saved) {
        super(activity, binding,saved);
    }

    @Override
    public void onCreate(@Nullable Bundle saved) {
        artist = Parcels.unwrap(activity.getIntent().getParcelableExtra(EXTRA.LOCAL_ARTIST));
        albums = new ObservableArrayList<>();
        tracks = new ObservableArrayList<>();
        soundCloudPlaylistVMs = new ObservableArrayList<>();
        navigationManager = new NavigationManager(activity);
        initToolbar();
        initPlayerVM();

    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        if (loaded) return;
        handler.postDelayed(() -> {
            ZikoUtils.animateScale(binding.fabPlay);
            ZikoUtils.animateFade(binding.customToolbar.rlOverlay);
            switch (artist.getType()) {
                case TYPE.DEEZER:
                    break;
                case TYPE.SOUNDCLOUD:
                    loadSoundCloudData();
                    break;
                case TYPE.LOCAL:
                    loadLocalData();
                    break;
                case TYPE.SPOTIFY:
                    loadSpotifyData();
                    break;
                default:
                    break;
            }

            loaded = true;
        }, 400);
    }

    private void loadSpotifyData() {
        loadSpotifyTracks();
        loadSpotifyAlbums(0);
    }


    private void loadSoundCloudData() {
        loadSoundCloudTracks();
        loadSoundCloudPlaylist();
    }

    private void loadLocalData() {
        loadLocalAlbums(15, 0);
        loadLocalTracks(15, 0);
    }

    /***
     * Init the toolar
     */
    private void initToolbar() {
        ZikoUtils.prepareToolbar(activity, binding.customToolbar, artist.getName(), artist.getImage());
    }


    /***
     *
     */

    private void initPlayerVM() {
        playerVM = new PlayerVM(activity, binding.viewPlayer);
    }


    @Override
    public void onResume() {
        super.onResume();
        navigationManager.onResume();
        playerVM.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        playerVM.onPause();
        navigationManager.onPause();
    }

    /**
     *
     */
    private void loadSpotifyTracks() {
        SpotifyApiManager.getInstance().getTopTracks(artist.getId()).subscribe(spotifyTopTracks -> {
            for (SpotifyTrack spotifyTrack : spotifyTopTracks.getTracks()) {
                tracks.add(new TrackVM(activity, Track.from(spotifyTrack)));
            }
        }, throwable -> Logger.e(throwable.getLocalizedMessage()));
    }

    private void loadSpotifyAlbums(int offset) {
        SpotifyApiManager.getInstance().getAlbums(artist.getId(), Constants.ALBUM_LIMIT, offset).subscribe(spotifyArtistAlbums -> {
            for (SpotifyAlbum album : spotifyArtistAlbums.items) {
                albums.add(new AlbumVM(activity, Album.from(album)));
            }
        }, throwable -> Logger.e(throwable.getLocalizedMessage()));
    }

    /***
     * Load the soundcloud music
     */
    private void loadSoundCloudTracks() {
        SoundCloudApiManager.getInstance().userTracks(Long.parseLong(artist.getId())).subscribe(soundCloudTracks -> {
            for (SoundCloudTrack soundCloudTrack : soundCloudTracks) {
                tracks.add(new TrackVM(activity, Track.from(soundCloudTrack, activity.getString(R.string.soundcloud_id))));
            }
        }, throwable -> Logger.e(throwable.getLocalizedMessage()));
    }

    /***
     * Load the soundcloud music
     */
    private void loadSoundCloudPlaylist() {
        SoundCloudApiManager.getInstance().userPlaylists(Long.parseLong(artist.getId())).subscribe(soundCloudPlaylists -> {
            for (SoundCloudPlaylist soundCloudPlaylist : soundCloudPlaylists) {
                soundCloudPlaylistVMs.add(new SoundCloudPlaylistVM(activity, soundCloudPlaylist));
            }
        }, throwable -> Logger.e(throwable.getLocalizedMessage()));
    }

    /***
     * Load the local music
     */
    private void loadLocalAlbums(int limit, int offset) {
        LocalMusicManager.getInstance().getLocalAlbums(limit, offset, artist != null ? artist.getName() : null, null).subscribe(localAlbums -> {
            for (LocalAlbum localAlbum : localAlbums) {
                albums.add(new AlbumVM(activity, Album.from(localAlbum)));
            }
        }, throwable -> {
            Logger.e(throwable.getLocalizedMessage());
        });
    }

    private void loadLocalTracks(int limit, int offset) {
        LocalMusicManager.getInstance().getLocalTracks(limit, offset, artist != null ? artist.getName() : null, -1, null).subscribe(localTracks -> {
            for (LocalTrack localTrack : localTracks) {
                tracks.add(new TrackVM(activity, Track.from(localTrack)));
            }

        }, throwable -> {
            Logger.e(throwable.getLocalizedMessage());
        });
    }

    public void onPlay(@SuppressWarnings("unused") View view) {
        EventBus.getDefault().post(new EventAddList(tracks));
    }

    @Override
    protected boolean onBackPressed() {
        if (playerVM.onBackPressed()) {
            binding.fabPlay.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

}
