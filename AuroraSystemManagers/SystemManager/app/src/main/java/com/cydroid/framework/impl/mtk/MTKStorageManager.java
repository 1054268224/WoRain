/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cydroid.framework.impl.mtk;

import com.cydroid.framework.IGnStorageManager;

import android.os.Environment;
import android.os.RemoteException;
import android.os.ServiceManager;
import  com.cydroid.softmanager.utils.Log;
import android.os.StatFs;
//import android.os.storage.IMountService;

/**
 * StorageManager is the interface to the systems storage service. The storage manager handles storage-related
 * items such as Opaque Binary Blobs (OBBs).
 * <p>
 * OBBs contain a filesystem that maybe be encrypted on disk and mounted on-demand from an application. OBBs
 * are a good way of providing large amounts of binary assets without packaging them into APKs as they may be
 * multiple gigabytes in size. However, due to their size, they're most likely stored in a shared storage pool
 * accessible from all programs. The system does not guarantee the security of the OBB file itself: if any
 * program modifies the OBB, there is no guarantee that a read from that OBB will produce the expected output.
 * <p>
 * Get an instance of this class by calling {@link android.content.Context#getSystemService(java.lang.String)}
 * with an argument of {@link android.content.Context#STORAGE_SERVICE}.
 */

public class MTKStorageManager implements IGnStorageManager {

    private static final String TAG = "StorageManager";
    // / M: @{
    private static final String ICS_STORAGE_PATH_SD1 = "/mnt/sdcard";
    private static final String ICS_STORAGE_PATH_SD2 = "/mnt/sdcard2";
    private static final String STORAGE_PATH_SD1 = "/storage/sdcard0";
    private static final String STORAGE_PATH_SD2 = "/storage/sdcard1";

    // / @}

    public String getVolumeState(String mountPoint) {
        if (mountPoint.equals(ICS_STORAGE_PATH_SD1)) {
            Log.d(TAG, "For backwards compatibility, replace " + mountPoint + " to /storage/sdcard0");
            mountPoint = STORAGE_PATH_SD1;
        } else if (mountPoint.equals(ICS_STORAGE_PATH_SD2)) {
            Log.d(TAG, "For backwards compatibility, replace " + mountPoint + " to /storage/sdcard1");
            mountPoint = STORAGE_PATH_SD2;
        }

//        IMountService mMountService;
//        mMountService = IMountService.Stub.asInterface(ServiceManager.getService("mount"));
//        if (mMountService == null) {
//            return Environment.MEDIA_REMOVED;
//        }
//
//        try {
//            return mMountService.getVolumeState(mountPoint);
//        } catch (Exception e) {
//            Log.e(TAG, "Failed to get volume state", e);
            return null;
//        }
    }

    public String getGnAvailableExternalStoragePath(long requireSize) {
        // Gionee <bug><huangshuiqiang> <2014-08-11> add for CR01321593 begin
        try {
            // Gionee <bug><huangshuiqiang> <2014-08-11> add for CR01321593 end
            if (Environment.MEDIA_MOUNTED.equals(getVolumeState(STORAGE_PATH_SD1))) {
                StatFs stat = new StatFs(STORAGE_PATH_SD1);
                if ((long) stat.getAvailableBlocks() * (long) stat.getBlockSize() > requireSize) {
                    return STORAGE_PATH_SD1;
                }
            }

            if (Environment.MEDIA_MOUNTED.equals(getVolumeState(STORAGE_PATH_SD2))) {
                StatFs stat2 = new StatFs(STORAGE_PATH_SD2);
                if ((long) stat2.getAvailableBlocks() * (long) stat2.getBlockSize() > requireSize) {
                    return STORAGE_PATH_SD2;
                }
            }
            // Gionee <bug><huangshuiqiang> <2014-08-11> add for CR01321593
            // begin
        } catch (Exception e) {
            Log.e(TAG, "Failed to get statFs", e);
            return null;
        }
        // Gionee <bug><huangshuiqiang> <2014-08-11> add for CR01321593 end
        return null;
    }

    /**
     * reture true if there is external sd card in device and ALREADY SWAP.
     */
    public boolean isSDExist() {
//        IMountService mMountService;
//        mMountService = IMountService.Stub.asInterface(ServiceManager.getService("mount"));
//        if (mMountService == null) {
            return false;
//        }
//        try {
//            return true;// mMountService.isSDExist();
//        } catch (Exception e) {
//            return false;
//        }
    }

    /**
     * Returns default path for writing.
     */
    public String getDefaultPath() {
        return STORAGE_PATH_SD1;
    }
}
