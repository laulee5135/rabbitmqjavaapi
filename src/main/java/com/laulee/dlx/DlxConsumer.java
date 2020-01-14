package com.laulee.dlx;

import com.rabbitmq.client.*;
import com.rabbitmq.client.impl.ChannelN;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by laulee on 2020/1/4.
 * 消息消费者，由于消费者的代码被注释掉了，10秒后，消息会从正常队列TEST_DLX_QUEUE到达死信交换机DLX_EXCHANGE,然后由死信队列DLX_QUEUE消费(DlxConsumer2)
 */
public class DlxConsumer {

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
        final Channel channel = conn.createChannel();

        //指定队列的死信交换机
        Map<String, Object> argss = new HashMap<String, Object>();
        argss.put("x-dead-letter-exchange", DLX_EXCHANGE);
        //TODO 通过 channel.queueDeclare 方法中设置 x-dead-letter-exchange 参数来为队列添加 DLX。也可以为这个 DLX 指定路由键，设置 x-dead-letter-routing-key 参数指定。如果没有指定，则使用原队列的路由键。
//        argss.put("x-expires", 5000);  //指定队列的TTL
//        args.put("x-message-ttl", EXPIRE_TIME); // 设置队列消息的过期时间
//        argss.put("x-max-length", 4);   //如果设置了队列的最大长度，超过长度时，先入队的消息被发送到DLX中, 该属性只有消息堆积的时候才有用。

        //声明普通队列（默认使用direct交换机，direct无须定义routingkey)
        channel.queueDeclare("TEST_DLX_QUEUE", false, false, false, argss);
//        channel.exchangeDeclare("TEST_DLX_EXCHANGE","topic",false,false,null);
//        channel.queueBind("TEST_DLX_QUEUE","TEST_DLX_EXCHANGE","TEST_DLX_ROUTING");

        //声明死信队列
        channel.queueDeclare(DLX_QUEUE, false, false, false, null);
        //声明死信交换机
        channel.exchangeDeclare(DLX_EXCHANGE, "topic", false, false, null);
        //死信交换机绑定死信队列，此处Dead letter routing key 设置为 #  （#多个单词或0个单词）
        channel.queueBind(DLX_QUEUE, DLX_EXCHANGE, "#");
        System.out.println("waiting for message ......");

        // 创建消费者
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, "UTF-8");
                //测试队列顺序执行
//                if (msg.contains("3")) {
                    System.out.println("开始等待....");
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    }  catch (Exception e){
                    }
//                }
                System.out.println("Received message : '" + msg + "'");
                channel.basicAck(envelope.getDeliveryTag(),false);
            }
        };
        // 开始获取消息
        channel.basicConsume("TEST_DLX_QUEUE", false, consumer);
    }
}
