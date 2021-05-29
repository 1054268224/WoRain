package com.cydroid.framework;

public interface IGnStorageManager {
    String getVolumeState(String mountPoint);

    String getGnAvailableExternalStoragePath(long requireSize);

    boolean isSDExist();

    String getDefaultPath();
}
