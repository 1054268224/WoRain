package com.opera.max.sdk.traffic_sell;

import com.opera.max.sdk.traffic_sell.ISellListener;

interface ITrafficSellService {
    /**
     * send the request to fetch the traffic product list
     * @param phone: phone number to load
     * @return send request successfully
     */
    boolean fetchProducts(String phone);

    /**
     * send the request to buy the traffic product
     * @param
     *	phone: phone number to be load
     *  pay_type: alipay or weixin
     *  product_id: get the product id by the method fetchProducts()
     * @return send request successfully
     */
    boolean buyTraffic(String phone, String pay_type, String product_id);

    /**
     * send the request to get the history order information for the device
     */
    void fetchHistory();

    /**
     * register the listener. The result of the request that you have send will be returned through the listener
     * @param listener to be registered
     */
    void registerSellListener(ISellListener listener);

    /**
     * unregister the listener.
     * @param listener that you have registered
     */
    void unregisterSellListener(ISellListener listener);
}
