package com.laulee.transaction;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Created by laulee on 2020/1/5.
 * transaction模式效率低
 * 消息可靠性之服务端的Transition模式
 */
public class TreansactionProducer {

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

        //事务模式可通过wireshark工具抓包，查看交互的过程：建立连接、开启事务、提交事务等
        try {
            //将channel设置成事物模式
            channel.txSelect();
            //发送消息
            channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());
            //提交事务
            channel.txCommit();
            System.out.println("消息发送成功");
        }catch (Exception e){
            //事务回滚
            channel.txRollback();
            System.out.println("事物已经回滚");
        }

        channel.close();
        conn.close();
    }
}
