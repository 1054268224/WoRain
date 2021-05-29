package com.cydroid.softmanager.powersaver.mode.item;

import java.io.FileWriter;
import java.io.IOException;

import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;
import android.os.SystemProperties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import android.content.Context;

public class PowerModeItemGpuLimit extends PowerModeItem {
    private static final String sDevFpsUpperBound = "/d/ged/hal/fps_upper_bound";

    public PowerModeItemGpuLimit(Context context, String mode, PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    // Gionee <yangxinruo> <2016-3-1> add for CR01643624 begin
    private void setGpuFpsLimit(int fps) {
        if (SystemProperties.getInt("ro.gn.fps.low", -1) >= 0) {
            File f = new File(sDevFpsUpperBound);
            FileWriter fw = null;
            try {
                // FOR:Reliance on default encoding 这里只写数字,没必要指定字符集
                fw = new FileWriter(f);// NOSONAR
                Log.d(TAG, "----->GpuLimit(), set gpu limit=" + fps);
                fw.write(String.valueOf(fps));
            } catch (Exception e) {
                Log.d(TAG, "write error");
            } finally {
                if (fw != null) {
                    try {
                        fw.close();
                    } catch (Exception ioe) {
                        Log.d(TAG, "close fw error");
                    }
                }
            }
        }
    }

    private boolean isGpuFpsLimitOn() {
        File f = new File(sDevFpsUpperBound);
        FileReader fr = null;
        BufferedReader br = null;
        try {
            // FOR:Reliance on default encoding 这里只写数字,没必要指定字符集
            fr = new FileReader(f);// NOSONAR
            br = new BufferedReader(fr);
            String s = "";
            String line = "";
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    s = line;
                }
            }
            int val = Integer.parseInt(s);
            Log.d(TAG, "----->GpuLimit get current val=" + val);
            int limitedFps = SystemProperties.getInt("ro.gn.fps.low", 40);
            return val == limitedFps;
        } catch (FileNotFoundException fnfe) {
            Log.d(TAG, "file not found error");
            return false;
        } catch (IOException ioe) {
            Log.d(TAG, "read error");
            return false;
        } catch (NumberFormatException nfe) {
            Log.d(TAG, "number format error");
            return false;
        } catch (Exception ex) {
            Log.d(TAG, "error:" + ex);
            return false;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (fr != null) {
                    fr.close();
                }
            } catch (Exception ioe) {
                Log.d(TAG, "close fr|br error");
            }
        }
    }

    private void setGpuFps() {
        int fps = SystemProperties.getInt("ro.gn.fps.low", 40);
        setGpuFpsLimit(fps);
    }

    private void restoreGpuFps() {
        int fps = SystemProperties.getInt("ro.gn.fps.high", 60);
        setGpuFpsLimit(fps);
    }
    // Gionee <yangxinruo> <2016-3-1> add for CR01643624 end

    @Override
    protected void saveCurrentToPreference(String saveKey) {
        mProviderHelper.putBoolean(saveKey, isGpuFpsLimitOn());
    }

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        boolean configVal = mProviderHelper.getBoolean(saveKey, false);
        Log.d(TAG, "GpuLimit->restore " + configVal + " from " + saveKey);
        if (configVal) {
            setGpuFps();
        } else {
            restoreGpuFps();
        }
        return true;
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
        return false;
    }

}
