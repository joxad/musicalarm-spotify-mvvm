package com.startogamu.zikobot.view.fragment.local;

import android.os.Bundle;

import com.joxad.easydatabinding.fragment.FragmentBase;
import com.startogamu.zikobot.BR;
import com.startogamu.zikobot.R;
import com.startogamu.zikobot.core.event.navigation_manager.EventCollapseToolbar;
import com.startogamu.zikobot.core.event.navigation_manager.EventTabBars;
import com.startogamu.zikobot.databinding.FragmentLocalArtistsBinding;
import com.startogamu.zikobot.viewmodel.fragment.local.FragmentLocalArtistVM;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by josh on 06/06/16.
 */
public class FragmentLocalArtists extends FragmentBase<FragmentLocalArtistsBinding, FragmentLocalArtistVM> {


    public static final String TAG = "FragmentLocalArtists";

    public static FragmentLocalArtists newInstance() {
        Bundle args = new Bundle();
        FragmentLocalArtists fragment = new FragmentLocalArtists();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int data() {
        return BR.fragmentLocalArtistVM;
    }

    @Override
    public int layoutResources() {
        return R.layout.fragment_local_artists;
    }

    @Override
    public FragmentLocalArtistVM baseFragmentVM(FragmentLocalArtistsBinding binding) {
        return new FragmentLocalArtistVM(this, binding);
    }

    @Override
    public void onResume() {
        EventBus.getDefault().post(new EventCollapseToolbar(null, null));
        EventBus.getDefault().post(new EventTabBars(true, TAG));
        super.onResume();
    }
}
