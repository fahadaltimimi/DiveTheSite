package com.fahadaltimimi.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseListFragment extends ListFragment {

    private SwipeRefreshLayout mSwipeRefreshLayout = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(getLayoutView(), parent, false);
        mSwipeRefreshLayout = view.findViewById(getSwipeRefreshLayout());
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startRefresh();
                refreshListView();
            }
        });
        return view;
    }

    protected void startRefresh() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }

    protected void stopRefresh() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    protected void refreshListView() {
        startRefresh();
    }

    abstract protected int getLayoutView();
    abstract protected  int getSwipeRefreshLayout();
}
