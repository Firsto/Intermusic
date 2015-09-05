package ru.firsto.intermusic;

import android.os.Parcel;

import com.vk.sdk.api.model.VKApiAudio;

import org.json.JSONObject;

/**
 * Created by razor on 02.09.15.
 */
public class Song extends VKApiAudio {

    public int position = 0;
    public boolean downloaded = false;
    public String path = "";

    public Song() {
        super();
    }

    public Song(Parcel in) {
        super(in);
        this.position = (in.readInt());
        this.downloaded = (in.readInt() == 1);
        this.path = in.readString();
    }

    public Song(JSONObject from) {
        super(from);
    }

    public Song(VKApiAudio audio) {
        this(getParcelFromVKApiAudio(audio));
    }

    private static Parcel getParcelFromVKApiAudio(VKApiAudio audio) {
        Parcel parcel = Parcel.obtain();
        audio.writeToParcel(parcel, 0);
        parcel.writeInt(0);
        parcel.writeInt(0);
        parcel.writeString("");
        parcel.setDataPosition(0);
        return parcel;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(position);
        dest.writeInt(this.downloaded ? 1 : 0);
        dest.writeString(this.path);
    }

    public static Creator<Song> CREATOR = new Creator<Song>() {
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}
