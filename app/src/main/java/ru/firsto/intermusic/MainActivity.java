package ru.firsto.intermusic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
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
import com.vk.sdk.api.model.VKApiAudio;
import com.vk.sdk.util.VKUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AudioListFragment.IAudioList {

    private static final String PREF_FIRST_START = "first_start";
    private static final String PREF_TOKEN = "token";

    private SharedPreferences mPrefs;

    VKAccessToken mToken;
    List<VKApiAudio> mAudioList = new ArrayList<>();

    Button mButtonAuth, mButtonLoadList;

    FragmentManager fm;
    Fragment listFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());
        Log.d("TAG", fingerprints[0]);

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


    }

    private void loadData() {
//        Log.d("TAG", "userId " + mToken.userId);
        VKRequest request = VKApi.audio().get(VKParameters.from(VKApiConst.OWNER_ID, "33845146", VKApiConst.COUNT, "5"));
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
            for (int i = 0; i < items.length(); i++) {
                mAudioList.add(new VKApiAudio(items.getJSONObject(i)));
            }
        }
    }

    @Override
    public List<VKApiAudio> getList() {
        return mAudioList;
    }
}
