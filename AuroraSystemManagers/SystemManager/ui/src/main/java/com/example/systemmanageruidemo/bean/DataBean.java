package com.example.systemmanageruidemo.bean;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

public class DataBean {

    public static final int PARENT_ITEM = 0;
    public static final int CHILD_ITEM = 1;

    private int type=PARENT_ITEM;
    private boolean isExpand;
    private Boolean isParentTrue;

    private String parentLeftTxt;
    private long parentRightTxt;
    List<DataBeanChild> children=new ArrayList<>();

    public DataBean(String parentLeftTxt, long parentRightTxt,Boolean isParentTrue) {
        this.isParentTrue = isParentTrue;
        this.parentLeftTxt = parentLeftTxt;
        this.parentRightTxt = parentRightTxt;
    }

    public List<DataBeanChild> getChildren() {
        return children;
    }

    public void setChildren(List<DataBeanChild> children) {
        this.children = children;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
    }

    public Boolean getParentTrue() {
        return isParentTrue;
    }

    public void setParentTrue(Boolean parentTrue) {
        isParentTrue = parentTrue;
    }

    public boolean isParentTrue() {
        return isParentTrue;
    }

    public void setParentTrue(boolean parentTrue) {
        isParentTrue = parentTrue;
    }


    public String getParentLeftTxt() {
        return parentLeftTxt;
    }

    public void setParentLeftTxt(String parentLeftTxt) {
        this.parentLeftTxt = parentLeftTxt;
    }

    public long getParentRightTxt() {
        return parentRightTxt;
    }

    public void setParentRightTxt(long parentRightTxt) {
        this.parentRightTxt = parentRightTxt;
    }

    public static class DataBeanChild {
        private Boolean isChildTrue;
        private String childLeftTxt;
        private long childRightTxt;
        private int type=CHILD_ITEM;
        private Drawable imageId;
        public DataBean parent;
        private boolean isHide=true;
        long id;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public DataBean getParent() {
            return parent;
        }

        public void setParent(DataBean parent) {
            this.parent = parent;
        }

        public DataBeanChild(){}

        public DataBeanChild(Drawable imageId, String childLeftTxt, long childRightTxt, Boolean isChildTrue) {
            this.isChildTrue = isChildTrue;
            this.childLeftTxt = childLeftTxt;
            this.childRightTxt = childRightTxt;
            this.imageId = imageId;
        }

        public Drawable getImageId() {
            return imageId;
        }

        public void setImageId(Drawable imageId) {
            this.imageId = imageId;
        }

        public boolean isHide() {
            return isHide;
        }

        public void setHide(boolean hide) {
            isHide = hide;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public Boolean getChildTrue() {
            return isChildTrue;
        }

        public void setChildTrue(Boolean childTrue) {
            isChildTrue = childTrue;
        }


        public String getChildLeftTxt() {
            return childLeftTxt;
        }

        public void setChildLeftTxt(String childLeftTxt) {
            this.childLeftTxt = childLeftTxt;
        }

        public long getChildRightTxt() {
            return childRightTxt;
        }

        public void setChildRightTxt(long childRightTxt) {
            this.childRightTxt = childRightTxt;
        }
    }
}
