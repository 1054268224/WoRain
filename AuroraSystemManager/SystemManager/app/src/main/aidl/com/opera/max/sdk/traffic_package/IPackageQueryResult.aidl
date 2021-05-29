package com.opera.max.sdk.traffic_package;

oneway interface IPackageQueryResult {

    /**
     *
     * @param jsonResult 以json格式返回的查询结果, 包含slotId，queryType, 运营商、流量查询结果等信息, 格式如下：
     * {
     *     slodId: $slot_id,
     *     queryType: $query_type
     *     errorCode: $error_code,
     *     errorMsg: $error_msg,
     *
     *     #queryType 为0，2时:
     *     queryId: $query_id,
     *     summary: {
     *         name: $name,
     *         total: $total,
     *         used: $used,
     *         left: $left
     *     }
     *
     *     #queryType 为1，2时:
     *     operatorInfo: {
     *         province:    $province,
     *         operator:    $operator,
     *         brand:       $brand,
     *         queryCode:   $query_code
     *         phoneNumber: $phone_number
     * }
     *
     * errorCode列表如下：
     *   0, 查询成功
     *   1, 无效的slotId, slotId应为0或1(双卡设备)，0(单卡设备)
     *   2, 未设置查询回调
     *   3, 该slotId的上次查询尚未结束
     *   4, 未设置省
     *   5, 未设置运营商品牌
     *   6, 未设置查询码
     *   7, 查询码发生变化
     *   8, 查询码不正确
     *   9, 网络错误
     *   10, 服务器错误
     *   11, 接收短信失败
     *   12, 发送短信失败
     *   13, 运营商繁忙
     *   14, SIM卡未就绪
     *   15, 未知错误
     *   16, 无效的查询类型
     *
     * total为整数，表示套餐总量，单位为KB，Integer.MIN_VALUE (-2147483648) 表示流量未知
     * used为整数，表示已使用流量，单位为KB，Integer.MIN_VALUE (-2147483648) 表示流量未知
     * left为整数，表示剩余流量，单位为KB，Integer.MIN_VALUE (-2147483648) 表示流量未知
     */
    void onQueryResult(String jsonResult);

    /*
     * @param slotId 为0或1(双卡设备)，0(单卡设备)
     * @param senderAddress 运营商地址
     * @param messageBody 运营商发的流量短信
     * @param timestampMillis 时间戳
     */
    void onReceivePackageQueryMessage(int slotId, String senderAddress, String messageBody, long timestampMillis);
}