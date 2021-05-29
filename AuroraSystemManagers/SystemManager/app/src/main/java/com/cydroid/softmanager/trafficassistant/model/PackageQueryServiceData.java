//Gionee: mengdw <2015-11-11> add for CR01589343 begin
package com.cydroid.softmanager.trafficassistant.model;

import java.io.Serializable;

public class PackageQueryServiceData implements Serializable {
    private static final long serialVersionUID = 1L;
    private int mSlotIndex;
    private int mQueryType;
    private int mErrorCode;
    private String mErrorMsg;
    private String mQueryID;
    private int mCommonTotal;
    private int mCommonUsed;
    private int mCommonLeft;
    private boolean mHasIdleData;
    private int mIdleTotal;
    private int mIdleUsed;
    private int mIdleLeft;
    private String mProvince;
    private String mOperator;
    private String mBrand;
    private String mQueryCode;
    private String mPhoneNumber;
    private boolean isCommonTotalInvalid;
    private boolean isCommonUsedInvalid;
    private boolean isCommonLeftInvalid;
    private boolean isIdleTotalInvalid;
    private boolean isIdleUsedInvalid;
    private boolean isIdleLeftInvalid;
    
    public void setSlotIndex(int slotIndex) {
        mSlotIndex = slotIndex;
    }
    
    public int getSlotIndex() {
        return mSlotIndex;
    }
    
    public void setQueryType(int queryType) {
        mQueryType = queryType;
    }
    
    public int getQueryType() {
        return mQueryType;
    }
    
    public void setErrorCode(int errorCode) {
        mErrorCode = errorCode;
    }
    
    public int getErrorCode() {
        return mErrorCode;
    }
    
    public void setErrorMsg(String errorMsg) {
        mErrorMsg = errorMsg;
    }
    
    public String getErrorMsg() {
        return mErrorMsg;
    }
    
    public void setQueryID(String queryId) {
        mQueryID = queryId;
    }
    
    public String getQueryID() {
        return mQueryID;
    }
    
    public void setCommonTotal(int commonTotal) {
        mCommonTotal = commonTotal;
    }
    
    public int getCommonTotal() {
        return mCommonTotal;
    }
    
    public void setCommonUsed(int commonUsed) {
        mCommonUsed = commonUsed;
    }
    
    public int getCommonUsed() {
        return mCommonUsed;
    }
    
    public void setCommonLeft(int commonLeft) {
        mCommonLeft = commonLeft;
    }
    
    public int getCommonLeft() {
        return mCommonLeft;
    }
    
    public void setHasIdleData(boolean hasIdleData) {
        mHasIdleData = hasIdleData;
    }
    
    public boolean isHasIdleData() {
        return mHasIdleData;
    }
    
    public void setIdleTotal(int idleTotal) {
        mIdleTotal = idleTotal;
    }
    
    public int getIdleTotal() {
        return mIdleTotal;
    }
    
    public void setIdleUsed(int idleUsed) {
        mIdleUsed = idleUsed;
    }
    
    public int getIdleUsed() {
        return mIdleUsed;
    }
    
    public void setIdleLeft (int idleLeft) {
        mIdleLeft = idleLeft;
    }
    
    public int getIdleLeft() {
        return mIdleLeft;
    }
    
    public void setProvince(String province) {
        mProvince = province;
    }
    
    public String getProvince() {
        return mProvince;
    }
    
    public void setOperator(String operator) {
        mOperator = operator;
    }
    
    public String getOperator() {
        return mOperator;
    }
    
    public void setBrand(String brand) {
        mBrand = brand;
    }
    
    public String getBrand() {
        return mBrand;
    }
    
    public void setQueryCode(String queryCode) {
        mQueryCode = queryCode;
    }
    
    public String getQueryCode() {
        return mQueryCode;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }
    
    public String getPhoneNumber() {
        return mPhoneNumber;
    }
    
    public void setCommonTotalInvalid(boolean isInvalid) {
        isCommonTotalInvalid = isInvalid;
    }
    
    public boolean isCommonTotalInvalid() {
        return isCommonTotalInvalid;
    }
    
    public void setCommonUsedInvalid(boolean isInvalid) {
        isCommonUsedInvalid = isInvalid;
    }
    
    public boolean isCommonUsedInvalid() {
        return isCommonUsedInvalid;
    }
    
    public void setCommonLeftInvalid(boolean isInvalid) {
        isCommonLeftInvalid = isInvalid;
    }
    
    public boolean isCommonLeftInvalid() {
        return isCommonLeftInvalid;
    }
    
    public void setIdleTotalInvalid(boolean isInvalid) {
        isIdleTotalInvalid = isInvalid;
    }
    
    public boolean isIdleTotalInvalid() {
        return isIdleTotalInvalid;
    }
    
    public void setIdleUsedInvalid(boolean isInvalid) {
        isIdleUsedInvalid = isInvalid;
    }
    
    public boolean isIdleUsedInvalid() {
        return isIdleUsedInvalid;
    }
    
    public void setIdleLeftInvalid(boolean isInvalid) {
        isIdleLeftInvalid = isInvalid;
    }
    
    public boolean isIdleLeftInvalid() {
        return isIdleLeftInvalid;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("mSlotIndex=" + mSlotIndex);
        sb.append(" mQueryType=" + mQueryType);
        sb.append(" mErrorCode=" + mErrorCode);
        sb.append(" mErrorMsg=" + mErrorMsg);
        sb.append(" mQueryID=" + mQueryID);
        sb.append(" mCommonTotal=" + mCommonTotal);
        sb.append(" mCommonUsed=" + mCommonUsed);
        sb.append(" mCommonLeft=" + mCommonLeft);
        sb.append(" mHasIdleData=" + mHasIdleData);
        sb.append(" mIdleTotal=" + mIdleTotal);
        sb.append(" mIdleUsed=" + mIdleUsed);
        sb.append(" mIdleLeft=" + mIdleLeft);
        sb.append(" mProvince=" + mProvince);
        sb.append(" mOperator=" + mOperator);
        sb.append(" mBrand=" + mBrand);
        sb.append(" mQueryCode=" + mQueryCode);
        sb.append(" mPhoneNumber=" + mPhoneNumber);
        sb.append(" isCommonTotalInvalid=" + isCommonTotalInvalid);
        sb.append(" isCommonUsedInvalid=" + isCommonUsedInvalid);
        sb.append(" isCommonLeftInvalid=" + isCommonLeftInvalid);
        sb.append(" isIdleTotalInvalid=" + isIdleTotalInvalid);
        sb.append(" isIdleUsedInvalid=" + isIdleUsedInvalid);
        sb.append(" isIdleLeftInvalid=" + isIdleLeftInvalid);
        return sb.toString();
    }
}
//Gionee: mengdw <2015-11-11> add for CR01589343 end