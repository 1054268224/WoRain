package com.cydroid.systemmanager.rubbishcleaner.common;

import android.graphics.drawable.Drawable;

public class RubbishInfo {
	public Drawable icon;
	public String name;
	// Gionee: <houjie> <2015-10-27> add for CR01575153 begin
    public String desc;
    public String descx;
    // Gionee: <houjie> <2015-10-27> add for CR01575153 end
	public String path;
	public String pkgName;
	public String type;
	public long db_id;
	// public int category;
	public long size;
	public boolean isChecked=true;
	public boolean isInstalled;

	// Gionee: <houjie> <2015-10-27> add for CR01575153 begin
    @Override
    public String toString() {
        return "RubbishInfo(name=" + name + " pkgName=" + pkgName + " desc=" + desc + " type=" + type + " path="
                + path + " size=" + size + " isChecked=" + isChecked + " isInstalled=" + isInstalled
                + " db_id=" + db_id + ")";
    }
    // Gionee: <houjie> <2015-10-27> add for CR01575153 end
}
