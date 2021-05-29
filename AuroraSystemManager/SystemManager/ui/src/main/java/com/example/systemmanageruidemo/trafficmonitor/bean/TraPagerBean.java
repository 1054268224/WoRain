package com.example.systemmanageruidemo.trafficmonitor.bean;

import android.annotation.Nullable;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

public class TraPagerBean {
    int simCardScount;
    @Nullable
    List<SIMBean> list = new ArrayList<>();

    public int getSimCardScount() {
        return simCardScount;
    }

    public void setSimCardScount(int simCardScount) {
        this.simCardScount = simCardScount;
    }

    public List<SIMBean> getList() {
        return list;
    }

    public void setList(List<SIMBean> list) {
        this.list = list;
    }

    public static class SIMBean {
        long id;
        String name;
        float surplusFlow;
        float usedFlow;
        long traPack;
        boolean issetted;

        public boolean isIssetted() {
            return issetted;
        }

        public void setIssetted(boolean issetted) {
            this.issetted = issetted;
        }

        public SIMBean(long id, String name) {
            this.id = id;
            this.name = name;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        public float getSurplusFlow() {
            return surplusFlow;
        }

        public void setSurplusFlow(float surplusFlow) {
            this.surplusFlow = surplusFlow;
        }

        public float getUsedFlow() {
            return usedFlow;
        }

        public void setUsedFlow(float usedFlow) {

            this.usedFlow = usedFlow;
        }

        public long getTraPack() {
            return traPack;
        }

        public void setTraPack(long traPack) {
            this.traPack = traPack;
        }
    }
}
