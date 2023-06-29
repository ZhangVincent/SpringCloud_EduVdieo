package com.eduvideo.orders.service;

import com.eduvideo.orders.model.dto.AddOrderDto;
import com.eduvideo.orders.model.dto.PayRecordDto;
import com.eduvideo.orders.model.dto.PayStatusDto;
import com.eduvideo.orders.model.po.XcPayRecord;

/**
 * @author zkp15
 * @version 1.0
 * @description 订单service接口类
 * @date 2023/6/27 20:58
 */
public interface OrderService {

    /***
    * @description 创建订单和交易记录，生成二维码并返回
    * @param userId
     * @param addOrderDto
    * @return com.eduvideo.orders.model.dto.PayRecordDto
    * @author zkp15
    * @date 2023/6/27 20:59
    */
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);

    /***
    * @description 查询支付记录
    * @param payNo
    * @return com.eduvideo.orders.model.po.XcPayRecord
    * @author zkp15
    * @date 2023/6/27 21:53
    */
    public XcPayRecord getPayRecordByPayno(String payNo);

    /***
    * @description 支付成功，更新支付记录表和订单表的状态
    * @param payStatusDto
    * @return void
    * @author zkp15
    * @date 2023/6/28 10:47
    */
    public void saveAliPayStatus(PayStatusDto payStatusDto);
}
