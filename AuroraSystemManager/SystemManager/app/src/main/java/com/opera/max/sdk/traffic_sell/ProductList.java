package com.opera.max.sdk.traffic_sell;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class ProductList implements Parcelable {
    public String operator; //the telecom operator name, Chinese
    public String province; //the province that the phone belong
    public List<Product> products = new ArrayList<Product>(); //product list

    public static class Product implements Parcelable {
        public String product_id; // product id
        public String title; // product title
        public String desc; //product description
        public double original_price; //product original price , show the discount value
        public double price; //the price to sell
        @Override
        public int describeContents() {
            return 0;
        }
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(product_id);
            dest.writeString(title);
            dest.writeString(desc);
            dest.writeDouble(original_price);
            dest.writeDouble(price);
        }
        public Product(){

        }
        public Product(Parcel in) {
            product_id = in.readString();
            title = in.readString();
            desc = in.readString();
            original_price = in.readDouble();
            price = in.readDouble();
        }
        public static final Parcelable.Creator<Product> CREATOR = new Parcelable.Creator<Product>() {
            @Override
            public Product createFromParcel(Parcel in) {
                return new Product(in);
            }

            @Override
            public Product[] newArray(int size) {
                return new Product[size];
            }
        };
    }

    public static final Parcelable.Creator<ProductList> CREATOR = new Parcelable.Creator<ProductList>() {
        @Override
        public ProductList createFromParcel(Parcel in) {
            return new ProductList(in);
        }

        @Override
        public ProductList[] newArray(int size) {
            return new ProductList[size];
        }
    };

    public ProductList() {
    }

    public ProductList(Parcel in) {
        operator = in.readString();
        province = in.readString();
        in.readTypedList(products, Product.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(operator);
        dest.writeString(province);
        dest.writeTypedList(products);
    }
}
