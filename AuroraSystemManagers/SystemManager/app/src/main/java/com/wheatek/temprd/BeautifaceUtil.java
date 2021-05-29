//Gionee  ningtao 20170713 modify for 161974 begin
package com.wheatek.temprd;

import java.lang.reflect.Method;

import android.os.SystemProperties;

public class BeautifaceUtil {
	
	
	public static boolean isWholebeautifaceSupport = getWholebeautifaceSupport();
	 
	static{
        if(BeautifaceUtil.getGnZnVersionNum().contains("SWW1617") || BeautifaceUtil.gnKRflag  
        		|| BeautifaceUtil.getGnZnVersionNum().contains("SW17W08")
        		|| BeautifaceUtil.getGnZnVersionNum().contains("SW17W13")){
            isWholebeautifaceSupport = false;
        }
    }

    public static final boolean gnKRflag = SystemProperties.get("ro.gn.oversea.custom").equals("KOREA_BOE");
    
   
    
    public static boolean getWholebeautifaceSupport() {
		try {
			Class cls = Class.forName("android.hardware.Camera");
			Method method = cls.getMethod("isExBeautyShotSupported");
			Boolean mObject=(Boolean)method.invoke(null);
			return mObject;
		} catch (Exception ex) {
			return false;
		}
	}
    
    
    public static String getGnZnVersionNum() {
        if(isOverseaProduct()) {
            return SystemProperties.get("ro.gn.gnvernumber");
        } else {
            return SystemProperties.get("ro.gn.gnznvernumber");
        }
    }
    
    public static boolean isOverseaProduct() {
        return SystemProperties.get("ro.gn.oversea.product").equals("yes");
    }
}
//Gionee ningtao  20170713 modify for 161974 end