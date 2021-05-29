package com.example.systemmanageruidemo.optimise;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.cydroid.softmanager.view.AnimBallView;
import com.cydroid.softmanager.view.ScoreCountView;
import com.example.systemmanageruidemo.BaseSupportProxyActivity;
import com.example.systemmanageruidemo.BuildConfig;
import com.example.systemmanageruidemo.R;
import com.example.systemmanageruidemo.actionpresent.OptimisePresent;
import com.example.systemmanageruidemo.actionview.OptimiseView;
import com.example.systemmanageruidemo.adapter.RecyclerAdapter;
import com.example.systemmanageruidemo.bean.DataBean;
import com.example.systemmanageruidemo.bean.PBean;
import com.example.systemmanageruidemo.testdata.OptimisePresenttest;


import java.util.ArrayList;
import java.util.List;

public class OptimiseActivity extends BaseSupportProxyActivity<OptimisePresent> implements OptimiseView {
    private Context mContext;
    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;

    private DataBean dataBean;
    private DataBean.DataBeanChild child;

    private List<DataBean> dataBeans = new ArrayList<>();
    private List<DataBean.DataBeanChild> children;

    private ScoreCountView scoreCount;
    private TextView mBtnOptimise;

    private AnimBallView animBallView;
    private boolean isStart = true;
    private List<PBean> list = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.ONLYTEST) {
            presenter = new OptimisePresenttest(this);
        }
        super.onCreate(savedInstanceState);
        mContext = getRealContext();
        if (getIntent() != null)
            mIntentScore = getIntent().getIntExtra("SYSTEM_SCORE", mIntentScore);
        setContentView(R.layout.activity_optimise);
        recyclerView = findViewById(R.id.optimising_item_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        initView();
        initData();

    }

    private int mIntentScore = 100;

    private void initData() {
        requestlist(list);
        starclean(mIntentScore);
    }

    private void initView() {
        scoreCount = (ScoreCountView) findViewById(R.id.score_view);
        mBtnOptimise = (TextView) findViewById(R.id.optimise_btn);
        animBallView = (AnimBallView) findViewById(R.id.anim_ball_view);
        animBallView.onStartAnim();
        scoreCount.setDefaultScore(mIntentScore);
    }


    OptimisePresent presenter;

    @Override
    public void setPresenter(OptimisePresent presenter) {
        this.presenter = presenter;
    }

    @Override
    public OptimisePresent getPresenter(OptimisePresent presenter) {
        return presenter;
    }

    @Override
    public void starclean(int currentscore) {
        presenter.onStarclean(currentscore);
    }

    @Override
    public void onfinishclean(int score) {

    }

    @Override
    public void onchangescore(int score) {
        try {
            scoreCount.scoreChange(score);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void requestlist(List<PBean> list) {
        presenter.onRequestlist(list);
    }

    @Override
    public void onresposelist(List<PBean> list) {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onitemstateschange(int groupindex, boolean isresult) {
        adapter.notifyDataSetChanged();
    }
}