/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2017-03-30
 */
package com.cydroid.softmanager.applock.verifier;

public class Config {
    public boolean mUseFp = true;
    public boolean mUseFpWhenWindowFocus = true;
    public boolean mUseFr = true;
    public boolean mDebug = false;
    public boolean mDebugPwd = false;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Config{");
        sb.append("mUseFp=" + mUseFp);
        sb.append(",mUseFpWhenWindowFocus=" + mUseFpWhenWindowFocus);
        sb.append(",mUseFr=" + mUseFr);
        sb.append(",mDebug=" + mDebug);
        sb.append(",mDebugPwd=" + mDebugPwd);
        sb.append("}");
        return sb.toString();
    }
}
