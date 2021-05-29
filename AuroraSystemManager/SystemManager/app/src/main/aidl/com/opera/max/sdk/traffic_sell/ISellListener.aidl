package com.opera.max.sdk.traffic_sell;

import com.opera.max.sdk.traffic_sell.ProductList;
import com.opera.max.sdk.traffic_sell.HistoryOrderList;

oneway interface ISellListener {
    /**
     * Called when get the product list from server.
     *
     * @param status:
     *     1000: success
     *     1002: failed
     * @param list: traffic product list
     */
    void onFetchProducts(int status, in ProductList list);

    /**
     * Called when the buy action end
     *
     * @param status:
     *     1000: pay success
     *     2000: pay ongoing
     *     2001: pay failed
     *     2002: pay canceled
     * @param trade_no: trade number
     */
    void onBuy(int status, String trade_no);

    /**
     * Called when get the history order from server
     *
     * @param status:
     *     1000: success
     *     1002: failed
     * @param historyList: history order list
     */
    void onFetchHistory(int status, in HistoryOrderList historyList);
}
