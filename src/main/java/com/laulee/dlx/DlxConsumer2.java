package com.laulee.dlx;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by laulee on 2020/1/4.
 * 消息消费者，由于消费者的代码被注释掉了，10秒后，消息会从正常队列TEST_DLX_QUEUE到达死信交换机DLX_EXCHANGE,然后由死信队列DLX_QUEUE消费(DlxConsumer2)
 */
public class DlxConsumer2 {

    private final static String DLX_QUEUE = "DLX_QUEUE";
    private final static String DLX_EXCHANGE = "DLX_EXCHNAGE";
    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        // 连接IP
        factory.setHost("127.0.0.1");
        // 默认监听端口
        factory.setPort(5672);
        // 虚拟机
        factory.setVirtualHost("/");
        // 设置访问的用户
        factory.setUsername("guest");
        factory.setPassword("guest");

        // 建立连接
        Connection conn = factory.newConnection();
        // 创建消息通道
        Channel channel = conn.createChannel();

        //声明队列（默认交换机 Direct）
        channel.queueDeclare(DLX_QUEUE, false, false, false, null);

        //声明死信交换机
        channel.exchangeDeclare(DLX_EXCHANGE, "topic", false, false, null);

        //绑定，此处Dead letter routing key 设置为 #  （#多个单词或0个单词）
        channel.queueBind(DLX_QUEUE, DLX_EXCHANGE, "#");
        System.out.println("waiting for message ......");

        // 创建消费者
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, "UTF-8");
                System.out.println("Received message : '" + msg + "'");
            }
        };
        // 开始获取消息
        // String queue, boolean autoAck, Consumer callback
        channel.basicConsume(DLX_QUEUE, true, consumer);
    }
}
