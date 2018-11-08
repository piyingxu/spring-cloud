package com.pyx.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author: yingxu.pi@transsnet.com
 * @date: 2018/11/7 18:28
 * 通过实现 ReturnCallback 接口，启动消息失败返回，比如路由不到队列时触发回调, 消息发送失败才调用
 */
@Component
public class RabitReturnCallback implements RabbitTemplate.ReturnCallback {

    private Logger logger = LoggerFactory.getLogger(RabbitConfirmCallBack.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init(){
        rabbitTemplate.setReturnCallback(this);             //指定 ReturnCallback
    }

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        logger.info("message={}, replyCode={}, replyText={}, exchange={}, routingKey={}", message, replyCode, replyText, exchange, routingKey);
    }

}
