package com.cydroid.softmanager.utils;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.cydroid.softmanager.model.ItemInfo;

public class NameSorting {
    public static void sort(List<? extends ItemInfo> list) {
        Collections.sort(list, new Comparator<ItemInfo>() {
            @Override
            public int compare(ItemInfo lhs, ItemInfo rhs) {
                return Collator.getInstance(Locale.CHINESE).compare(lhs.getTitle(), rhs.getTitle());
            }
        });
    }
}
