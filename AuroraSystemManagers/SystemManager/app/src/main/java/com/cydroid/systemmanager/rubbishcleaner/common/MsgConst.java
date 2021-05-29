package com.cydroid.systemmanager.rubbishcleaner.common;

import android.os.SystemProperties;

public class MsgConst {
	public static final int START = 0;
	public static final int SCAN_ITEM = 1;
	public static final int FIND_ITEM = 2;
	public static final int END = 3;
	public static final int DEL_ITEM = 4;
	public static final int DEL_ITEM_BY_DIALOG = 5;
	public static final int DEL_END = 6;

    // Gionee: <houjie> <2015-10-27> add for CR01575153 begin
    public static final int DATA_MANAGER_TYPE_NORMAL = 0;
    public static final int DATA_MANAGER_TYPE_DEEPLY = 1;

    public static final String RUBBISH_DETIAL_EXTRA_CLEAN_TYPE = "com.cydroid.systemmanager.rubbishcleaner.extra.CLEAN_TYPE";
    public static final String RUBBISH_DETIAL_EXTRA_RUBBISH_TYPE = "com.cydroid.systemmanager.rubbishcleaner.extra.RUBBISH_TYPE";
    // Gionee: <houjie> <2015-10-27> add for CR01575153 end
	public static final boolean OVERSEA_VF_CUSTOM = SystemProperties.get("ro.cy.custom", "unknown").equals("VISUALFAN");
	//Chenyee guoxt 20180505 modify for CSW1703VF-53 begin
	public static final String cyProject = SystemProperties.get("ro.cy.common.mainboard.prop");
	public static final boolean cy1703VF = cyProject.equals("CSW1703A") && OVERSEA_VF_CUSTOM;
	//Chenyee guoxt 20180505 modify for CSW1703VF-53 end
}
