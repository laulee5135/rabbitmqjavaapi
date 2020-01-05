package com.laulee.limit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Created by laulee on 2020/1/4.
 */
public class LimitProducer {

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
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        for (int i = 0; i < 100; i++) {
            channel.basicPublish("", QUEUE_NAME, null, (msg+i).getBytes());
        }
        channel.close();
        conn.close();
    }

}
