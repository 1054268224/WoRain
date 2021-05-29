/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2016-12-27
 */
package com.cydroid.softmanager.memoryclean.strategy;

import com.cydroid.softmanager.utils.Log;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class MemoryCleanWeaponsFactory {
    private static final String TAG = "MemoryCleanWeaponsFactory";
    private static final List<Class<?>> WEAPONS_LIST = new ArrayList<Class<?>>();
    static {
        WEAPONS_LIST.add(Rocket.class);
        WEAPONS_LIST.add(AssultRifle.class);
        WEAPONS_LIST.add(Cannon.class);
        WEAPONS_LIST.add(Emmagee.class);
        WEAPONS_LIST.add(Pistol.class);
    }

    public static MemoryCleanWeapon createWeapon(int index) {
        Class<?> weaponClass = WEAPONS_LIST.get(index);
        if (weaponClass != null) {
            return createWeapon(weaponClass);
        }
        return null;
    }

    private static MemoryCleanWeapon createWeapon(Class<?> weaponClass) {
        MemoryCleanWeapon weapon = null;
        try {
            Constructor con = weaponClass.getConstructor();
            weapon = (MemoryCleanWeapon) con.newInstance();
        } catch (Exception e) {
            Log.e(TAG, "createWeapon exception:" + e.toString());
        }
        return weapon;
    }
}
