package com.laulee.confirm;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;

/**
 * Created by laulee on 2020/1/5.
 * 消息可靠性之服务端的Confirm模式--异步消息确认
 */
public class AsyncConfirmProducer {

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

        //开启Confirm模式
        channel.confirmSelect();
        for (int i = 0; i < 10; i++) {
            //发送消息
            channel.basicPublish("",QUEUE_NAME,null,(msg+i).getBytes());
        }

        //异步监听确认和未确认的消息
        channel.addConfirmListener(new ConfirmListener() {
            public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                //如果true表示批量执行了deliveryTag这个值以前（小于deliveryTag）的所有消息，如果为false的话表示单条确认
                System.out.println(String.format("Broker 已确认消息，标识：%d, 多个消息：%b",deliveryTag,multiple));
            }

            public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                System.out.println("Broker未确认消息，标识" + deliveryTag);
            }
        });

        System.out.println("消息执行完成");
        channel.close();
        conn.close();
    }
}
