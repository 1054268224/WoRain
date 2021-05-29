package com.example.systemmanageruidemo.softmanager.adapter;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.TextView;

public class SoftItem {
    String packagename;
    private Drawable imageId;
    private String name;
    private String momory;
    private Boolean isTrue;

    public String getPackagename() {
        return packagename;
    }

    public void setPackagename(String packagename) {
        this.packagename = packagename;
    }

    public SoftItem(String packagename, Drawable imageId, String name, String momory, Boolean isTrue) {
        this.packagename=packagename;
        this.imageId = imageId;
        this.name = name;
        this.momory = momory;
        this.isTrue = isTrue;
    }

    public Drawable getImageId() {
        return imageId;
    }

    public void setImageId(Drawable imageId) {
        this.imageId = imageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMomory() {
        return momory;
    }

    public void setMomory(String momory) {
        this.momory = momory;
    }

    public Boolean getTrue() {
        return isTrue;
    }

    public void setTrue(Boolean aTrue) {
        isTrue = aTrue;
    }
}
