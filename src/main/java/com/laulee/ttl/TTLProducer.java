package com.laulee.ttl;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by laulee on 2019-12-24.
 */
public class TTLProducer {

    private final static String TEST_TTL_QUEUE = "TEST_TTL_QUEUE";
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

        //通过队列属性设置消息过期时间  疑问：过期之后的消息去了哪里？
        Map<String, Object> argss = new HashMap<String, Object>();
        argss.put("x-message-ttl",60000);
        //声明队列（默认交换机 Direct）
        channel.queueDeclare(TEST_TTL_QUEUE, false, false, false, argss);

        //对每条消息设置过期时间
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                .deliveryMode(2)
                .contentEncoding("UTF-8")
                .expiration("10000")   //TTL
                .build();

        // ⚠️⚠️⚠️️ 此处两种方式设置消息过期时间的方式都使用了，将以较小的数值为准

        //发送消息
        channel.basicPublish("", TEST_TTL_QUEUE, properties, msg.getBytes());
        channel.close();
        conn.close();
    }


}
