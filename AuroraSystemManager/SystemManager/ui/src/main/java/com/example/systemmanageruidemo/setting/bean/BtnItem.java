package com.example.systemmanageruidemo.setting.bean;

import com.example.systemmanageruidemo.setting.ItemTypeDef;

public class BtnItem extends BaseItem {
    private int id;
    private String title;

    public BtnItem(int id, String title) {
        this.id = id;
        this.title = title;
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

    @Override
    public int typeCode() {
        return ItemTypeDef.Type.ITEM2.getCode();
    }
}
