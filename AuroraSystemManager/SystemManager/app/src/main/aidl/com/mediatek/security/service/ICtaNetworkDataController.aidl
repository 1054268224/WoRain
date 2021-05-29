package com.mediatek.security.service;

import com.mediatek.security.service.IRequestNetworkPermissionCallback;

interface ICtaNetworkDataController {
    /**
     * Request network permission for the calling package
     *
     * @param callback  the callback after user confirm.
     * @return          true success get access internet permission,
     *                  false get permission fail,will inform user to confirm,
     *                  After user confirm, callback will be called.
     */
    boolean requestNetworkPermission(in IRequestNetworkPermissionCallback callback);

     /**
     * Request network permission for some packageNames at once
     *
     * @param packageNames  the name of packages caller want to request permission at once.
     * @param callback      the callback after user confirm.
     * @return              true success get access internet permission,
     *                      false get permission fail,will inform user to confirm,
     *                      After user confirm, callback will be called.
     */
    boolean requestNetworkPermissions(in String[] packageNames,
        in IRequestNetworkPermissionCallback callback);
}
