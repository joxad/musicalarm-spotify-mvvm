package com.startogamu.zikobot.home.artists;

import com.joxad.easydatabinding.fragment.v4.FragmentBase;
import com.startogamu.zikobot.Constants;
import com.startogamu.zikobot.R;
import com.startogamu.zikobot.BR;
import com.startogamu.zikobot.databinding.SimilarArtistsFragmentBinding;
import android.os.Bundle;

import android.support.annotation.Nullable;
/**
 * Generated by generator-android-template
 */
public class SimilarArtistsFragment extends FragmentBase<SimilarArtistsFragmentBinding, SimilarArtistsFragmentVM> {


  public static SimilarArtistsFragment newInstance(int artistId) {
       Bundle args = new Bundle();
       args.putInt(Constants.Extra.INSTANCE.getARTIST_ID(), artistId);
       SimilarArtistsFragment fragment = new SimilarArtistsFragment();
       fragment.setArguments(args);
       return fragment;
   }
    @Override
    public int data() {
        return BR.similarArtistsFragmentVM;
    }

    @Override
    public int layoutResources() {
        return R.layout.similar_artists_fragment;
    }

    @Override
    public SimilarArtistsFragmentVM baseFragmentVM(SimilarArtistsFragmentBinding binding,@Nullable Bundle savedInstance) {
        return new SimilarArtistsFragmentVM(this, binding,savedInstance);
    }
}
