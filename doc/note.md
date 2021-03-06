使用场景：
1.跨系统的异步通信，异步、解藕、削峰
	异步：批量数据异步处理
	削峰：高负载任务负载均衡
	解藕：串行任务并行化
	广播：基于发布/订阅模型实现一对多通信

2.应用内的同步变异步
3.基于Pub/Sub模型实现的事件驱动，例如：各种通知
4.RabbitMQ实现事务的最终一致性。


1.死信队列：消息发送到交换机上，交换机无法路由到任何一个队列上的消息就是dead letter queue
没有设置备份交换机、私信交换机策略该消息会被直接被删除
有一种方法将所有死信消息归集起来，就是DLX Dead Letter Exchange
哪些情况消息会变成私信？
    1.消息过期 TTL
        TTL有两种设置的方式：（以最小时间的为准）
        1)通过队列的属性设置，这样的话队列中所有的消息过期时间一致，在 channel.queueDeclare 方法中加入 x-message-ttl 参数实现，单位毫秒。如果不设置 TTL，则表示消息不会过期；如果设置为 0， 除非此时消息可以立即投递给消费者，否则消息会被丢失。
        2)每个消息单独设置，设置方法为在 channel.basicPublish 方法中加入 expiration 属性参数，单位毫秒。
    2.关闭自动应答手动应答时reject（消息拒绝）或 Nack(不应答) 并且 requeue==false（不重新入队） 
    3.队列达到最大长度--先入队的消息会被删除对nack（拒绝多条）和reject（拒绝一条）说明：拒绝的消息如果设置了私信队列那么会到死信队列中，如果没有设置私信队列
    那么当前requeue=true(适用于多个消费者，如果是一个消费者会出现循环消费的问题)

消息什么时候从队列删除？解：发送到消费者之后。（不是等消费者处理完）

延迟队列、私信队列

wireshark

如果保证消费者正确消费完毕？
1.消费者回调
	业务库，消息落库
	1）提供一个回调的API
	2）消费者发送消息
2.补偿机制（消息的重复或者确认）（消费者）
	如果在1中没有回调。才用补偿机制。根据当前消息的状态进行 重发次数或者定时任务机制实现。
3.消息幂等性
	消费者，每一条数据都会带一个唯一的ID，根据该字段进行控制重复
4.消息顺序性
	一个队列只有一个消费者的情况下，才能保证顺序。 

集群：
通过镜像队列实现高可用mq的消息复制。

谁来创建对象（交换机、队列】、绑定关系）  由消费者创建	
rabbitmq中的连接什么时候创建，什么时候销毁？

rabbitmq消费者和生产者两端的连接、队列、交换机等配置信息要一致。


一个队列被多个消费者监听，默认采用的是轮训的方式分发消息。这种模式属于工作队列（work queue）。看官网解释

问题：
1.怎么自动删除没人消费的消息？  
	Q：1.设置队列的消息TTL 2.设置单条消息的TTL
2.无法路由的消息，去了哪里？    
	Q：无法被路由的消息变成了死信，如果没有设置备份交换机或者死信交换机，该消息直接被删除。 如果设置了DLX（dead letter exchange 死信交换机），那么所有死信会被路由到此，再由一个绑定的死信队列进行消费。
3.可以让消息优先得到消费吗？(优先级队列)
	Q：使用priority 和 x-max-priority
4.如何实现延迟发送消息？（延迟队列）
	Q: 第一种：使用TTL+DLX  第二种：延迟队列插件
5.MQ怎么实现RPC？ 了解
6.Rabbitmq 流控怎么做？ 设置队列大小有用吗？（设置队列的大小没用，如果消费者消费的快，那么队列该属性永远无法触及）
	Q：流控分为服务端 和 消费端，服务端：目前没有对特定数量的消息的限制，只能通过设置mq所占的内存、磁盘的容量比例来调整。  消费端：通过参数prefetchCount（预取的数量，如下3），channel.basicQos(3)（只有等3条消息ack之后，才会接收下一个消息）


MQ可靠性投递：
1.确保消息发送到RabbitMQ服务器
	服务端：有两种模式可以解决
	1.transaction模式
	//将channel设置成事物模式
	channel.txSelect();
	//提交事物
	channel.txCommit();
	//事物回滚
	channel.txRollback();
	2.Confirm模式
	//将channel设置为comfirm模式
	channel.confirmSelect();
	if(channel.waitForConfirms()){
		//消息发送成功
	}

2.确保消息路由到正确的队列（见代码ReturnListenerProducer.java）
	1.ReturnListener
	2.备份交换机
3.确保消息路在队列中正确的存储
	1.队列持久化   //channel.queueDeclare(QUEUE_NAME, false, false, false, argss);  第二个参数为true
	2.交换机持久化  //channel.exchangeDeclare("TEST_EXCHANGE","topic",false,false,argss);  第三个参数为true
	3.消息持久化   //AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().deliveryMode(2).build();   deliveryMode=2表示消息持久化
4.确保消息从队列正确的投递到消费者（见代码 AckConsumer.java）
	消费者确认
	channel.basicAck(); // 手工应答
	channel.basicReject();  //单条拒绝
	channel.basicNack();  //批量拒绝
5.其他
	1.消费者回调（解决消息达到消费者并且消费者也已经处理完毕）  场景：A系统发送消息到MQ，B系统进行消费MQ   【消费方消息落库】
		两种实现方式：
		1.1 A系统提供一个回调的API，B系统消费成功之后，调用此API，修改A系统业务的状态。
		1.2 B系统可以发送消息，B系统消费完毕之后，将消费结果发送到另一个MQ中，A系统再进行消费该MQ中的消息修改状态，也就是通过异步两次通信的方式保证消费完成。
	2.补偿机制（解决消费者没有回调[1.1]或没有发送消息[1.2]）
		消息生产者业务库中可以设置一个重发次数的字段，通过跑批实现重发。
		如果N次之后还是无法正确消费，触发人工干预。
	3.消息幂等性（解决重复消费）
		消费方消息落库，通过唯一ID等标识判断。（相当于一张消费控制表）
	4.消息顺序性（场景：删除微博必须在发布微博之后进行）
		一个队列只有一个消费者的情况下，才能保证顺序，如果是多个消费者的，可以通过设置全局ID的方式保证顺序消费。


高可用
1.集群：只能同步交换机不能同步队列和队列中的消息（需要使用镜像队列实现此功能policy）
	1.1 通信基础：多个节点的时候，第一需要做节点之间的身份认证：erlang.cookie，每个节点的此文件都需一致。第二，节点加入集群不通过IP加入，需修改hosts加入hostname。
	1.2 磁盘节点与内存节点 
		一般两者搭配起来使用，最少有一个磁盘节点。
	1.3配置步骤	
	1.4 位了数据一致和高性能只能部署在局域网内LAN，如果非要实现广域网部署，那么通过插件实现：federation插件、shovel插件

2.镜像队列
3.HA方案（解决应用层访问的问题）

经验总结：
1.配置文件与命令规范：A_TO_B_QUEUE  或者 A_TO_B_EXCHANGE
2.调用封装: 比如sb中对经常使用的rabbitmqtemplate进行二次封装，调用接口就知道发送的哪个交换机或哪个队列，无需业务代码里再进行指定。
3.信息落库（生产者发送消息前落库，利于消息可追溯、消息可重发）+定时任务（效率低，占用磁盘空间）
4.如何减少连接数？
	批量发送消息，具体的功能由消费者进行循环执行。但是每批发送的消息不要超过4M，不发大文件。
5.生产者先发送消息还是先登记业务表？ 先登记业务表，后发送消息，防止消息会滚数据不一致的问题。
6.谁来创建对象（交换机、队列、绑定关系）？ 由消费者进行创建，谁消费谁创建。有疑问：在系统中先启动哪方？ 如果启动的是生产者，那么没有队列，这些消息是放在什么地方的？
7.运维监控
	zabbix
8.其他插件


交换机类型：direct、fanout、topic 每一种对应投递队列方式不同。具体见官网。
direct：任何发送到Direct Exchange的消息都会被转发到routing_key中指定的Queue
fanout：fanout无须指定routingkey，在这个模式下routing key失去作用，提前将Exchange与Queue进行绑定，一个Exchange可以绑定多个Queue，一个Queue可以同多个Exchange进行绑定
topic：

Question：
1、消息队列的作用与使用场景?
2、创建队列和交换机的方法?
3、多个消费者监听一个生产者时，消息如何分发?  1.轮训 2.公平分发（使用basicQos实现公平分发）
4、无法被路由的消息，去了哪里?   如果没有设置死信交换机或者备份交换机的时候直接被丢弃，解决：1.使用mandatory = true 配合ReturnListerner，实现消息回发  2.指定备份交换机
5、消息在什么时候会变成Dead Letter (死信) ? 只有当消息到达死信交换机的时候消息才是死信。
6、RabbitMQ如何实现延迟队列?
7、如何保证消息的可靠性投递?
8、如何在服务端和消费端做限流?  服务端默认是内存少于40%，磁盘小于1G的时候不再接收消息，可通过配置文件修改。也可在网关层进行限流。
9、如何保证消息的顺序性?
10、RabbitMQ的集群节点类型?
11、消息的幂等性：在有补偿机制的情况下会有消息重发的情况，可在生产者 AMQP.BasicProperites.Builder().messageId(String.valueOf(UUID.rangdomUUID()) 生成一个唯一ID，并且业务消息中指定一个唯一的业务ID，然后在消费者一端，进行消息落库，来区分避免重复消费。


Spring AMQP默认使用的消息转换器是SimpleMessageConverter


疑问：
1.交换机、队列等对象是由生产者创建还是消费者创建？为什么？
2.为什么声明队列的时候最后一个参数 arguments 必须在生产端和消费者都得声明同样的参数(eg:"x-message-ttl")？不一样时会报错？   生产者消费者声明同一个队列时，定义要一致
3.生产者在一个通道里批量发送不同属性（是否带AMQP.BasicProperties，比如过期属性）的数据，为什么带过期属性的数据和没带过期属性的数据都会存活，过期的数据并没有删除或者转移到死信队列（假如有）中。
4.MQ什么情况下会发生堵塞（堆积）？怎么处理？
5.怎么知道一个消息是否已经被消费（或者 消息是否还在队列中）？（数据量很大）。只要被ack的消息都是已经被消费的，已经消费的数据就不会存在在队列中（通过消息落库确定）
6.怎么通过UI界面或者命令查看队列中内容？ 好像没有
7.一个队列中是内容是顺序被消费的，如果某一个消息消费很慢，没有及时ack，就会造成后面数据的积压，如何解决这样的超时消费？
8.自动确认模式下，消费过程中消费者挂了，队列中的消息都会消失吗？
9.当是自动确认模式下，是先消费完消息再在队列中确认删除还是先确认删除再消费（执行业务代码）？关联问题8
10.如果发生堆积，有TTL的消息集体失效怎么办？