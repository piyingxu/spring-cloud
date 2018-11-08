package com.pyx.service;

import com.pyx.control.WebController;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.Queue;

import java.io.IOException;

/**
 * @author: yingxu.pi@transsnet.com
 * @date: 2018/11/7 14:35
 */
@Component
public class RabbitReceiver {

    private Logger logger = LoggerFactory.getLogger(WebController.class);

    @Bean
    public Queue helloQueue() {
        return new Queue("transsnet_queue_simple");
    }

    @RabbitListener(queues = "transsnet_queue_simple")
    @RabbitHandler
    public void receiveSimple(@Payload String msg, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel) {
        try {
            logger.info("====================> receiveSimple msg={}, deliveryTag={}", msg, deliveryTag);
            channel.basicQos(2); //每次读取2条消息
            Thread.sleep(20000);//模拟任务处理耗时
            channel.basicAck(deliveryTag, false); //true 确认所有的消息，false只确认当前消息
            //channel.basicReject(deliveryTag, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*@RabbitListener(
        bindings = {
            @QueueBinding(
                value = @Queue(value = "transsnet_queue_fanout", durable = "false", autoDelete = "false", exclusive = "false"), //自定义一个transsnet_queue_fanout来接收Exchange=transsnet_fanout的消息
                exchange = @Exchange(value = "transsnet_fanout", type = ExchangeTypes.FANOUT)
            )
        }
    )
    @RabbitHandler
    public void receiveFanout(String msg) {
        logger.info("receiveFanout: " + msg);
    }

    @RabbitListener(
        bindings = {
            @QueueBinding(
                value = @Queue(value = "transsnet_queue_direct", durable = "false", autoDelete = "false", exclusive = "false"), //自定义一个transsnet_queue_direct来接收Exchange=transsnet_direct的消息
                exchange = @Exchange(value = "transsnet_direct", type = ExchangeTypes.DIRECT),
                key = "update" //只接收routingKey=add为前缀的消息
            )
        }
    )
    @RabbitHandler
    public void receiveDirect(String msg) {
        logger.info("receiveDirect: " + msg);
    }

    @RabbitListener(
        bindings = {
            @QueueBinding(
                value = @Queue(value = "transsnet_queue_topic", durable = "false", autoDelete = "false", exclusive = "false"), //自定义一个transsnet_queue_topic来接收Exchange=transsnet_topic的消息
                exchange = @Exchange(value = "transsnet_topic", type = ExchangeTypes.TOPIC),
                key = "add.#" //只接收routingKey=add为前缀的消息
            )
        }
    )
    @RabbitHandler
    public void receiveTopic(String msg) {
        logger.info("receiveTopic: " + msg);
    }
*/

}
