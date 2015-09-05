package ru.firsto.intermusic;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.util.VKUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AudioListFragment.IAudioList {

    private static final String PREF_FIRST_START = "first_start";
    private static final String PREF_TOKEN = "token";

    private DBHelper mHelper;
    private SharedPreferences mPrefs;

    VKAccessToken mToken;
    List<Song> mAudioList = new ArrayList<>();

    Button mButtonAuth, mButtonLoadList;

    FragmentManager fm;
    Fragment listFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());
        Log.d("TAG", fingerprints[0]);

        mHelper = new DBHelper(getApplicationContext());
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstStart = mPrefs.getBoolean(PREF_FIRST_START, true);

        fm = getSupportFragmentManager();
        listFragment = fm.findFragmentByTag(AudioListFragment.TAG);

        if ((mToken = VKAccessToken.tokenFromSharedPreferences(getApplicationContext(), PREF_TOKEN)) == null || mToken.isExpired()) {
            login();
        } else {
            loadData();
        }

        mButtonAuth = (Button) findViewById(R.id.btnAuth);
        mButtonAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        mButtonLoadList = (Button) findViewById(R.id.btnLoadList);
        mButtonLoadList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });

        Button removesongs = (Button) findViewById(R.id.removeSongs);
        removesongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cnt = 0;
                for (Song song : mAudioList) {
                    if (song.downloaded) {
                        File file = new File(song.path);
                        if (file.delete()) cnt++;
                        Log.d("TAG", "deleted: " + song.path);
                        mHelper.updateSongPath(song.id, "");
                        mAudioList.get(mAudioList.indexOf(song)).path = "";
                        mAudioList.get(mAudioList.indexOf(song)).downloaded = false;
                    }
                }
                Log.d("TAG", "deleted " + cnt + " songs");
            }
        });

        registerReceiver(completeReceiver, new IntentFilter(DownloadService.ACTION));
        registerReceiver(stopReceiver, new IntentFilter(DownloadReceiver.ACTION));
    }

    @Override
    protected void onStop() {
        unregisterReceiver(completeReceiver);
        unregisterReceiver(stopReceiver);
        super.onStop();
    }

    private void loadData() {
//        Log.d("TAG", "userId " + mToken.userId);
        VKRequest request = VKApi.audio().get(VKParameters.from(VKApiConst.OWNER_ID, "33845146", VKApiConst.COUNT, "30"));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
//                Log.d("TAG", response.responseString);
//                VKApiAudio audio = new VKApiAudio(response.json);
                try {
                    parseJSON(response.json.getJSONObject("response"));
                    Log.d("TAG", "list size: " + mAudioList.size());
                    loadList();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
            }

            @Override
            public void onError(VKError error) {
                Log.d("TAG", "error: " + error.errorMessage);
                super.onError(error);
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                super.onProgress(progressType, bytesLoaded, bytesTotal);
            }
        });
    }

    private void loadList() {
        Log.d("TAG", "initFragment()");
        if (listFragment != null) {
            fm.beginTransaction().remove(listFragment).commit();
            listFragment = null;
        }
        if (listFragment == null) {
            listFragment = new AudioListFragment();
            fm.beginTransaction()
                    .add(R.id.container, listFragment, AudioListFragment.TAG)
                    .commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // Пользователь успешно авторизовался
                Log.d("TAG", "auth success");
                mToken = res;
                Log.d("TAG", mToken.accessToken + "\n" + mToken.isExpired());
                ((TextView) findViewById(R.id.textView)).setText(mToken.accessToken);
                mToken.saveTokenToSharedPreferences(getApplicationContext(), PREF_TOKEN);
                loadData();
            }

            @Override
            public void onError(VKError error) {
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                Log.d("TAG", "auth failed");
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void login() {
        VKSdk.login(this, VKScope.AUDIO);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_save) {
            Log.d("menu", "save");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void parseJSON(JSONObject response) throws JSONException {
        int count = response.optInt("count", 0);
        Log.d("TAG", "count: " + count);
        if (count == 0) {
            return;
        } else {
//            if (count > 10) count = 10;
            JSONArray items = response.optJSONArray("items");
            Song song;
            DBHelper.SongCursor cursor;
            for (int i = 0; i < items.length(); i++) {
                Song apisong = new Song(items.getJSONObject(i));
                cursor = mHelper.querySong(apisong.id);
                cursor.moveToFirst();
                song = cursor.getSong();
                if (song == null || song.id == 0) {
                    song = apisong;
                    mHelper.insertSong(song);
                } else {
                    File file = new File(song.path);
                    if (file.exists()) {
                        apisong.path = song.path;
                        apisong.downloaded = true;
                    }
                    song = apisong;
                    mHelper.updateSong(song);
//                    mHelper.updateSongPath(song.id, path);
//                    song = cursor.getSong();
                    Log.d("TAG", "apisong path " + song.path + " // downloaded " + song.downloaded);
                }
                song.position = i;
                mHelper.updateSongPosition(song.id, song.position);
//                mAudioList.add(new VKApiAudio(items.getJSONObject(i)));
                mAudioList.add(song);
            }
        }
    }

    @Override
    public List<Song> getList() {
        return mAudioList;
    }

    private static final int NOTIFY_ID = 1;
    private BroadcastReceiver completeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int id = intent.getIntExtra("id", 0);
            String path = intent.getStringExtra("path");
            boolean needNotify = intent.getBooleanExtra("needNotify", false);
            Log.d("TAG", "download complete " + id + " // path " + path);

            mHelper.updateSongPath(id, path);
            DBHelper.SongCursor cursor = mHelper.querySong(id);
            cursor.moveToFirst();
            Song song = cursor.getSong();
            mAudioList.get(song.position).downloaded = song.downloaded;
            mAudioList.get(song.position).path = song.path;

            if (needNotify) {
                Intent notificationIntent = new Intent(context, MainActivity.class);

                PendingIntent contentIntent = PendingIntent.getActivity(context,
                        0, notificationIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);

                Resources res = context.getResources();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

                builder.setContentIntent(contentIntent)
                        .setSmallIcon(android.R.drawable.ic_menu_save)
                                // большая картинка
                        .setLargeIcon(BitmapFactory.decodeResource(res, android.R.drawable.ic_menu_save))
                                //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                        .setTicker("Скачивание завершено!")
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                                //.setContentTitle(res.getString(R.string.notifytitle)) // Заголовок уведомления
                        .setContentTitle("Скачивание завершено")
                                //.setContentText(res.getString(R.string.notifytext))
                        .setContentText("Скачивание завершено"); // Текст уведомления

                // Notification notification = builder.getNotification(); // до API 16
                Notification notification = builder.build();

                NotificationManager notificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(NOTIFY_ID, notification);
            }
        }
    };

    private BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int notificationId = intent.getIntExtra("notificationId", 0);

            Log.d("TAG", "Button Receiver onReceive");
            DownloadService.interrupted = true;

            // cancel notification
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(notificationId);
        }
    };
}
