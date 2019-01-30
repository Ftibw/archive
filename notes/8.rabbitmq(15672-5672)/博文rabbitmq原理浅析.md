### 1.背景

RabbitMQ是一个由erlang开发的AMQP(Advanved Message Queue)的开源实现。

### 2.应用场景

#### 2.1异步处理

场景说明：用户注册后，需要发注册邮件和注册短信,传统的做法有两种1.串行的方式;2.并行的方式 
 (1)串行方式:将注册信息写入数据库后,发送注册邮件,再发送注册短信,以上三个任务全部完成后才返回给客户端。 这有一个问题是,邮件,短信并不是必须的,它只是一个通知,而这种做法让客户端等待没有必要等待的东西. 
 ![这里写图片描述](https://img-blog.csdn.net/20170209145852454?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvd2hvYW1peWFuZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
 (2)并行方式:将注册信息写入数据库后,发送邮件的同时,发送短信,以上三个任务完成后,返回给客户端,并行的方式能提高处理的时间。 
 ![这里写图片描述](https://img-blog.csdn.net/20170209150218755?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvd2hvYW1peWFuZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast) 
 假设三个业务节点分别使用50ms,串行方式使用时间150ms,并行使用时间100ms。虽然并性已经提高的处理时间,但是,前面说过,邮件和短信对我正常的使用网站没有任何影响，客户端没有必要等着其发送完成才显示注册成功,英爱是写入数据库后就返回. 
 (3)消息队列 
 引入消息队列后，把发送邮件,短信不是必须的业务逻辑异步处理 
 ![这里写图片描述](https://img-blog.csdn.net/20170209150824008?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvd2hvYW1peWFuZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast) 
 由此可以看出,引入消息队列后，用户的响应时间就等于写入数据库的时间+写入消息队列的时间(可以忽略不计),引入消息队列后处理后,响应时间是串行的3倍,是并行的2倍。

#### 2.2 应用解耦

场景：双11是购物狂节,用户下单后,订单系统需要通知库存系统,传统的做法就是订单系统调用库存系统的接口. 
 ![这里写图片描述](https://img-blog.csdn.net/20170209151602258?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvd2hvYW1peWFuZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

 这种做法有一个缺点:

- 当库存系统出现故障时,订单就会失败。(这样马云将少赚好多好多钱^ ^)
- 订单系统和库存系统高耦合. 
   引入消息队列 
   ![这里写图片描述](https://img-blog.csdn.net/20170209152116530?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvd2hvYW1peWFuZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
- 订单系统:用户下单后,订单系统完成持久化处理,将消息写入消息队列,返回用户订单下单成功。
- 库存系统:订阅下单的消息,获取下单消息,进行库操作。 
   就算库存系统出现故障,消息队列也能保证消息的可靠投递,不会导致消息丢失(马云这下高兴了).

#### 流量削峰

流量削峰一般在秒杀活动中应用广泛 
 场景:秒杀活动，一般会因为流量过大，导致应用挂掉,为了解决这个问题，一般在应用前端加入消息队列。 
 作用: 
 1.可以控制活动人数，超过此一定阀值的订单直接丢弃(我为什么秒杀一次都没有成功过呢^^) 
 2.可以缓解短时间的高流量压垮应用(应用程序按自己的最大处理能力获取订单) 
 ![这里写图片描述](https://img-blog.csdn.net/20170209161124911?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvd2hvYW1peWFuZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
 1.用户的请求,服务器收到之后,首先写入消息队列,加入消息队列长度超过最大值,则直接抛弃用户请求或跳转到错误页面. 
 2.秒杀业务根据消息队列中的请求信息，再做后续处理.

 

# 系统架构

Rabbitmq系统最核心的组件是Exchange和Queue，下图是系统简单的示意图。Exchange和Queue是在rabbitmq server（又叫做broker）端，producer和consumer在应用端。

![img](https://images2015.cnblogs.com/blog/806920/201509/806920-20150926161722819-779629690.png)

## producer&Consumer

producer指的是消息生产者，consumer消息的消费者。

## Queue

消息队列，提供了FIFO的处理机制，具有缓存消息的能力。rabbitmq中，队列消息可以设置为持久化，临时或者自动删除。

1. 设置为持久化的队列，queue中的消息会在server本地硬盘存储一份，防止系统crash，数据丢失
2. 设置为临时队列，queue中的数据在系统重启之后就会丢失
3. 设置为自动删除的队列，当不存在用户连接到server，队列中的数据会被自动删除

## Exchange

Exchange类似于数据通信网络中的交换机，提供消息路由策略。rabbitmq中，producer不是通过信道直接将消息发送给queue，而是先发送给Exchange。一个Exchange可以和多个Queue进行绑定，producer在传递消息的时候，会传递一个ROUTING_KEY，Exchange会根据这个ROUTING_KEY按照特定的路由算法，将消息路由给指定的queue。和Queue一样，Exchange也可设置为持久化，临时或者自动删除。

Exchange有4种类型：direct(默认)，fanout, topic, 和headers，不同类型的Exchange转发消息的策略有所区别：

1. Direct
    直接交换器，工作方式类似于单播，Exchange会将消息发送完全匹配ROUTING_KEY的Queue
2. fanout
    广播是式交换器，不管消息的ROUTING_KEY设置为什么，Exchange都会将消息转发给所有绑定的Queue。
3. topic
     主题交换器，工作方式类似于组播，Exchange会将消息转发和ROUTING_KEY匹配模式相同的所有队列，比如，ROUTING_KEY为user.stock的Message会转发给绑定匹配模式为  * .stock,user.stock， * . * 和#.user.stock.#的队列。（ *  表是匹配一个任意词组，#表示匹配0个或多个词组）
4. headers
    消息体的header匹配（ignore）

## Binding

所谓绑定就是将一个特定的 Exchange 和一个特定的 Queue 绑定起来。Exchange 和Queue的绑定可以是多对多的关系。

## virtual host

在rabbitmq server上可以创建多个虚拟的message broker，又叫做virtual hosts  (vhosts)。每一个vhost本质上是一个mini-rabbitmq  server，分别管理各自的exchange，和bindings。vhost相当于物理的server，可以为不同app提供边界隔离，使得应用安全的运行在不同的vhost实例上，相互之间不会干扰。producer和consumer连接rabbit  server需要指定一个vhost。

# 通信过程

假设P1和C1注册了相同的Broker，Exchange和Queue。P1发送的消息最终会被C1消费。基本的通信流程大概如下所示：

1. P1生产消息，发送给服务器端的Exchange
2. Exchange收到消息，根据ROUTINKEY，将消息转发给匹配的Queue1
3. Queue1收到消息，将消息发送给订阅者C1
4. C1收到消息，发送ACK给队列确认收到消息
5. Queue1收到ACK，删除队列中缓存的此条消息

Consumer收到消息时需要显式的向rabbit broker发送basic.ack消息或者consumer订阅消息时设置auto_ack参数为true。在通信过程中，队列对ACK的处理有以下几种情况：

1. 如果consumer接收了消息，发送ack,rabbitmq会删除队列中这个消息，发送另一条消息给consumer。
2. 如果cosumer接受了消息, 但在发送ack之前断开连接，rabbitmq会认为这条消息没有被deliver,在consumer在次连接的时候，这条消息会被redeliver。
3. 如果consumer接受了消息，但是程序中有bug,忘记了ack,rabbitmq不会重复发送消息。
4. rabbitmq2.0.0和之后的版本支持consumer reject某条（类）消息，可以通过设置requeue参数中的reject为true达到目地，那么rabbitmq将会把消息发送给下一个注册的consumer。