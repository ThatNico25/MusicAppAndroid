package com.example.musicappandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;

import com.example.musicappandroid.databinding.FragmentFirstBinding;

import java.util.ArrayList;

public class MusicListFragment extends Fragment {

    private FragmentFirstBinding binding;
    private MusicPlayerFragment fragment;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView musicsVisual = null;
        if( getView() != null) {
            musicsVisual = getView().findViewById(R.id.listMusic);
        }

        if(musicsVisual != null) {
            musicsVisual.setOnItemClickListener((parent, view1, position, id) -> {

                Bundle bundle = new Bundle();

                String info = parent.getItemAtPosition(position).toString();
                bundle.putString("music_info", info);
                bundle.putInt("posMusic", position);

                ArrayList<String> lv_arr = new ArrayList<>();
                for(int i = 0; i < parent.getCount(); i++) {
                    lv_arr.add(parent.getItemAtPosition(i).toString());
                }

                bundle.putStringArrayList("listArr", lv_arr);

                fragment = new MusicPlayerFragment(false);
                fragment.setArguments(bundle);

                FragmentManager t;
                if (getActivity() != null) {
                    t = getActivity().getSupportFragmentManager();
                    t.beginTransaction()
                            .add(R.id.listMusicPage, fragment)
                            .commit();
                    fragment.onDestroyView();
                    fragment = null;

                    t.getFragments();
                }

                NavHostFragment.findNavController(MusicListFragment.this).navigate(R.id.action_FirstFragment_to_SecondFragment);
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}