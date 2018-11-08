package com.pyx.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

/**
 * @author: yingxu.pi@transsnet.com
 * @date: 2018/11/7 17:32
 * 通过实现 ConfirmCallback 接口，消息发送到 Broker 后触发回调，确认消息是否到达 Broker 服务器，也就是只确认是否正确到达Exchange中,比如指定的Exchange不存在就会触发
 */
@Component
public class RabbitConfirmCallBack implements RabbitTemplate.ConfirmCallback {

    private Logger logger = LoggerFactory.getLogger(RabbitConfirmCallBack.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);            //指定 ConfirmCallback
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        logger.info("消息唯一标识={}, 确认结果={}, 失败原因={}", correlationData, ack, cause);
    }

}
