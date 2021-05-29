package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.powersaver.utils.PowerConfig;
import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;

import java.util.ArrayList;

import com.cydroid.softmanager.R;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.provider.Settings;

public class PowerModeItemScreenOffTimeout extends PowerModeItem {
    public PowerModeItemScreenOffTimeout(Context context, String mode,
            PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    private int getScreenTimeout() {
        int defTimeout = 15000;
        return Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT,
                defTimeout);
    }

    /* screen timeout controller */
    private void setScreenTimeout(int timeout) {
        int value = timeout;
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, value);
    }

    @Override
    protected void saveCurrentToPreference(String saveKey) {
        mProviderHelper.putInt(saveKey, getScreenTimeout());
    }

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        int configVal = mProviderHelper.getInt(saveKey, 15000);
        setScreenTimeout(configVal);
        return true;
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
        int configVal = mProviderHelper.getInt(compareConfigKey, 15000);
        return getScreenTimeout() != configVal;
    }

    @Override
    public float getWeightForValue(PowerConfig powerConfig, String value) {
        if (value == null) {
            return 0f;
        }
        int timeout = 15000;
        try {
            timeout = Integer.parseInt(value);
        } catch (Exception e) {
            return 0f;
        }
        float weight = 0.0f;
        switch (timeout) {
            case 15000:
                weight = powerConfig.timeout_zero_weight;
                break;
            case 30000:
                weight = powerConfig.timeout_one_weight;
                break;
            case 60000:
                weight = powerConfig.timeout_two_weight;
                break;
            case 120000:
                weight = powerConfig.timeout_three_weight;
                break;
            case 300000:
                weight = powerConfig.timeout_four_weight;
                break;
            case 600000:
                weight = powerConfig.timeout_five_weight;
                break;
            case 1800000:
                weight = powerConfig.timeout_six_weight;
                break;
            case -1:
                weight = powerConfig.timeout_seven_weight;
                break;
            default:
                weight = powerConfig.timeout_zero_weight;
                break;
        }
        return weight;
    }

    @Override
    public ArrayList<String> getAvailCandidateValues() {
        ArrayList<String> res = new ArrayList<String>();
        res.add("15000");
        res.add("30000");
        res.add("60000");
        res.add("120000");
        res.add("300000");
        res.add("600000");
        res.add("1800000");
        return res;
    }

    @Override
    public ArrayList<String> getCandidateValueDescs() {
        ArrayList<String> decs = super.getCandidateValueDescs();
        for (int i = 0; i < decs.size(); i++) {
            String dec = decs.get(i);
            int decVal = 0;
            try {
                decVal = Integer.parseInt(dec);
                String newValStr = "";
                //Gionee <GN_Oversea_Bug> <xuyongji> <20161214> modify for CR01776095 beign
                if (decVal > 60000) {
                    newValStr = mContext.getResources().getString(R.string.mode_item_timeout_ms,
                            decVal / 60000);
                } else if (decVal == 60000) {
                	newValStr = mContext.getResources().getString(R.string.mode_item_timeout_m,
                            decVal / 60000);
                } else {
                    newValStr = mContext.getResources().getString(R.string.mode_item_timeout_s,
                            decVal / 1000);
                }
                //Gionee <GN_Oversea_Bug> <xuyongji> <20161214> modify for CR01776095 end
                decs.set(i, newValStr);
            } catch (NumberFormatException nfe) {
                continue;
            } catch (NotFoundException nofe) {
                continue;
            } catch (Exception ex) {
                continue;
            }
        }
        return decs;
    }

    @Override
    public String getTitle() {
        return mContext.getResources().getString(R.string.set_sleep_time);
    }

    @Override
    public String getFormat() {
        return mContext.getResources().getString(R.string.mode_item_timeout_perfix);
    }

    @Override
    public float getWeightForCurrent(PowerConfig powerConfig) {
        return getWeightForValue(powerConfig, String.valueOf(getScreenTimeout()));
    }
}
