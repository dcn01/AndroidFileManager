package com.filemanager.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.utils.FileUtils;
import com.bumptech.glide.Glide;
import com.filemanager.R;
import com.filemanager.adapter.ImageAdapter;
import com.filemanager.util.ACache;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ImageFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView mRecyclerView;
    private List<File> mFiles;
    private ImageAdapter mAdapter;
    private SwipeRefreshLayout mRefreshLayout;
    private Gson mGson;
    private ImageView mLoading;
    private TextView mLoadingText;
    private ACache mCatch;

    public ImageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View ret = inflater.inflate(R.layout.fragment_image, container, false);
        mLoading = (ImageView) ret.findViewById(R.id.loading_gif);
        mLoadingText = (TextView) ret.findViewById(R.id.loading_text);
        Glide.with(getContext()).load(R.drawable.loading)
                .asGif().into(mLoading);
        mFiles = new ArrayList<>();
        mGson = new Gson();
        mCatch = ACache.get(getContext());


        mRecyclerView = (RecyclerView) ret.findViewById(R.id.id_recyclerview);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));

        mRefreshLayout = (SwipeRefreshLayout) ret.findViewById(R.id.image_refresh);
        mRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mRefreshLayout.setOnRefreshListener(this);
        initData();

        return ret;
    }

    private void initData() {

        //开线程初始化数据
        new Thread(new Runnable() {
            @Override
            public void run() {

                judge();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.setAdapter(mAdapter = new ImageAdapter(getContext(), mFiles));
                        mLoading.setVisibility(View.INVISIBLE);
                        mLoadingText.setVisibility(View.INVISIBLE);
                        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                        mAdapter.setOnItemClickLitener(new ImageAdapter.OnItemClickLitener() {
                            @Override
                            public void onItemClick(View view, int position) {
                            }

                            @Override
                            public void onItemLongClick(View view, int position) {
                            }
                        });
                    }
                });
            }
        }).start();
    }

    /**
     * 判断缓存是否存在，初始化数据
     */
    private void judge() {
        SharedPreferences table = getContext().getSharedPreferences("table", Context.MODE_PRIVATE);

        boolean first = table.getBoolean("firstImage", true);
        int num = table.getInt("numImage", 0);
        if (!first) {
            for (int i = 0; i < num; i++) {
                String s = String.valueOf(i);
                String string = mCatch.getAsString(s);
                if (!string .equals("null")) {
                    File file = mGson.fromJson(string, File.class);
                    mFiles.add(file);
                }

            }
        } else {
            
            mFiles = FileUtils.listFilesInDirWithFilter(Environment.getExternalStorageDirectory(), ".jpg");
            addCatch();
        }
    }

    /**
     * 添加缓存
     */
    public void addCatch() {

        ArrayList<String> strings = new ArrayList<>();
        SharedPreferences first = getActivity().getSharedPreferences("table", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = first.edit();

        for (int i = 0; i < mFiles.size(); i++) {
            String s = mGson.toJson(mFiles.get(i));
            strings.add(s);
        }
        edit.putBoolean("firstImage", false);
        edit.putInt("numImage", strings.size());
        for (int i = 0; i < strings.size(); i++) {
            String s = String.valueOf(i);
            mCatch.put(s, strings.get(i), ACache.TIME_DAY);
        }
        edit.commit();
    }

    /**
     * 下拉刷新
     */
    @Override
    public void onRefresh() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                mFiles = FileUtils.listFilesInDirWithFilter(Environment.getExternalStorageDirectory(), ".jpg");
                addCatch();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        mAdapter.notifyDataSetChanged();
                        mRefreshLayout.setRefreshing(false);
                        Toast.makeText(getContext(), "刷新完成", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();

    }

}