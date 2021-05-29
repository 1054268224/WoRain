/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Pu Yongming
 *
 * Date: 2017-03-24
 */
package com.cydroid.softmanager.applock.verifier;

public interface LifecycleAware {
    void onCreate();
    void onStart();
    void onResume();
    void onPause();
    void onStop();
    void onDestroy();

    void onWindowFocusChanged(boolean focus);
}
