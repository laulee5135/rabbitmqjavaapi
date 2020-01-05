package com.laulee.ack;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by laulee on 2020/1/4.
 */
public class AckConsumer {

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
        // String queue, boolean durable, boolean exclusive, boolean autoDelete,Map<String, Object> arguments
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" Waiting for message....");

        // 创建消费者
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, "UTF-8");
                System.out.println("Received message : '" + msg + "'");
                if(msg.contains("拒绝")){
                    //拒绝消息
                    //requeue: 是否重新入队列，true 是，false直接丢弃，相当于告诉队列可以直接删除掉了
                    //requeue 为true的时候会造成消息重复消费
                    channel.basicReject(envelope.getDeliveryTag(),false);      //第一个参数：发布的每一条消息都会获得一个唯一的deliveryTag，(任何channel上发布的第一条消息的deliveryTag为1，此后的每一条消息都会加1)，deliveryTag在channel范围内是唯一的
                }else if(msg.contains("异常")){
                    //不应答
                    channel.basicNack(envelope.getDeliveryTag(),false,false); //第二个参数：批量确认标志。如果值为true，则执行批量确认，此deliveryTag之前收到的消息全部进行确认; 如果值为false，则只对当前收到的消息进行确认
                }else{
                    //手动应答
                    //如果不应答，队列中的消息回你一直存在，重新连接的时候会重复消费
                    channel.basicAck(envelope.getDeliveryTag(),false);
                }
            }
        };
        // 开始获取消息, 注意这里开启了手工应答
        // String queue, boolean autoAck, Consumer callback
        channel.basicConsume(QUEUE_NAME, false, consumer);
    }

}
