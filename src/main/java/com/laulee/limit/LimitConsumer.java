package com.laulee.limit;

import com.rabbitmq.client.*;

import java.io.IOException;

/**
 * Created by laulee on 2020/1/4.
 */
public class LimitConsumer {

    private final static String QUEUE_NAME = "ORIGIN_QUEUE1";
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
        final Channel channel = conn.createChannel();

        // 声明队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" consumer1 Waiting for message....");

        // 创建消费者,并接收消息
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, "UTF-8");

                System.out.println("Received message : '" + msg + "'");
                channel.basicAck(envelope.getDeliveryTag(),true);
            }
        };
        //非自动确认消息的前提下，如果一定数目的消息，（通过基于consumer或者channel设置Qos的值）未被确认前，不进行消费新的消息
        //因为consumer1的处理速率很慢，收到两条消息后都没有发送ack,队列不会再发消息给consumer1
        channel.basicQos(2);  //只有等2条消息ack之后，才会接收下一个消息；比如，消费端可以设置为1，就会一条消息处理完了之后，再消费下一个消息
        channel.basicConsume(QUEUE_NAME, false, consumer);
    }

}
