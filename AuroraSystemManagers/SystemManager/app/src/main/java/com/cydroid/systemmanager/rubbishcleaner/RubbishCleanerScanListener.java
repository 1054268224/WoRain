// Gionee: <houjie> <2015-10-27> add for CR01575153 begin 
package com.cydroid.systemmanager.rubbishcleaner;

public interface RubbishCleanerScanListener {
    void onScanStart();
    void onScanItem(int group,Object obj);
    void onFindItem(int group,Object obj);
    void onScanEnd(int group);
}
// Gionee: <houjie> <2015-10-27> add for CR01575153 end