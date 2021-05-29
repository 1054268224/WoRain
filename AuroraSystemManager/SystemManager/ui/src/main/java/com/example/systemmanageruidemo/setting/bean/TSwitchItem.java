package com.example.systemmanageruidemo.setting.bean;

import com.example.systemmanageruidemo.setting.ItemTypeDef;

public class TSwitchItem extends BaseItem {
    private int id;
    private String title;
    private String desc;
    private Boolean isTrue;

    public TSwitchItem(int id, String title, String desc, Boolean isTrue) {
        this.id = id;
        this.title = title;
        this.desc = desc;
        this.isTrue = isTrue;
    }

    public Boolean getTrue() {
        return isTrue;
    }

    public void setTrue(Boolean aTrue) {
        isTrue = aTrue;
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
        return ItemTypeDef.Type.ITEM4.getCode();
    }
}
