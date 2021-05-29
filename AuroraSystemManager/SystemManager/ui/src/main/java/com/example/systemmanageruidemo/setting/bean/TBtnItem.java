package com.example.systemmanageruidemo.setting.bean;

import com.example.systemmanageruidemo.setting.ItemTypeDef;

public class TBtnItem extends BaseItem {
    private int id;
    private String title;
    private String desc;

    public TBtnItem(int id, String title, String desc) {
        this.id = id;
        this.title = title;
        this.desc = desc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public int typeCode() {
        return ItemTypeDef.Type.ITEM5.getCode();
    }
}
