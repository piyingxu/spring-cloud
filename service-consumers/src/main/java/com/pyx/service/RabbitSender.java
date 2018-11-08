package com.pyx.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: yingxu.pi@transsnet.com
 * @date: 2018/11/7 13:45
 */
@Component
public class RabbitSender {

    private Logger logger = LoggerFactory.getLogger(RabbitSender.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(String msg) {
        logger.info("发送了一条消息:{}", msg);
        CorrelationData data = new CorrelationData(); //相关回调数据传递，标记消息的唯一性
        data.setId(String.valueOf(System.currentTimeMillis()));
        //简单模式，不指定exchange
        rabbitTemplate.convertAndSend("","transsnet_queue_simple", msg, data); //在不指定exchange的情况下，routingKey将默认作为队列名称，发送到这个队列中去, 如果这个队列不存在则这个消息丢失
        /*
        //fanout模式
        rabbitTemplate.convertAndSend("transsnet_fanout", "", msg, data);
        //direct模式
        rabbitTemplate.convertAndSend("transsnet_direct", "update", msg, data);
        //topic模式
        rabbitTemplate.convertAndSend("transsnet_topic", "addOrder", msg, data);
        */

    }
}
