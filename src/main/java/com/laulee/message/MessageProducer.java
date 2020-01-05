package com.laulee.message;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by laulee on 2020/1/4.
 */
public class MessageProducer {
    private final static String QUEUE_NAME = "ORIGIN_QUEUE";
    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        // 连接IP
        factory.setHost("127.0.0.1");
        // 连接端口
        factory.setPort(5672);
        // 虚拟机
        factory.setVirtualHost("/");
        // 用户
        factory.setUsername("guest");
        factory.setPassword("guest");
        // 建立连接
        Connection conn = factory.newConnection();
        // 创建消息通道
        Channel channel = conn.createChannel();

        Map<String,Object> headers = new HashMap(1);
        headers.put("name","laulee");

        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder()
                .deliveryMode(2)
                .contentEncoding("UTF-8")
                .expiration("100000")
                .headers(headers)   //自定义属性
                .priority(5)    //优先级，默认为5，配合队列的x-max-priority属性使用
                .build();

        String msg = "Hello world, Rabbit MQ2";

        // 声明队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        //发送消息
        channel.basicPublish("", QUEUE_NAME, properties, msg.getBytes());
        channel.close();
        conn.close();
    }
}
