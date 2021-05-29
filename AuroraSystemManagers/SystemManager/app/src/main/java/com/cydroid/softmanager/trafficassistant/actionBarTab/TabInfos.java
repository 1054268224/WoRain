package com.cydroid.softmanager.trafficassistant.actionBarTab;

public class TabInfos {

    private int mTabNums = 2;
    private String[] mTabTitleTexts;
    private int[] mLayoutId;
    private int mCurrItem = 0;

    public int getTabNums() {
        return mTabNums;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI_EXPOSE_REP", justification = "seems no problem")
    public String[] getTabTexts() {
        return mTabTitleTexts;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI_EXPOSE_REP", justification = "seems no problem")
    public int[] getLayoutIds() {
        return mLayoutId;
    }
    
    public int getTabCurrItem(){
    	return mCurrItem;
    }

    public void setTabNums(int nums) {
        this.mTabNums = nums;
    }

    public void setTabTexts(String[] txts) {
        mTabTitleTexts = new String[txts.length];
        System.arraycopy(txts, 0, mTabTitleTexts, 0, txts.length);
        // this.mTabTitleTexts = txts;
    }

    public void setLayoutIds(int[] ids) {
        mLayoutId = new int[ids.length];
        System.arraycopy(ids, 0, mLayoutId, 0, ids.length);
        // mLayoutId = ids;
    }
    
    public void setTabCurrItem(int item){
    	mCurrItem = item;
    }
}
