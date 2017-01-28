package com.filemanager.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.filemanager.R;
import com.filemanager.adapter.VideoAdapter;
import com.filemanager.util.FileUtil;

import java.io.File;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private List<File> mFiles;
    private VideoAdapter mAdapter;

    public VideoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View ret = inflater.inflate(R.layout.fragment_video, container, false);
        mFiles = FileUtil.getSpecificTypeOfFile(getContext(), new String[]{".mp4"});
        mRecyclerView = (RecyclerView) ret.findViewById(R.id.id_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter = new VideoAdapter(getContext(), mFiles));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter.setOnItemClickLitener(new VideoAdapter.OnItemClickLitener() {

            @Override
            public void onItemClick(View view, int position) {
                String path = mFiles.get(position).getPath();
                Intent intent = FileUtil.openFile(path);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }


        });

        return ret;
    }
}