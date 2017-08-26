package com.startogamu.zikobot.home.playlists

import android.databinding.Bindable
import android.databinding.ObservableArrayList
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import com.joxad.androidtemplate.core.view.utils.EndlessRecyclerOnScrollListener
import com.joxad.zikobot.data.db.AlarmManager
import com.joxad.zikobot.data.db.CurrentPlaylistManager
import com.joxad.zikobot.data.db.PlaylistManager
import com.joxad.zikobot.data.db.model.ZikoPlaylist
import com.joxad.zikobot.data.db.model.ZikoTrack
import com.raizlabs.android.dbflow.rx2.kotlinextensions.list
import com.startogamu.zikobot.ABasePlayerActivityVM
import com.startogamu.zikobot.BR
import com.startogamu.zikobot.Constants
import com.startogamu.zikobot.R
import com.startogamu.zikobot.core.AppUtils
import com.startogamu.zikobot.databinding.PlayerViewBottomBinding
import com.startogamu.zikobot.databinding.PlaylistDetailActivityBinding
import com.startogamu.zikobot.home.track.TrackVM
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.tatarka.bindingcollectionadapter2.ItemBinding


/**
 * Generated by generator-android-template
 */
class PlaylistDetailActivityVM(activity: PlaylistDetailActivity, binding: PlaylistDetailActivityBinding, savedInstance: Bundle?) : ABasePlayerActivityVM<PlaylistDetailActivity, PlaylistDetailActivityBinding>(activity, binding, savedInstance) {

    override fun playAll(view: View?) {
        val list: List<ZikoTrack> = items.map { it.model }
        CurrentPlaylistManager.INSTANCE.play(list)
    }

    fun prepareAlarm(@SuppressWarnings("unused") view: View?) {
        playlistVM.prepareAlarm(activity)
    }

    fun editAlarm(@SuppressWarnings("unused") view: View?) {
        playlistVM.showEditAlarm(activity)
    }

    override fun playerBinding(): PlayerViewBottomBinding {
        return binding.viewPlayer!!
    }

    lateinit var items: ObservableArrayList<TrackVM>
    val itemBinding: ItemBinding<TrackVM> = ItemBinding.of<TrackVM>(BR.trackVM, R.layout.track_item)
    lateinit var playlistVM: PlaylistVM

    override fun onCreate(saved: Bundle?) {
        super.onCreate(saved)
        items = ObservableArrayList()
        playlistVM = PlaylistVM(false, activity, ZikoPlaylist())
        AppUtils.initToolbar(activity, binding.toolbarDetailActivity!!)
        loadData()
        AlarmManager.INSTANCE.refreshAlarm
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    notifyPropertyChanged(BR.alarmEditTitle)
                })
        binding.playlistDetailTracksRV.addOnScrollListener(object : EndlessRecyclerOnScrollListener() {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                addTracks(page)
            }
        })
        binding.toolbarDetailActivity?.appBarLayout?.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
                val alpha: Float = Math.abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange
                binding.playlistDetailAlarmTitle.alpha = 1 - alpha
        }

    }

    /***
     * Load the data spotify WS or DB to show them in the recycler
     */
    private fun loadData() {
        val pId = activity.intent.getLongExtra(Constants.Extra.PLAYLIST_ID, 0)
        PlaylistManager.INSTANCE.findOne(pId)
                .querySingle()
                .subscribe({ zi ->
                    playlistVM = PlaylistVM(false, activity, zi)
                    binding.toolbarDetailActivity!!.toolbar.setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.menu_delete -> {
                                PlaylistManager.INSTANCE.delete(zi)
                                activity.finish()
                                PlaylistManager.INSTANCE.refreshSubject.onNext(true)
                            }
                        }
                        true
                    }
                    AppUtils.initFab(binding.fabPlay)
                    AppUtils.initFab(binding.fabAlarm)

                })

        addTracks(0)
    }

    /***
     * Add the tracks of the playlist
     */
    private fun addTracks(i: Int) {
        PlaylistManager.INSTANCE.findTracks(playlistVM.model.id, i).list {
            for (track in it) {
                items.add(TrackVM(activity, track))
            }
        }
    }

    /***
     * Handle the back press
     * if player is  up, it will go bottom
     */
    override fun onBackPressed(): Boolean {
        if (playerVM.onBackPressed()) {
            binding.fabPlay.visibility = View.GONE
            binding.fabAlarm.visibility = View.GONE
            return true
        }
        return false
    }


    @Bindable
    fun getAlarmEditTitle(): String? {
        return playlistVM.getAlarmTitle()
    }

    companion object {

        private val TAG = PlaylistDetailActivityVM::class.java.simpleName
    }


}
