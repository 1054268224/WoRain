package com.opera.max.sdk.traffic_package;

import com.opera.max.sdk.traffic_package.IPackageQueryResult;

interface IPackageQueryService {
    /**
     * 获取对应卡槽上sim卡所在的省份
     * @param slotId 为0或1(双卡设备)，0(单卡设备)
     * @return 设置的省份名称(如北京市返回值为"北京"，黑龙江省返回值为"黑龙江")或返回null(如果未设置)
     */
    String getProvince(int slotId);

    /**
     * 设置对应卡槽上sim卡所在的省份
     * @param slotId 为0或1(双卡设备)，0(单卡设备)
     * @param province 省份名称(如北京市值为"北京"，黑龙江省值为"黑龙江")
     * @return 若设置成功返回true, 否则返回false(非法值)
     */
    boolean setProvince(int slotId, String province);

    /**
     * 获取对应卡槽上sim卡所在的运营商品牌
     * @param slotId 为0或1(双卡设备)，0(单卡设备)
     * @return 设置的品牌，下列值之一："动感地带","全球通","神州行","联通2G","联通3G","联通4G","中国电信"或返回null(如果未设置)
     */
    String getOperatorBrand(int slotId);

    /**
     * 设置对应卡槽上sim卡所在的运营商品牌
     * @param slotId 为0或1(双卡设备)，0(单卡设备)
     * @param operatorBrand 只能设置为以下之一(不含标点符号)： "动感地带","全球通","神州行","联通2G","联通3G", "联通4G","中国电信"
     * @return 若设置成功返回true, 否则返回false(非法值)
     */
    boolean setOperatorBrand(int slotId, String operatorBrand);

    /**
     * 获取对应卡槽上sim卡所用的流量查询码
     * @param slotId 为0或1(双卡设备)，0(单卡设备)
     * @return 流量查询短信查询码，如"CXLL"，或返回null(表明该值需要用户输入)
     */
    String getQueryCode(int slotId);

    /**
     * 设置对应卡槽上sim卡所用的流量查询码
     * @param slotId 为0或1(双卡设备)，0(单卡设备)
     * @param queryCode 流量查询短信查询码，如"CXLL"
     * @return 若设置成功返回true, 否则返回false(非法值)
     */
    boolean setQueryCode(int slotId, String queryCode);

    /**
     * 设置查询服务的回调函数
     * @param queryCallback 查询结果回调
     */
    void setQueryListener(IPackageQueryResult queryCallback);

    /**
     * 查询对应卡槽上sim卡消耗的流量或运营商信息
     * @param slotId 为0或1(双卡设备)，0(单卡设备)
     * @param queryType 查询类型：
     * 0: 查询流量信息
     * 1: 查询运营商信息：含省份、品牌、查询指令
     * 2: 自动查询运营商及流量信息
     * @return 请求查询结果状态码：
     * 0: 请求成功
     * 1: 无效的slotId
     * 2: 未设置返回结果callback
     * 3: 该卡槽上次查询未返回(正在查询)
     * 4: 省信息为空，无法查询流量
     * 5: 运营商品牌为空，无法查询流量
     * 16: 无效的查询类型
     */
    int query(int slotId, int queryType);

    /**
     * 上报对应卡槽上sim卡不准确的流量查询结果
     * @param slotId 为0或1(双卡设备)，0(单卡设备)
     * @param queryId 查询结果唯一标识，由查询结果获得
     */
    void reportInaccurateQuery(int slotId, String queryId);

    /**
     * 仅供调试用！
     * 设置运营商发的流量短信，而后调用query()方法时，不再发送短信查询流量，而是直接解析该流量短信。
     * @param slotId 为0或1(双卡设备)，0(单卡设备)
     * @param messageBody 运营商发的流量短信
     *
     */
    void setPackageQueryMessageForDebug(int slotId, String messageBody);

    /**
     * 设置套餐使用情况
     * @param slotId 为0或1(双卡设备)，0(单卡设备)
     * @param totalInKb 套餐总量；单位：KB
     * @param totalUsedInKb 套餐已用；单位：KB
     * @param idleTotalInKb 闲时套餐总量；单位：KB
     * @param idelUsedInKb 闲时套餐已用；单位：KB
     * @param idleVisbile 是否显示闲时套餐；
     * @return 若设置成功返回true, 否则返回false
     */
    boolean setPackageUsageManually(int slotId, int totalInKb, int totalUsedInKb, int idleTotalInKb, int idelUsedInKb, boolean idleVisbile);

    /**
     * 获取套餐总量
     * @param slotId 为0或1(双卡设备)，0(单卡设备)
     * @return 套餐总量；单位：KB；如未设置或者其它错误返回：-1
     */
    int getPackageTotalInKb(int slotId);

    /**
     * 获取套餐已用
     * @param slotId 为0或1(双卡设备)，0(单卡设备)
     * @return 套餐已用；单位：KB；如未设置或者其它错误返回：-1
     */
    int getPackageUsedInKb(int slotId);

    /**
     * 获取闲时套餐总量
     * @param slotId 为0或1(双卡设备)，0(单卡设备)
     * @return 闲时套餐总量；单位：KB；如未设置或者其它错误返回：-1
     */
    int getIdlePackageTotalInKb(int slotId);

    /**
     * 获取闲时套餐已用
     * @param slotId 为0或1(双卡设备)，0(单卡设备)
     * @return 闲时套餐已用；单位：KB；如未设置或者其它错误返回：-1
     */
    int getIdlePackageUsedInKb(int slotId);

    /**
     * 是否需要显示闲时流量
     * @param slotId 为0或1(双卡设备)，0(单卡设备)
     * @return 如果显示显示流量返回true，否则返回false
     */
    boolean showIdlePackageUsage(int slotId);

    /**
     * 设置闲时流量的时间段
     * @param slotId 为0或1(双卡设备)，0(单卡设备)
     * @param startTime 启动计算闲时流量的时间；格式: HH:mm
     * @param endTime 结束计算闲时流量的时间；格式: HH:mm
     * @return 成功返回true，否则返回false
     */
    boolean setIdleTrafficTime(int slotId, String startTime, String endTime);
}

