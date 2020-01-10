package com.laulee.simple;

import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Created by laulee on 2020-01-01.
 */
public class MyProducer {
    private final static String QUEUE_NAME = "ORIGIN_QUEUE1";
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
        Connection conn = factory.newConnection(); // 创建消息通道
        Channel channel = conn.createChannel();
        String msg = "Hello world, Rabbit MQ2";
        // 声明队列
        /**
         * boolean durable:是否持久化，代表队列在服务器重启后是否还存在。
         * boolean exclusive:是否排他性队列。排他性队列只能在声明它的Connection中使用，连接断开时自动删除。
         * boolean autoDelete:是否自动删除。如果为true，至少有一个消费者连接到这个队列，之后所有与这个队列连接 的消费者都断开时，队列会自动删除。
         * Map<String, Object> arguments:队列的其他属性，例如x-message-ttl、x-expires、x-max-length、x-max- length-bytes、x-dead-letter-exchange、x-dead-letter-routing-key、x-max-priority。
         */
        // String queue, boolean durable, boolean exclusive, boolean autoDelete,Map<String, Object> arguments
//        Map<String, Object> argss = new HashMap<String, Object>();
//        argss.put("x-message-ttl",6000);
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        // 发送消息(发送到默认交换机AMQP Default，Direct)
        // 如果有一个队列名称跟Routing Key相等，那么消息会路由到这个队列
        // String exchange, String routingKey, BasicProperties props, byte[] body
        channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());
        channel.close();
        conn.close();
    }
}