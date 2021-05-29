package com.cydroid.powersaver.launcher;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class Function implements Serializable {

    public int functionId;
    public String title;
    public String packageName;
    public String activityName;
    public int iconRes;
    public Drawable icon;
    public boolean functionNull;
    public boolean systemApp;

}
