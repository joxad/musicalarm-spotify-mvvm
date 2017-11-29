package com.startogamu.zikobot.home.artists

import com.joxad.easydatabinding.fragment.v4.FragmentBase
import com.startogamu.zikobot.R
import com.startogamu.zikobot.BR
import com.startogamu.zikobot.databinding.ArtistHomeFragmentBinding
import android.os.Bundle
import com.startogamu.zikobot.Constants

/**
 * Generated by generator-android-template
 */
class ArtistHomeFragment : FragmentBase<ArtistHomeFragmentBinding, ArtistHomeFragmentVM>() {
    override fun data(): Int {
        return BR.artistHomeFragmentVM
    }

    override fun layoutResources(): Int {
        return R.layout.artist_home_fragment
    }

    override fun baseFragmentVM(binding: ArtistHomeFragmentBinding, savedInstance: Bundle?): ArtistHomeFragmentVM {
        return ArtistHomeFragmentVM(this, binding, savedInstance)
    }

    companion object {


        fun newInstance(artistId:Int): ArtistHomeFragment {
            val args = Bundle()
            val fragment = ArtistHomeFragment()
            args.putInt(Constants.Extra.ARTIST_ID, artistId)
            fragment.arguments = args
            return fragment
        }
    }
}