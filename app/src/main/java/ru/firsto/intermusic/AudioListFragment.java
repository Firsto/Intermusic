package ru.firsto.intermusic;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.vk.sdk.api.model.VKApiAudio;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by razor on 14.08.15.
 */
public class AudioListFragment extends Fragment {

    ListView mListAudio;
    List<VKApiAudio> mAudioList;

    public static final String TAG = "audiolist";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof IAudioList) {
            mAudioList = ((IAudioList) getActivity()).getList();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        mListAudio = (ListView) view.findViewById(R.id.listAudio);
//        ArrayAdapter adapter = new ArrayAdapter<String>();
        ArrayList<String> songs = new ArrayList<>();
        for (VKApiAudio audio : mAudioList) {
            songs.add(audio.title);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, songs);
        mListAudio.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        mListAudio.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "song: " + mAudioList.get(position).title + "\nurl: " + mAudioList.get(position).url, Toast.LENGTH_SHORT).show();

            }
        });

        return view;
    }

    public interface IAudioList {
        public List<VKApiAudio> getList();
    }

    public class AudioListAdapter extends ArrayAdapter<String> {

        public AudioListAdapter(Context context, int resource) {
            super(context, resource);
        }
    }
}
