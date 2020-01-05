package com.laulee.confirm;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Created by laulee on 2020/1/5.
 * 消息可靠性之服务端的Confirm模式--批量消息确认
 */
public class BatchConfirmProducer {

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

        try {
            //开启发送方确认模式
            channel.confirmSelect();
            for (int i = 0; i < 10; i++) {
                channel.basicPublish("",QUEUE_NAME,null,msg.getBytes());
            }
            //批量确认结果
            //直到所有嘻嘻都发布，只要有一个未被Broker确认就会IOException
            channel.waitForConfirmsOrDie();
            System.out.println("消息发送完毕，批量确认成功");
        }catch (Exception e){
            //发生异常，可能需要对所有的消息进行重发
            e.printStackTrace();
        }


        channel.close();
        conn.close();
    }
}
