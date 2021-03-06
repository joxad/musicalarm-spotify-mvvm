package com.joxad.zikobot.app.youtube;

import android.content.Context;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.joxad.easydatabinding.fragment.v4.FragmentBaseVM;
import com.joxad.zikobot.app.BR;
import com.joxad.zikobot.app.R;
import com.joxad.zikobot.app.databinding.FragmentYoutubeSearchBinding;
import com.joxad.zikobot.app.localtracks.TrackVM;
import com.joxad.zikobot.app.player.event.EventPlayTrack;
import com.joxad.zikobot.app.search.SearchManager;
import com.joxad.zikobot.app.youtube.download.EventSelectItemYtDownload;
import com.joxad.zikobot.app.youtube.download.FragmentDownload;
import com.joxad.zikobot.app.youtube.download.FragmentDownloadVM;
import com.joxad.zikobot.data.event.search.EventQueryChange;
import com.joxad.zikobot.data.model.Track;
import com.joxad.zikobot.data.module.youtube.VideoItem;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.tatarka.bindingcollectionadapter2.ItemBinding;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Generated by generator-android-template
 */
public class FragmentYoutubeSearchVM extends FragmentBaseVM<FragmentYoutubeSearch, FragmentYoutubeSearchBinding> {

    public ItemBinding itemViewYoutube = ItemBinding.of(BR.youtubeItemVM, R.layout.item_youtube);
    public ObservableArrayList<YoutubeItemVM> youtubeItemVMs;
    private Subscription youtubeSubscription;
    private YoutubeConnector yc;
    public ObservableBoolean loading;

    /***
     * @param fragment
     * @param binding
     */
    public FragmentYoutubeSearchVM(FragmentYoutubeSearch fragment, FragmentYoutubeSearchBinding binding,@Nullable Bundle savedInstanceState) {
        super(fragment, binding,savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        youtubeItemVMs = new ObservableArrayList<>();
        yc = new YoutubeConnector(fragment.getContext());
        loading = new ObservableBoolean(false);
    }


    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        query(SearchManager.QUERY);
    }

    @Subscribe
    public void onReceive(EventSelectItemYtDownload eventSelectItemYt) {
        VideoItem model = eventSelectItemYt.getVideoItem();
        if (model == null) {
            loading.set(false);
        } else {
            yc.detailObservable(model.getId()).subscribe(videoItem -> {
                loading.set(false);
                videoItem.setRef(model.getRef());
                FragmentDownload.newInstance(videoItem).show(fragment.getFragmentManager(), FragmentDownloadVM.TAG);
            }, throwable -> {
                loading.set(false);

                Toast.makeText(fragment.getContext(), throwable.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            });
        }
        //we call the detail in order to get the duration of the item
    }

    @Subscribe
    public void onReceive(EventSelectItemYt eventSelectItemYt) {
        //we call the detail in order to get the duration of the item
        loading.set(true);
    }

    @Subscribe
    public void onReceive(EventSelectItemYtLoaded eventSelectItemYtLoaded) {
        VideoItem model = eventSelectItemYtLoaded.getModel();
        if (model == null) {
            loading.set(false);
        } else {
            yc.detailObservable(model.getId()).subscribe(videoItem -> {
                loading.set(false);
                videoItem.setRef(model.getRef());
                EventBus.getDefault().post(new EventPlayTrack(new TrackVM(fragment.getContext(), Track.from(videoItem))));
            }, throwable -> {
                loading.set(false);

                Toast.makeText(fragment.getContext(), throwable.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(EventQueryChange event) {
        query(event.getQuery());
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public void query(final String query) {

        if (query.length() < 2) {
            youtubeItemVMs.clear();
            notifyPropertyChanged(BR.showNoResult);
            return;
        }
        youtubeItemVMs.clear();

        youtubeSubscription = yc.queryObservable(query).subscribe(videoItems -> {
            for (VideoItem videoItem : videoItems) {
                youtubeItemVMs.add(new YoutubeItemVM(fragment.getContext(), videoItem));
            }
            notifyPropertyChanged(BR.showNoResult);


        }, throwable -> {
            Logger.e(throwable.getMessage());
            notifyPropertyChanged(BR.showNoResult);

        });
    }

    class YoutubeConnector {
        private YouTube youtube;
        private YouTube.Search.List query;
        private YouTube.Videos.List videos;
        // Your developer key goes here


        public YoutubeConnector(Context context) {
            youtube = new YouTube.Builder(new NetHttpTransport(),
                    new JacksonFactory(), hr -> {
            }).setApplicationName(context.getString(R.string.app_name)).build();

            try {
                query = youtube.search().list("id,snippet");
                query.setKey(context.getString(R.string.youtube_key));
                query.setMaxResults(50L);
                query.setType("video");
                query.setFields("items(id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url)");
                videos = youtube.videos().list("id,contentDetails,snippet,statistics");
                videos.setKey(context.getString(R.string.youtube_key));
                videos.setMaxResults((long) 1);
                videos.setFields("items(id,contentDetails,snippet,statistics)");

            } catch (IOException e) {
                Log.d("YC", "Could not initialize: " + e);
            }
        }

        public Observable<VideoItem> detailObservable(String id) {
            return Observable.fromCallable(() -> getDetail(id))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io());
        }

        public Observable<List<VideoItem>> queryObservable(String keywords) {
            return Observable.fromCallable(() -> query(keywords))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io());
        }


        public VideoItem getDetail(String id) {

            videos.setId(id);
            try {
                VideoListResponse response = videos.execute();
                List<Video> results = response.getItems();

                List<VideoItem> items = new ArrayList<VideoItem>();
                for (Video result : results) {
                    VideoItem item = new VideoItem();
                    item.setTitle(result.getSnippet().getTitle());
                    item.setDescription(result.getSnippet().getDescription());
                    item.setThumbnailURL(result.getSnippet().getThumbnails().getHigh().getUrl());
                    item.setId(id);
                    item.setDuration(durationFrom(result.getContentDetails().getDuration()));
                    items.add(item);
                }
                return items.get(0);
            } catch (IOException e) {
                Log.d("YC", "Could not search: " + e);
                return null;
            }


        }

        private long durationFrom(String ytDuration) {
            String time = ytDuration.substring(2);
            long duration = 0L;
            Object[][] indexs = new Object[][]{{"H", 3600}, {"M", 60}, {"S", 1}};
            for (int i = 0; i < indexs.length; i++) {
                int index = time.indexOf((String) indexs[i][0]);
                if (index != -1) {
                    String value = time.substring(0, index);
                    duration += Integer.parseInt(value) * (int) indexs[i][1] * 1000;
                    time = time.substring(value.length() + 1);
                }
            }
            return duration;

        }

        public List<VideoItem> query(String keywords) {

            query.setQ(keywords);
            try {
                SearchListResponse response = query.execute();
                List<SearchResult> results = response.getItems();

                List<VideoItem> items = new ArrayList<VideoItem>();
                for (SearchResult result : results) {
                    VideoItem item = new VideoItem();
                    item.setTitle(result.getSnippet().getTitle());
                    item.setDescription(result.getSnippet().getDescription());
                    item.setThumbnailURL(result.getSnippet().getThumbnails().getDefault().getUrl());
                    item.setId(result.getId().getVideoId());
                    items.add(item);
                }
                return items;
            } catch (IOException e) {
                Log.d("YC", "Could not search: " + e);
                return null;
            }


        }
    }

    @Bindable
    public boolean getShowNoResult() {
        return SearchManager.QUERY.length() > 2 && youtubeItemVMs.isEmpty();
    }
}

