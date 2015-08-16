package ru.firsto.intermusic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.vk.sdk.api.model.VKApiAudio;

import java.util.List;

/**
 * Created by razor on 14.08.15.
 */
public class AudioListFragment extends Fragment {

    ListView mListAudio;
    List<VKApiAudio> mAudioList;
    AudioPlayer mAudioPlayer;

    public static final String TAG = "audiolist";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof IAudioList) {
            mAudioList = ((IAudioList) getActivity()).getList();
        }

        mAudioPlayer = new AudioPlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        mListAudio = (ListView) view.findViewById(R.id.listAudio);

        AudioListAdapter adapter = new AudioListAdapter(getActivity(), mAudioList, mAudioPlayer);
        mListAudio.setAdapter(adapter);

        return view;
    }

    private void releasePlayer() {
        if (mAudioPlayer.isExist()) {
            try {
                mAudioPlayer.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface IAudioList {
        List<VKApiAudio> getList();
    }
}
