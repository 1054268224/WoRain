// Gionee <yangxinruo> <2016-3-18> add for CR01654969 begin
package com.cydroid.softmanager.powersaver.mode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.cydroid.softmanager.powersaver.mode.item.PowerModeItem;
import com.cydroid.softmanager.powersaver.mode.item.PowerModeItemPause;
import com.cydroid.softmanager.powersaver.utils.PowerConfig;
import com.cydroid.softmanager.powersaver.utils.PowerConfigParser;
import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;

import android.content.Context;
import android.os.SystemProperties;
import android.content.pm.PackageManager;
import com.cydroid.softmanager.common.Consts;

public class ModeItemsController {
    private static final String TAG = "ModeItemsController";

    private final Context mContext;
    private final PowerConfig mPowerConfig;

    private final LinkedList<PowerModeItem> mItems = new LinkedList<PowerModeItem>();
    private String mMode = "";
    private int mPosition;

    private State mState;

    public enum State {
        INIT, FINISH, PAUSE
    }

    public ModeItemsController(Context context, String mode, String[] configArray) {
        mContext = context;
        mPowerConfig = PowerConfigParser.getProjectConfig(mContext);

        mMode = mode;
        mState = State.INIT;
        mPosition = 0;

        loadPowerItemsFromModeConfig(configArray);
    }

    private boolean loadPowerItemsFromModeConfig(String[] configItemNames) {
        mItems.clear();
        Log.d(TAG, "mode: " + mMode);
        for (String configNameStr : configItemNames) {
            if (configNameStr.length() <= 0) {
                continue;
            }
            String[] configNameArray = configNameStr.split(":");
            String configName = configNameArray[0];
            //guoxt mdodify for CR01774750 begin 
            if(configName.equals("GNPush")){
                continue;
            }
	       //guoxt mdodify for 124274 2017-05-04 begin 
			if(Consts.gnKRFlag && (configName.equals("Gps") || configName.equals("AutoSync"))){
				continue;
				}
             //guoxt mdodify for 124274 2017-05-04 end

            //guoxt mdodify for CSW1703CX-987 2018-06-19 begin
            if(Consts.cyCXFlag && configName.equals("DisableApps")){
                continue;
            }
            //guoxt mdodify for CSW1703CX-987 2018-06-19 end
            String configDefaultVal = PowerModeItem.SWITCH_PASS;
//            int displayPriority = -1;
            if (configNameArray.length > 1)
                configDefaultVal = configNameArray[1];
//            if (configNameArray.length > 2)
//                displayPriority = Integer.parseInt(configNameArray[2]);
            // Gionee xionghg 2017-06-01 add for 150397 begin
            if (Consts.gnIPFlag && (configName.equals("Bluetooth") || configName.equals("Gps") || configName.equals("AutoSync"))) {
                configDefaultVal = PowerModeItem.SWITCH_PASS;
            }
            // Gionee xionghg 2017-06-01 add for 150397 end   
            PowerModeItem item = PowerModeItemSimpleFactory.getInstanceByName(mContext, mMode, configName);
            if (item != null) {
                addItemAtLast(item, configDefaultVal, mItems.size());
            }
        }
        return true;
    }
  //guoxt mdodify for CR01774750 begin 
	public static boolean isPackageExist(Context context,String packagename) {
        try{		
            PackageManager pm = context.getPackageManager();	
            pm.getInstallerPackageName(packagename);	
            return true;	
        }catch(Exception e) {		
            return false;	
        }	
    }
    //guoxt mdodify for CR01774750 end 

    public boolean addItemAfterItem(PowerModeItem newItem, String searchItem) {
        int i;
        int insertIndex = -1;
        for (i = 0; i < mItems.size(); i++) {
            PowerModeItem item = mItems.get(i);
            if (item.getName().equals(searchItem)) {
                insertIndex = i;
            }
            if (item.getName().equals(newItem.getName())) {
                Log.d(TAG, "duplicate item " + newItem.getName());
                return false;
            }
        }
        if (insertIndex >= 0) {
            mItems.add(insertIndex + 1, newItem);
            return true;
        }
        return false;
    }

//    public boolean addItemAtLast(String name, String defaultVal) {
//        return addItemAtLast(name, defaultVal, Integer.MAX_VALUE, true);
//    }

    private boolean addItemAtLast(PowerModeItem newItem, String defaultVal, int position) {
        int i = 0;
        for (i = 0; i < mItems.size(); i++) {
            PowerModeItem item = mItems.get(i);
            if (item.getName().equals(newItem.getName())) {
                Log.d(TAG, "duplicate item " + newItem.getName());
                return false;
            }
        }
        if (defaultVal == null || defaultVal.isEmpty()) {
            defaultVal = PowerModeItem.SWITCH_PASS;
        }
        newItem.setDefaultValue(defaultVal);
        if (position > mItems.size()) {
            mItems.add(newItem);
        } else {
            mItems.add(position, newItem);
        }
        return true;
    }

    public PowerModeItem getItemByName(String name) {
        for (PowerModeItem item : mItems) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }

//    public void saveToConfig() {
//        Log.d(TAG, "saveConfig items(count:" + mItems.size() + ")");
//        for (PowerModeItem item : mItems) {
//            item.store();
//        }
//    }

    public void saveCheckPoint() {
        Log.d(TAG, "saveCheckPoint items(count:" + mItems.size() + ")");
        for (PowerModeItem item : mItems) {
            item.save();
        }
    }

    public boolean applyConfig() {
        Log.d(TAG, "applyConfig items(count:" + mItems.size() + ")");
        boolean res = true;
        if (mState == State.PAUSE) {
            Log.d(TAG, "applyConfig items paused at " + mPosition + ",continue");
            PowerModeItem pauseItem = mItems.get(mPosition);
            if (pauseItem instanceof PowerModeItemPause) {
                ((PowerModeItemPause) pauseItem).runAfterCallback();
                mPosition++;
            }
        }
        int i = 0;
        for (i = mPosition; i < mItems.size(); i++) {
            PowerModeItem item = mItems.get(i);
            if (item instanceof PowerModeItemPause) {
                ((PowerModeItemPause) item).runBeforeCallback();
                mPosition = i;
                mState = State.PAUSE;
                return res;
            } else {
                item.applyWithCallback();
            }
        }
        mPosition = i;
        mState = State.FINISH;
        return res;
    }

    public boolean restoreCheckPoint() {
        return restoreCheckPoint(false);
    }

    public boolean restoreCheckPoint(boolean isShouldIgnoreExternalChange) {
        Log.d(TAG, "restoreCheckPoint items(count:" + mItems.size() + ")");
        boolean res = true;
        if (mState == State.PAUSE) {
            Log.d(TAG, "restoreCheckPoint items paused at " + mPosition + ",continue");
            PowerModeItem pauseItem = mItems.get(mPosition);
            if (pauseItem instanceof PowerModeItemPause) {
                ((PowerModeItemPause) pauseItem).runAfterCallback();
                mPosition++;
            }
        }
        int i = 0;
        for (i = mPosition; i < mItems.size(); i++) {
            PowerModeItem item = mItems.get(i);
            if (item instanceof PowerModeItemPause) {
                ((PowerModeItemPause) item).runBeforeCallback();
                mPosition = i;
                mState = State.PAUSE;
                return res;
            } else {
                item.restoreWithCallback(isShouldIgnoreExternalChange);
            }
        }
        mPosition = i;
        mState = State.FINISH;
        return res;
    }

//    public void resetConfig() {
//        // mProviderHelper.removeKey(CONFIG_CHANGED_PREFIX + mMode);
//        Log.d(TAG, "isChanged items(count:" + mItems.size() + ")");
//        for (int i = 0; i < mItems.size(); i++) {
//            PowerModeItem item = mItems.get(i);
//            item.resetToDefaultValue();
//        }
//    }

    public void setConfigFromItemInfos(List<ModeItemInfo> configList) {
        for (ModeItemInfo itemInfo : configList) {
            setConfigFromItemInfo(itemInfo.name, itemInfo);
        }
    }

    private void setConfigFromItemInfo(String name, ModeItemInfo info) {
        for (PowerModeItem item : mItems) {
            if (item.getName().equals(name)) {
                item.setModeItemInfo(info);
                break;
            }
        }
    }

    public void resetConfigToDefault() {
        for (PowerModeItem item : mItems) {
            item.resetToDefaultValue();
        }
    }

    public boolean isConfigChangedFromDefault() {
        Log.d(TAG, "isChanged items(count:" + mItems.size() + ")");
        for (int i = 0; i < mItems.size(); i++) {
            PowerModeItem item = mItems.get(i);
            if (!item.isConfigEqualToDefaultValue()) {
                return true;
            }
        }
        return false;
    }

    public float getCalculatedWeights() {
        Log.d(TAG, "getWeights items(count:" + mItems.size() + ")");
        float res = 0f;
        for (PowerModeItem item : mItems) {
            res -= item.getCalculatedWeight(mPowerConfig);
        }
        return res;
    }

    public float getDefaultWeights() {
        Log.d(TAG, "getWeights items(count:" + mItems.size() + ")");
        float res = 0f;
        for (int i = 0; i < mItems.size(); i++) {
            PowerModeItem item = mItems.get(i);
            float weight = item.getWeightForValue(mPowerConfig, item.getDefaultValue());
            res -= weight;
            Log.d(TAG, "getDefaultWeights get weight for " + item.getName() + " weight=" + weight);
        }
        return res;
    }

    public float getCurrentWeights() {
        Log.d(TAG, "getCurrentWeights items(count:" + mItems.size() + ")");
        float res = 0f;
        for (int i = 0; i < mItems.size(); i++) {
            PowerModeItem item = mItems.get(i);
            float weight = item.getWeightForCurrent(mPowerConfig);
            res -= weight;
            Log.d(TAG, "getCurrentWeights get weight for " + item.getName() + " weight=" + weight);
        }
        return res;
    }

    public List<ModeItemInfo> getItemInfos() {
        Log.d(TAG, "getItemInfos mItemsInfo(count:" + mItems.size() + ")");
        ArrayList<ModeItemInfo> itemsInfo = new ArrayList<ModeItemInfo>();
        for (int i = 0; i < mItems.size(); i++) {
            ModeItemInfo info = mItems.get(i).getModeItemInfo();
            itemsInfo.add(info);
        }
        return itemsInfo;
    }

    public int removeItem(String name) {
        int i = 0;
        for (i = 0; i < mItems.size(); i++) {
            PowerModeItem item = mItems.get(i);
            if (item.getName().equals(name))
                break;
        }
        if (i >= mItems.size())
            return -1;
        Log.d(TAG, "remove item " + name + " from " + mMode);
        mItems.remove(i);
        return mItems.size();
    }

    public State getState() {
        return mState;
    }

    public void replaceAndUnionByMode(ModeItemsController modeItemsController) {
        for (ModeItemInfo otherControllerItemInfo : modeItemsController.getItemInfos()) {
            boolean replaceFlag = false;
            for (int i = 0; i < mItems.size(); i++) {
                if (mItems.get(i).getName().equals(otherControllerItemInfo.name)) {
                    Log.d(TAG, "unionMode replace item " + otherControllerItemInfo.name + " from "
                            + modeItemsController.getMode());
                    mItems.set(i, modeItemsController.getItemByName(otherControllerItemInfo.name));
                    replaceFlag = true;
                }
            }
            if (!replaceFlag) {
                Log.d(TAG, "unionMode add item " + otherControllerItemInfo.name + " from "
                        + modeItemsController.getMode());
                mItems.add(modeItemsController.getItemByName(otherControllerItemInfo.name));
            }
        }
    }

    public void replaceByAndCutMode(ModeItemsController modeItemsController) {
        Iterator<ModeItemInfo> otherControllerItemInfoIterator = modeItemsController.getItemInfos()
                .iterator();
        while (otherControllerItemInfoIterator.hasNext()) {
            ModeItemInfo otherControllerItemInfo = otherControllerItemInfoIterator.next();
            for (int i = 0; i < mItems.size(); i++) {
                if (mItems.get(i).getName().equals(otherControllerItemInfo.name)) {
                    Log.d(TAG,
                            "intersectMode replace item " + otherControllerItemInfo.name + " from "
                                    + modeItemsController.getMode() + " and remove from "
                                    + modeItemsController.getMode());
                    mItems.set(i, modeItemsController.getItemByName(otherControllerItemInfo.name));
                    modeItemsController.removeItem(otherControllerItemInfo.name);
                    otherControllerItemInfoIterator.remove();
                }
            }
        }
    }

    public String getMode() {
        return mMode;
    }

    public void moveToFirst(String itemName) {
        PowerModeItem item = getItemByName(itemName);
        if (item == null) {
            return;
        }
        mItems.remove(item);
        mItems.addFirst(item);
    }

    public void moveToLast(String itemName) {
        PowerModeItem item = getItemByName(itemName);
        if (item == null) {
            return;
        }
        mItems.remove(item);
        mItems.addLast(item);
    }
}
// Gionee <yangxinruo> <2016-3-18> add for CR01654969 end