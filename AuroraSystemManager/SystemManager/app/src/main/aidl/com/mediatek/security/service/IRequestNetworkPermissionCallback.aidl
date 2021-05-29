package com.mediatek.security.service;
/** {@hide} */
interface IRequestNetworkPermissionCallback {
    oneway void onRequestPermissionResult( int grantedResult);
}