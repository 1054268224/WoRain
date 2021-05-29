package com.cydroid.softmanager.utils;

/**
 * Created by zhaocaili on 18-8-2.
 * Modify by HZH on 2019/6/14 for EJSL-1528 start
 */

public class MemoryFormatterUtils {

    public static long translateCapacity(long capacity) {
        final long M = 1000 * 1000;
        long result = capacity;
        if (capacity < 67108864L) {
            result = 64 * M;          // 64MB
        } else if (capacity < 134217728L) {
            result = 128 * M;         // 128MB
        } else if (capacity < 268435456L) {
            result = 256 * M;         // 256MB
        } else if (capacity < 536870912L) {
            result = 512 * M;         // 512MB
        } else if (capacity < 1073741824L) {
            result = 1000 * M;        // 1GB
        } else if (capacity < 1610612736L) {
            result = 1500 * M;        // 1.5GB
        } else if (capacity <= 2147483648L) {
            result = 2 * 1000 * M;    // 2GB
        } else if (capacity <= 3221225472L) {
            result = 3 * 1000 * M;    // 3GB
        } else if (capacity <= 4294967296L) {
            result = 4 * 1000 * M;    // 4GB
        } else if (capacity <= 6442450944L) {
            result = 6 * 1000 * M;    // 6GB
        } else if (capacity <= 8589934592L) {
            result = 8 * 1000 * M;    // 8GB
        } else if (capacity <= 17179869184L) {
            result = 16 * 1000 * M;   // 16GB
        } else if (capacity <= 32000000000L) {
            result = 32 * 1000 * M;   // 32GB
        }
        return result;
    }
}
