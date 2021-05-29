// Gionee: <houjie> <2015-10-27> add for CR01575153 begin 
package com.cydroid.systemmanager.rubbishcleaner;

public interface RubbishCleanerStrategy {
    void init();
    void recycle();
    void startScanRubbish();
    int getCMCleanScanType();
    
    int getAdScanType();
    int getCacheScanType();
    int getApkScanType();
    int getResidualScanType();
    int getBigFileScanType();

    boolean getCacheChecked();
    boolean getResidualChecked();
}
// Gionee: <houjie> <2015-10-27> add for CR01575153 end
