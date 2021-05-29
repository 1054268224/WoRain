package com.example.systemmanageruidemo.actionpresent;

import com.example.systemmanageruidemo.actionview.OptimiseView;
import com.example.systemmanageruidemo.bean.PBean;

import java.util.List;

public interface OptimisePresent  extends PresentI<OptimiseView>{
    void onStarclean(int score);
    void changeScore(int a);
    void finishClean(int a);
    void onRequestlist(List<PBean> list);
    void responseList(List<PBean> list);
    void itemStatesChange(int groupindex, boolean isresult);
}
