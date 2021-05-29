package com.example.systemmanageruidemo.testdata;

import com.example.systemmanageruidemo.actionpresent.OptimisePresent;
import com.example.systemmanageruidemo.actionview.OptimiseView;
import com.example.systemmanageruidemo.bean.PBean;

import java.util.List;

public class OptimisePresenttest implements OptimisePresent {

    OptimiseView view;

    public OptimisePresenttest(OptimiseView view) {
        this.view = view;
    }

    @Override
    public void onStarclean(int score) {
        for (int i = 0; i < 4; i++) {
            changeScore(-1);
        }
        finishClean(0);
    }

    @Override
    public void changeScore(int a) {
        getViewAction().onchangescore(a);
    }

    @Override
    public void finishClean(int a) {
        getViewAction().onfinishclean(0);
    }

    @Override
    public void onRequestlist(List<PBean> list) {
        for (int i = 0; i < 4; i++) {
            list.add(new PBean());
        }
        responseList(list);
    }

    @Override
    public void responseList(List<PBean> list) {
        getViewAction().onresposelist(list);
    }

    @Override
    public void itemStatesChange(int groupindex, boolean isresult) {
        view.onitemstateschange(groupindex, isresult);
    }

    @Override
    public void setViewAction(OptimiseView viewAvtion) {
        this.view = viewAvtion;
    }

    @Override
    public OptimiseView getViewAction() {
        return view;
    }
}
