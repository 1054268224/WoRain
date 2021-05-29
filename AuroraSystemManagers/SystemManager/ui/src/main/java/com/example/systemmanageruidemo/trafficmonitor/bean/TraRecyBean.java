package com.example.systemmanageruidemo.trafficmonitor.bean;

import android.annotation.Nullable;
import android.content.integrity.AtomicFormula;
import android.graphics.drawable.Drawable;

public class TraRecyBean {

    String packageName;
    Drawable imageId;
    String name;
    long usedTraSize;
    boolean islimit;

    boolean isInvalidControlApp; //禁止被控制

    public boolean isInvalidControlApp() {
        return isInvalidControlApp;
    }

    public void setInvalidControlApp(boolean invalidControlApp) {
        isInvalidControlApp = invalidControlApp;
    }

    public TraRecyBean(String packageName, String name) {
        this.packageName = packageName;
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
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

    public long getUsedTraSize() {
        return usedTraSize;
    }

    public void setUsedTraSize(long usedTraSize) {
        this.usedTraSize = usedTraSize;
    }

    public boolean isIslimit() {
        return islimit;
    }

    public void setIslimit(boolean islimit) {
        this.islimit = islimit;
    }
}
