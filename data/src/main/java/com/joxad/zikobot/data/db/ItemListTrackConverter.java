package com.joxad.zikobot.data.db;

import android.os.Parcel;

import com.joxad.zikobot.data.model.Track;

import org.parceler.Parcels;
import org.parceler.converter.ArrayListParcelConverter;

public class ItemListTrackConverter extends ArrayListParcelConverter<Track> {
    @Override
    public void itemToParcel(Track item, Parcel parcel) {
        parcel.writeParcelable(Parcels.wrap(item), 0);
    }

    @Override
    public Track itemFromParcel(Parcel parcel) {
        return Parcels.unwrap(parcel.readParcelable(Track.class.getClassLoader()));
    }
}