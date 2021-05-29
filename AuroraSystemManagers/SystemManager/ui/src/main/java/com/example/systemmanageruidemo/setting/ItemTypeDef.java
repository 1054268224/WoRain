package com.example.systemmanageruidemo.setting;

public class ItemTypeDef {

    public static final int ITEM_TYPE_1 = 1;
    public static final int ITEM_TYPE_2 = 2;
    public static final int ITEM_TYPE_3 = 3;
    public static final int ITEM_TYPE_4 = 4;
    public static final int ITEM_TYPE_5 = 5;

    public enum Type {
        ITEM1(ITEM_TYPE_1),
        ITEM2(ITEM_TYPE_2),
        ITEM3(ITEM_TYPE_3),
        ITEM4(ITEM_TYPE_4),
        ITEM5(ITEM_TYPE_5);
        int code;

        Type(int code) { this.code = code; }

        public int getCode() {
            return code;
        }

        public static Type getItemType(int code){
            switch (code) {
                case ITEM_TYPE_1:
                    return Type.ITEM1;
                case ITEM_TYPE_2:
                    return Type.ITEM2;
                case ITEM_TYPE_3:
                    return Type.ITEM3;
                case ITEM_TYPE_4:
                    return Type.ITEM4;
                case ITEM_TYPE_5:
                    return Type.ITEM5;
            }
            return Type.ITEM1;
        }
    }
}
