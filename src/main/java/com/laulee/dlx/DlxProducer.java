package com.laulee.dlx;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.HashMap;
import java.util.Map;

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

        Map<String, Object> argss = new HashMap<String, Object>();
        argss.put("x-dead-letter-exchange", "DLX_EXCHNAGE");
//        argss.put("x-expires", 9000);  //指定队列的TTL
//        argss.put("x-max-length", 4);   //如果设置了队列的最大长度，超过长度时，先入队的消息被发送到DLX中, 该属性只有消息堆积的时候才有用。

        // 声明队列
        channel.queueDeclare("TEST_DLX_QUEUE", false, false, false, argss);

        //设置属性，消息10秒过期
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder()
                .deliveryMode(2)  //消息持久化
                .contentEncoding("UTF-8")
                .expiration("10000")
                .build();
        //发送消息(如下方式发送消息，为什么所有的消息发送送到队列之后都变成没有过期，期望是偶数部分的数据过期，实际测试结果是已经过期了，只不过是没有正确显示
        // 但是当下一个数据是没有过期属性的时候，会发生阻塞（按顺序消费），当当前消费者开始消费的时候会把过期的数据发送到死信队列中，这时才会正确消费和在后台显示)
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                channel.basicPublish("", "TEST_DLX_QUEUE", properties, (msg+i).getBytes());
            }else {
                channel.basicPublish("", "TEST_DLX_QUEUE",null, (msg+i).getBytes());
            }

        }
        channel.close();
        conn.close();
    }
}
