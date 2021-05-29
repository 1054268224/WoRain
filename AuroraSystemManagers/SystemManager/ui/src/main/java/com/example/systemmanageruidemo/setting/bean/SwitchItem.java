package com.example.systemmanageruidemo.setting.bean;

import com.example.systemmanageruidemo.setting.ItemTypeDef;

public class SwitchItem extends BaseItem {
    private int id;
    private String title;
    private Boolean isTrue;

    public SwitchItem(int id, String title, boolean isTrue) {
        this.id = id;
        this.title = title;
        this.isTrue = isTrue;
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

    public Boolean getTrue() {
        return isTrue;
    }

    public void setTrue(Boolean aTrue) {
        isTrue = aTrue;
    }

    @Override
    public int typeCode() {
        return ItemTypeDef.Type.ITEM3.getCode();
    }
}
