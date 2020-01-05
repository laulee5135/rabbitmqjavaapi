package com.laulee.confirm;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Created by laulee on 2020/1/5.
 *
 * 消息可靠性之服务端的Confirm模式--单条消息确认
 */
public class NormalConfirmProducer {

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

        //通过wireshark抓包可见，客户端发送开启Confirm请求，服务端返回Confirm.select-ok, 客户端发送消息，服务端返回Ack
        //开启发送方确认模式
        channel.confirmSelect();
        channel.basicPublish("",QUEUE_NAME,null,msg.getBytes());
        //普通的Confirm，发送一条，确认一条
        if(channel.waitForConfirms()){
            //消息发送成功
            System.out.println("消息发送成功");
        }

        channel.close();
        conn.close();
    }
}
