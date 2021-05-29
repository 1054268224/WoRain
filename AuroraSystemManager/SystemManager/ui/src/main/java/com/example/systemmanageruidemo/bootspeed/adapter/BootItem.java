package com.example.systemmanageruidemo.bootspeed.adapter;

import android.graphics.drawable.Drawable;

public class BootItem {
    Drawable imageID;
    String name;
    Boolean isTrue;
    String packagename;

    public String getPackagename() {
        return packagename;
    }

    public void setPackagename(String packagename) {
        this.packagename = packagename;
    }

    public BootItem(String packagename, Drawable imageID, String name, Boolean isTrue) {
        this.packagename = packagename;
        this.imageID = imageID;
        this.name = name;
        this.isTrue = isTrue;
    }

    public Drawable getImageID() {
        return imageID;
    }

    public void setImageID(Drawable imageID) {
        this.imageID = imageID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getTrue() {
        return isTrue;
    }

    public void setTrue(Boolean aTrue) {
        isTrue = aTrue;
    }
}
