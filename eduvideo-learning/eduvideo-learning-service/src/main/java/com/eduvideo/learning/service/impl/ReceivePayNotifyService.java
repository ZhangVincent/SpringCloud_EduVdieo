package com.eduvideo.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.eduvideo.learning.config.PayNotifyConfig;
import com.eduvideo.learning.service.MyCourseTablesService;
import com.eduvideo.messagesdk.model.po.MqMessage;
import com.eduvideo.messagesdk.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @author zkp15
 * @version 1.0
 * @description 接收支付结果通知service
 * @date 2022/10/5 5:06
 */
@Slf4j
@Service
public class ReceivePayNotifyService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    MyCourseTablesService myCourseTablesService;

    //接收支付结果通知
//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(value = PayNotifyConfig.CHOOSECOURSE_PAYNOTIFY_QUEUE),
//            exchange = @Exchange(value = PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT, type = ExchangeTypes.FANOUT)
//
//    ))
    @RabbitListener(queues = PayNotifyConfig.CHOOSECOURSE_PAYNOTIFY_QUEUE)
    public void receive(String message) {
        //获取消息
        MqMessage mqMessage = JSON.parseObject(message, MqMessage.class);
        log.debug("学习中心服务接收支付结果:{}", mqMessage);

        //消息类型
        String messageType = mqMessage.getMessageType();
        //订单类型,60201表示购买课程
        String businessKey2 = mqMessage.getBusinessKey2();
        //这里只处理支付结果通知
        if (PayNotifyConfig.MESSAGE_TYPE.equals(messageType) && "60201".equals(businessKey2)) {
            //获取选课记录id
            String choosecourseId = mqMessage.getBusinessKey1();

            //添加选课
            boolean b = myCourseTablesService.saveChooseCourseStauts(choosecourseId);
            if (b) {
                //向订单服务回复
                send(mqMessage);
            }
        }

    }

    /**
     * @param message 回复消息
     * @return void
     * @description 回复消息
     * @author zkp15
     * @date 2022/9/20 9:43
     */
    public void send(MqMessage message) {
        //转json
        String msg = JSON.toJSONString(message);
        // 发送消息
        rabbitTemplate.convertAndSend(PayNotifyConfig.PAYNOTIFY_REPLY_QUEUE, msg);
        log.debug("学习中心服务向订单服务回复消息:{}", message);
    }


}
