/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-29
 */
package com.cydroid.softmanager.update;

public class GeneralUpdateStrategyFactory extends UpdateStrategyFactory {
    private static final String TAG = "GeneralUpdateStrategyFactory";
    private static GeneralUpdateStrategyFactory sInstance;

    public synchronized static GeneralUpdateStrategyFactory getInstance() {
        if (null == sInstance) {
            sInstance = new GeneralUpdateStrategyFactory();
        }
        return sInstance;
    }

    private GeneralUpdateStrategyFactory() {
       // mUpdateStrategyList.add(AlignWhiteListUpdateStrategy.class);
        // mUpdateStrategyList.add(FreezeAppListUpdateStrategy.class);
        // mUpdateStrategyList.add(RootAppListUpdateStrategy.class);
        // mUpdateStrategyList.add(CompetingAppListUpdateStrategy.class);
        // Gionee <yangxinruo><2016-1-5> add for CR01618272 begin
//        mUpdateStrategyList.add(BlackKillListUpdateStrategy.class);
        // Gionee <yangxinruo><2016-1-5> add for CR01618272 end
        // Gionee <yangxinruo> <2016-5-10> add for CR01658695 begin
//        if (SystemProperties.get("ro.gn.app.securepay.support", "no").equals("yes")) {
//            mUpdateStrategyList.add(SafeListUpdateStrategy.class);
//        }
        // Gionee <yangxinruo> <2016-5-10> add for CR01658695 end
        mUpdateStrategyList.add(VirusLibraryUpdateStrategy.class);
        
        //mengjk modify for CR147217 begin
//      mUpdateStrategyList.add(OneCleanListUpdateStrategy.class);
//      mUpdateStrategyList.add(BlackKillListUpdateStrategy.class);
//      mUpdateStrategyList.add(AutoBootBlackListUpdateStrategy.class);
    //mengjk modify for CR147217 end
    }
}
