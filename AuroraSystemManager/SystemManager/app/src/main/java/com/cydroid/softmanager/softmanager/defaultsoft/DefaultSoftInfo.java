package com.cydroid.softmanager.softmanager.defaultsoft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultSoftInfo {
    private DefaultSoftResolveInfo mBestMatch;
    private final List<DefaultSoftResolveInfo> mAllMatch = new ArrayList<DefaultSoftResolveInfo>();
    private final Map<String, DefaultSoftResolveInfo> mMapEntries = new HashMap<String, DefaultSoftResolveInfo>();

    public DefaultSoftInfo() {
    }

    public DefaultSoftInfo(DefaultSoftInfo source) {
        mBestMatch = source.mBestMatch;
        mAllMatch.addAll(source.mAllMatch);
        mMapEntries.putAll(source.mMapEntries);
    }

    public DefaultSoftResolveInfo getBestMatch() {
        return mBestMatch;
    }

    public void setBestMatch(DefaultSoftResolveInfo bestMatch) {
        mBestMatch = bestMatch;
    }

    public List<DefaultSoftResolveInfo> getMatches() {
        return mAllMatch;
    }

    public void addMatch(DefaultSoftResolveInfo info) {
        if (!mAllMatch.contains(info)) {
            mAllMatch.add(info);
        }
    }

    public void addEmptyMatch(DefaultSoftResolveInfo info) {
        mAllMatch.add(0, info);
    }

    public void removeMatch(DefaultSoftResolveInfo info) {
        mAllMatch.remove(info);
    }

    public DefaultSoftResolveInfo getByUnique(String unique) {
        return mMapEntries.get(unique);
    }

    public void addToMapEntry(DefaultSoftResolveInfo resolveInfo) {
        mMapEntries.put(resolveInfo.getUnique(), resolveInfo);
    }

    public void removeFromMapEntry(DefaultSoftResolveInfo resolveInfo) {
        mMapEntries.remove(resolveInfo.getUnique());
    }

    public void clear() {
        mAllMatch.clear();
        mMapEntries.clear();
    }
}
