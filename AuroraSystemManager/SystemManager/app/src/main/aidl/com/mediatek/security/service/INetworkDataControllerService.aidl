package com.mediatek.security.service;

import java.util.List;

import com.mediatek.security.datamanager.CheckedPermRecord;

interface INetworkDataControllerService {

    /**
     * Modify status of one package
     *
     * @param checkedCellular package name, uid and status of the package.
     * @return                true modify success, false modify fail.
     */
    boolean modifyNetworkDateRecord(in CheckedPermRecord checkedCellular);

    /**
     * Get internet access permission of specified uer id.

     * @param uid The user id of which want to get the permission.
     * @return    CheckedPermRecord include package name, user id and status.
     */
    CheckedPermRecord getNetworkDataRecord(in int uid);

    /**
     * Gte all the package record list
     *
     * @return All the Permission record list recorded in network data controller.
     */
    List<CheckedPermRecord> getNetworkDataRecordList();
}
