package com.laulee.returnlistener;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by laulee on 2020/1/5.
 */
public class ReturnListenerProducer {

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

        channel.addReturnListener(new ReturnListener() {
            public void handleReturn(int replyCode, String replyText, String exchange, String routingKey, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("============监听器收到了无法路由，被返回的消息===========");
                System.out.println("replyTest:" + replyText);
                System.out.println("exchange:" + exchange);
                System.out.println("routingKey:" + routingKey);
                System.out.println("message:"+new String(body));
            }
        });

        //另一种防止消息路由失败时丢失的方式：声明交换机时指定备份交换机
        Map<String,Object> argss = new HashMap<String, Object>();
        argss.put("alternate-exchange","ALTERNATE_EXCHANGE");//指定交换机的备份交换机
        channel.exchangeDeclare("TEST_EXCHANGE","topic",false,false,argss);

        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder()
                .deliveryMode(2).contentEncoding("UTF-8").build();

        //发送到了默认的交换机上，由于没有任何队列使用这个关键字跟交换机绑定，所以会被退回
//        channel.queueDeclare(QUEUE_NAME, false, false, false, null);    //测试消息被退回，注释掉
        //第三个参数时甚至的mandatory，如果mandatory是false，消息也会被直接丢弃
        channel.basicPublish("","routingkeylee",true,properties,"测试交换机将消息正确路由到队列".getBytes());

        TimeUnit.SECONDS.sleep(10);

        channel.close();
        conn.close();

    }

}
