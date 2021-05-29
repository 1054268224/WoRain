package com.opera.max.sdk.traffic_sell;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class HistoryOrderList implements Parcelable {
    public List<OrderInfo> list = new ArrayList<OrderInfo>();

    public static class OrderInfo  implements Parcelable {
        public String phone;  //phone number
        public String title; //product title
        public String desc; //product description
        public double price; // product price to sell
        public String status; // order status description
        public int status_code; //order status code
        public String trade_no; //trade number
        public String pay_type; // pay type ,just one of weixin and alipay
        public String timestamp; //order pay time, yyyymmddhh24miss
        public String update_timestamp; //order time last updated, yyyymmddhh24miss
        @Override
        public int describeContents() {
            return 0;
        }
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(phone);
            dest.writeString(title);
            dest.writeString(desc);
            dest.writeDouble(price);
            dest.writeString(status);
            dest.writeInt(status_code);
            dest.writeString(trade_no);
            dest.writeString(pay_type);
            dest.writeString(timestamp);
            dest.writeString(update_timestamp);
        }

        public OrderInfo(){

        }
        public OrderInfo(Parcel in){
            phone = in.readString();
            title = in.readString();
            desc = in.readString();
            price = in.readDouble();
            status = in.readString();
            status_code = in.readInt();
            trade_no = in.readString();
            pay_type = in.readString();
            timestamp = in.readString();
            update_timestamp = in.readString();
        }
        public static final Parcelable.Creator<OrderInfo> CREATOR = new Parcelable.Creator<OrderInfo>() {
            @Override
            public OrderInfo createFromParcel(Parcel in) {
                return new OrderInfo(in);
            }

            @Override
            public OrderInfo[] newArray(int size) {
                return new OrderInfo[size];
            }
        };
    }

    public static final Parcelable.Creator<HistoryOrderList> CREATOR = new Parcelable.Creator<HistoryOrderList>() {
        @Override
        public HistoryOrderList createFromParcel(Parcel in) {
            return new HistoryOrderList(in);
        }

        @Override
        public HistoryOrderList[] newArray(int size) {
            return new HistoryOrderList[size];
        }
    };

    public HistoryOrderList() {
    }

    public HistoryOrderList(Parcel in) {
        in.readTypedList(list, OrderInfo.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(list);
    }
}
