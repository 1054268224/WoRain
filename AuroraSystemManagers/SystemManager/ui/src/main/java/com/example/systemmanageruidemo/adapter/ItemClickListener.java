package com.example.systemmanageruidemo.adapter;

import com.example.systemmanageruidemo.bean.DataBean;

public interface ItemClickListener {

    void onExpandChildren(DataBean dataBean);

    void onHideChildren(DataBean dataBean);



    void onCheckChange(DataBean dataBean,boolean ischeck);

}
