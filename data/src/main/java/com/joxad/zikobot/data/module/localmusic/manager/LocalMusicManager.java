package com.joxad.zikobot.data.module.localmusic.manager;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.joxad.zikobot.data.db.model.ZikoAlbum;
import com.joxad.zikobot.data.db.model.ZikoAlbum_Table;
import com.joxad.zikobot.data.db.model.ZikoArtist;
import com.joxad.zikobot.data.db.model.ZikoArtist_Table;
import com.joxad.zikobot.data.db.model.ZikoTrack;
import com.joxad.zikobot.data.db.model.ZikoTrack_Table;
import com.joxad.zikobot.data.module.localmusic.model.LocalTrack;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;


/**
 * Created by Josh on 06/04/2016.
 */

public class LocalMusicManager {

    private Context context;

    private PublishSubject<Boolean> synchroDone;
    private ContentResolver cr;

    public LocalMusicManager(Context context) {
        this.context = context;
        synchroDone = PublishSubject.create();
        cr = context.getContentResolver();
    }

    public Observable<Boolean> observeSynchro() {
        return Observable.fromCallable(() -> {
            try {
                syncLocalData();
            } catch (Exception e) {
                Log.d(LocalMusicManager.class.getSimpleName(), e.getLocalizedMessage());
            }
            synchroDone.onNext(true);
            return true;
        });
    }


    public void syncLocalData() {
        try {
            for (LocalTrack localTrack : TrackLoader.getAllTracks(context)) {
                ZikoArtist zikoArtist = createArtistIfNeed(localTrack.getArtistId(), localTrack.getArtistName());
                ZikoAlbum zikoAlbum = createAlbumIfNeed(localTrack.getAlbumId(), localTrack.getAlbumName(), zikoArtist);
                createTrack(localTrack, zikoArtist, zikoAlbum);
            }
        } catch (Exception e) {
            Log.e(LocalMusicManager.class.getSimpleName(), e.getLocalizedMessage());
        }
    }


    private ZikoArtist createArtistIfNeed(long artistId, String artistName) {
        ZikoArtist zikoArtist = new Select().from(ZikoArtist.class).where(ZikoArtist_Table.localId.eq(artistId)).querySingle();
        if (zikoArtist == null) {
            zikoArtist = ZikoArtist.Companion.local(artistId, artistName);
            zikoArtist.setFavorite(true);
            zikoArtist.save();
        }
        return zikoArtist;
    }

    final public static Uri sArtworkUri = Uri
            .parse("content://media/external/audio/albumart");

    private ZikoAlbum createAlbumIfNeed(long albumId, String albumName, ZikoArtist zikoArtist) {
        ZikoAlbum zikoAlbum = new Select().from(ZikoAlbum.class)
                .where(ZikoAlbum_Table.localId.eq(albumId))
                .or(ZikoAlbum_Table.name.like(albumName))
                .querySingle();
        if (zikoAlbum == null) {
            zikoAlbum = ZikoAlbum.Companion.local(albumId, albumName, zikoArtist);
            Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
            if (isUriBitmapValid(albumArtUri))
                zikoAlbum.setImage(albumArtUri.toString());
            zikoAlbum.setFavorite(true);
            zikoAlbum.save();
        }
        return zikoAlbum;
    }


    private boolean isUriBitmapValid(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cur = cr.query(uri, projection, null, null, null);
        boolean b = false;
        if (cur != null) {
            cur.moveToFirst();
            try {

                String filePath = cur.getString(0);
                if (filePath == null || filePath.isEmpty()) {
                    b = false;
                } else b = (new File(filePath)).exists();
            } catch (CursorIndexOutOfBoundsException e) {
                b = false;
            }


        } else {
            b = false;
        }
        return b;
    }

    private ZikoTrack createTrack(LocalTrack localTrack, ZikoArtist artist, ZikoAlbum album) {
        ZikoTrack zikoTrack = new Select().from(ZikoTrack.class).where(ZikoTrack_Table.localId.eq(localTrack.getId())).querySingle();
        if (zikoTrack == null) {
            zikoTrack = ZikoTrack.local(localTrack, artist, album);
            zikoTrack.save();
        }
        return zikoTrack;
    }

    public Single<Boolean> synchroDoneObservable() {
        return synchroDone.lastOrError();
    }
}
