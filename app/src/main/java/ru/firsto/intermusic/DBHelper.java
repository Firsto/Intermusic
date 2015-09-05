package ru.firsto.intermusic;

/**
 * Created by razor on 01.09.15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Parcel;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    public static final String TAG = "DatabaseHelper";

    private static final String DB_NAME = "songs.sqlite";
    private static final int VERSION = 1;

    private static final String TABLE_SONGS = "songs";
    private static final String COLUMN_SONG_ID          = "_id";
    private static final String COLUMN_SONG_OWNER_ID    = "owner_id";
    private static final String COLUMN_SONG_ARTIST      = "artist";
    private static final String COLUMN_SONG_TITLE       = "title";
    private static final String COLUMN_SONG_DURATION    = "duration";
    private static final String COLUMN_SONG_URL         = "url";
    private static final String COLUMN_SONG_LYRICS_ID   = "lyrics_id";
    private static final String COLUMN_SONG_ALBUM_ID    = "album_id";
    private static final String COLUMN_SONG_GENRE       = "genre_id";
    private static final String COLUMN_SONG_ACCESS_KEY  = "access_key";

    private static final String COLUMN_SONG_POSITION  = "position";
    private static final String COLUMN_SONG_DOWNLOADED  = "downloaded";
    private static final String COLUMN_SONG_PATH  = "path";


    public DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создание таблицы "songs"
        db.execSQL("CREATE TABLE " + TABLE_SONGS + " (" +
//                        "_id integer primary key autoincrement," +
                        COLUMN_SONG_ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_SONG_OWNER_ID + " INTEGER, " +
                        COLUMN_SONG_ARTIST + " VARCHAR(127), " +
                        COLUMN_SONG_TITLE + " VARCHAR(127), " +
                        COLUMN_SONG_DURATION + " INTEGER, " +
                        COLUMN_SONG_URL + " VARCHAR(512), " +
                        COLUMN_SONG_LYRICS_ID + " INTEGER, " +
                        COLUMN_SONG_ALBUM_ID + " INTEGER, " +
                        COLUMN_SONG_GENRE + " INTEGER, " +
                        COLUMN_SONG_ACCESS_KEY + " VARCHAR(255), " +
                        COLUMN_SONG_POSITION + " INTEGER, " +
                        COLUMN_SONG_DOWNLOADED + " TINYINT NOT NULL DEFAULT 0, " +
                        COLUMN_SONG_PATH + " VARCHAR(255) NOT NULL DEFAULT '' " +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Здесь реализуются изменения схемы и преобразования данных
        // при обновлении схемы
    }

    public void clearDatabase() {
        Log.d("TAG", "clearDatabase()");
        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(getWritableDatabase().getPath(), null, 0);
            db.delete(TABLE_SONGS, null, null);
            getWritableDatabase().beginTransaction();
            getWritableDatabase().delete(TABLE_SONGS, null, null);
            getWritableDatabase().endTransaction();
            Log.d("TAG", "cleared >>>");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ContentValues getCV(Song song){
        ContentValues cv = new ContentValues();
//        cv.put(COLUMN_SONG_ID, song.id);
        cv.put(COLUMN_SONG_OWNER_ID  , song.owner_id);
        cv.put(COLUMN_SONG_ARTIST    , song.artist);
        cv.put(COLUMN_SONG_TITLE     , song.title);
        cv.put(COLUMN_SONG_DURATION  , song.duration);
        cv.put(COLUMN_SONG_URL       , song.url);
        cv.put(COLUMN_SONG_LYRICS_ID , song.lyrics_id);
        cv.put(COLUMN_SONG_ALBUM_ID  , song.album_id);
        cv.put(COLUMN_SONG_GENRE     , song.genre);
        cv.put(COLUMN_SONG_ACCESS_KEY, song.access_key);

        cv.put(COLUMN_SONG_POSITION, song.position);
        cv.put(COLUMN_SONG_DOWNLOADED, song.downloaded);
        cv.put(COLUMN_SONG_PATH, song.path);

        return cv;
    };

    public long insertSong(Song song) {
        ContentValues contentValues = getCV(song);
        contentValues.put(COLUMN_SONG_ID, song.id);

        return getWritableDatabase().insert(TABLE_SONGS, null, contentValues);
    }

    public long updateSong(Song song) {
        return getWritableDatabase().update(TABLE_SONGS, getCV(song), COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(song.id)});
    }

    public long updateSongPath(int id, String path) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_SONG_DOWNLOADED, "".equals(path) ? 0 : 1);
        cv.put(COLUMN_SONG_PATH, path);

        return getWritableDatabase().update(TABLE_SONGS, cv, COLUMN_SONG_ID + " = ?", new String[]{ String.valueOf(id) } );
    }

    public long updateSongPosition(int id, int position) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_SONG_POSITION, position);

        return getWritableDatabase().update(TABLE_SONGS, cv, COLUMN_SONG_ID + " = ?", new String[]{ String.valueOf(id) } );
    }

    public SongCursor querySongs() {
        // Эквивалент "select * from songs order by _id asc"
        Cursor wrapped = getReadableDatabase().query(TABLE_SONGS,
                null, null, null, null, null, COLUMN_SONG_ID + " asc");
        return new SongCursor(wrapped);
    }

    public SongCursor queryAlbumSongs(int albumId) {
        // Эквивалент "select * from songs where album_id = albumId order by _id asc"
        Cursor wrapped = getReadableDatabase().query(TABLE_SONGS,
                null,
                COLUMN_SONG_ALBUM_ID + " = ?",
                new String[]{ String.valueOf(albumId)},
                null,
                null,
                COLUMN_SONG_ID + " asc");
        return new SongCursor(wrapped);
    }

    public SongCursor querySong(int id) {
        Cursor wrapped = getReadableDatabase().query(TABLE_SONGS,
                null, // Все столбцы
                COLUMN_SONG_ID + " = ?", // Поиск по идентификатору песни
                new String[]{ String.valueOf(id) }, // С этим значением
                null, // group by
                null, // order by
                null, // having
                "1"); // 1 строка
        return new SongCursor(wrapped);
    }

    /**
     * Вспомогательный класс с курсором, возвращающим строки таблицы "songs".
     * Метод {getSong()} возвращает экземпляр Song, представляющий
     * текущую строку.
     */

    public static class SongCursor extends CursorWrapper {
        public SongCursor(Cursor c) {
            super(c);
        }

        /**
         * Возвращает объект Song, представляющий текущую строку,
         * или null, если текущая строка недействительна.
         */
        public Song getSong() {
            if (isBeforeFirst() || isAfterLast())
                return null;

            Parcel parcel = Parcel.obtain();
            parcel.writeInt(getInt(getColumnIndex(COLUMN_SONG_ID)));
            parcel.writeInt(getInt(getColumnIndex(COLUMN_SONG_OWNER_ID)));
            parcel.writeString(getString(getColumnIndex(COLUMN_SONG_ARTIST)));
            parcel.writeString(getString(getColumnIndex(COLUMN_SONG_TITLE)));
            parcel.writeInt(getInt(getColumnIndex(COLUMN_SONG_DURATION)));
            parcel.writeString(getString(getColumnIndex(COLUMN_SONG_URL)));
            parcel.writeInt(getInt(getColumnIndex(COLUMN_SONG_LYRICS_ID)));
            parcel.writeInt(getInt(getColumnIndex(COLUMN_SONG_ALBUM_ID)));
            parcel.writeInt(getInt(getColumnIndex(COLUMN_SONG_GENRE)));
            parcel.writeString(getString(getColumnIndex(COLUMN_SONG_ACCESS_KEY)));

            parcel.writeInt(getInt(getColumnIndex(COLUMN_SONG_POSITION)));
            parcel.writeInt(getInt(getColumnIndex(COLUMN_SONG_DOWNLOADED)));
            parcel.writeString(getString(getColumnIndex(COLUMN_SONG_PATH)));

            parcel.setDataPosition(0);

            return Song.CREATOR.createFromParcel(parcel);
        }
    }
}
