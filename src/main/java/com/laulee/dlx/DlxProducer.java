package com.laulee.dlx;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Created by laulee on 2020/1/4.
 * 消息生产者，通过TTL测试死信队列
 */
public class DlxProducer {

    private final static String DLX_QUEUE = "DLX_QUEUE";
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
        String msg = "Hello world, Rabbit MQ，DLX MSG";
        // 声明队列
        channel.queueDeclare(DLX_QUEUE, false, false, false, null);

        //设置属性，消息10秒过期
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder()
                .deliveryMode(2)  //消息持久化
                .contentEncoding("UTF-8")
                .expiration("10000")
                .build();
        //发送消息
        for (int i = 0; i < 10; i++) {
            channel.basicPublish("", DLX_QUEUE, null, msg.getBytes());
        }
        channel.close();
        conn.close();
    }
}
