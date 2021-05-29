package com.example.systemmanageruidemo.actionview;

import com.example.systemmanageruidemo.actionpresent.OptimisePresent;
import com.example.systemmanageruidemo.bean.PBean;

import java.util.List;

public interface OptimiseView extends ViewAction<OptimisePresent> {
    void starclean(int score);
    void onfinishclean(int score);
    void onchangescore(int score);
    void requestlist(List<PBean> list);
    void onresposelist(List<PBean> list);
    void onitemstateschange(int groupindex, boolean isresult);
}
