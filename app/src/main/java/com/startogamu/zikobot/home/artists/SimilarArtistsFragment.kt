package com.startogamu.zikobot.home.artists

import com.joxad.easydatabinding.fragment.v4.FragmentBase
import com.startogamu.zikobot.Constants
import com.startogamu.zikobot.R
import com.startogamu.zikobot.BR
import com.startogamu.zikobot.databinding.SimilarArtistsFragmentBinding
import android.os.Bundle

/**
 * Generated by generator-android-template
 */
class SimilarArtistsFragment : FragmentBase<SimilarArtistsFragmentBinding, SimilarArtistsFragmentVM>() {
    override fun data(): Int {
        return BR.similarArtistsFragmentVM
    }

    override fun layoutResources(): Int {
        return R.layout.similar_artists_fragment
    }

    override fun baseFragmentVM(binding: SimilarArtistsFragmentBinding, savedInstance: Bundle?): SimilarArtistsFragmentVM {
        return SimilarArtistsFragmentVM(this, binding, savedInstance)
    }

    companion object {


        fun newInstance(artistId: Int): SimilarArtistsFragment {
            val args = Bundle()
            args.putInt(Constants.Extra.ARTIST_ID, artistId)
            val fragment = SimilarArtistsFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
