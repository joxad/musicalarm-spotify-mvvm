package com.startogamu.musicalarm.viewmodel.fragment;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.android.databinding.library.baseAdapters.BR;
import com.startogamu.musicalarm.MusicAlarmApplication;
import com.startogamu.musicalarm.R;
import com.startogamu.musicalarm.core.utils.REQUEST;
import com.startogamu.musicalarm.databinding.FragmentLocalMusicBinding;
import com.startogamu.musicalarm.di.manager.LocalMusicManager;
import com.startogamu.musicalarm.model.LocalTrack;
import com.startogamu.musicalarm.view.fragment.LocalMusicFragment;
import com.startogamu.musicalarm.viewmodel.RecyclerViewViewModel;
import com.startogamu.musicalarm.viewmodel.ViewModel;
import com.startogamu.musicalarm.viewmodel.items.ItemLocalTrackViewModel;
import com.startogamu.musicalarm.viewmodel.items.ItemSpotifyTrackViewModel;

import net.droidlabs.mvvm.recyclerview.adapter.binder.ItemBinder;
import net.droidlabs.mvvm.recyclerview.adapter.binder.ItemBinderBase;

import java.util.List;

import javax.inject.Inject;

import rx.Subscriber;

/**
 * Created by josh on 26/03/16.
 */
public class LocalMusicViewModel extends BaseObservable implements RecyclerViewViewModel<ItemLocalTrackViewModel> {

    private static final String TAG = LocalMusicViewModel.class.getSimpleName();
    private LocalMusicFragment fragment;
    private FragmentLocalMusicBinding binding;
    private ObservableArrayList<ItemLocalTrackViewModel> localTrackViewModels= new ObservableArrayList<>();

    @Inject
    LocalMusicManager localMusicManager;

    public LocalMusicViewModel(LocalMusicFragment fragment, FragmentLocalMusicBinding binding) {

        this.fragment = fragment;
        this.binding = binding;
        MusicAlarmApplication.get(fragment.getActivity()).contentComponent.inject(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(fragment.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(fragment.getActivity(),new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST.PERMISSION_STORAGE);
            } else {
                loadLocalMusic();
            }
        } else {
            loadLocalMusic();
            // continue with your code
        }

    }

    public void loadLocalMusic() {
        localMusicManager.getLocalTracks().subscribe(new Subscriber<List<LocalTrack>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG,""+e.getLocalizedMessage());

            }

            @Override
            public void onNext(List<LocalTrack> localTracks) {
                Log.d(TAG,""+localTracks.size());
                for (LocalTrack localTrack : localTracks) {
                    localTrackViewModels.add(new ItemLocalTrackViewModel(fragment ,localTrack));
                }
            }
        });
    }


    @Bindable
    @Override
    public ObservableArrayList<ItemLocalTrackViewModel> getItems() {
        return localTrackViewModels;
    }

    @Override
    public ItemBinder<ItemLocalTrackViewModel> getItemsBinder() {
        return new ItemBinderBase<>(BR.itemLocalTrackViewModel, R.layout.item_local_track);
    }



    @Override
    public void onDestroy() {

    }
}
