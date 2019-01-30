# RabbitMQ消费者的几个参数

​             ![96](https://upload.jianshu.io/users/upload_avatars/1528900/6a8c2914a9b8.jpg?imageMogr2/auto-orient/strip|imageView2/1/w/96/h/96) 

​             [王鸿缘](https://www.jianshu.com/u/07b68d1085ff)                          

​                                2016.09.10 21:57*               字数 1411             阅读 10772评论 0喜欢 5

## 分布式消息中间件

RabbitMQ是用Erlang语言编写的分布式消息中间件，常常用在大型网站中作为消息队列来使用，主要目的是各个子系统之间的解耦和异步处理。消息中间件的基本模型是典型的生产者-消费者模型，生产者发送消息到消息队列，消费者监听消息队列，收到消息后消费处理。

在使用RabbitMQ做消息分发时，主要有三个概念要注意：Exchange，RoutingKey，Queue。

Exchange可以理解为交换器，RoutingKey可以理解为路由，Queue作为真实存储消息的队列和某个Exchange绑定，具体如何路由到感兴趣的Queue则由Exchange的三种模式决定：

- fanout
- topic
- direct

Exchange为fanout时，生产者往此Exchange发送的消息会发给每个和其绑定的Queue，此时RoutingKey并不起作用；Exchange为topic时，生产者可以指定一个支持通配符的RoutingKey（如demo.*）发向此Exchange，凡是Exchange上RoutingKey满足此通配符的Queue就会收到消息；direct类型的Exchange是最直接最简单的，生产者指定Exchange和RoutingKey，然后往其发送消息，消息只能被绑定的满足RoutingKey的Queue接受消息。(通常如果不指定RoutingKey的具体名字，那么默认的名字其实是Queue的名字）

## Concurrency与Prefetch

在通常的使用中(Java项目)，我们一般会结合spring-amqp框架来使用RabbitMQ，spring-amqp底层调用RabbitMQ的java  client来和Broker交互，比如我们会用如下配置来建立RabbitMQ的连接池、声明Queue以及指明监听者的监听行为：

```
<rabbit:connection-factory id="connectionFactory" />

<!-- template非必须，主要用于生产者发送消息-->
<rabbit:template id="template" connection-factory="connectionFactory" />

<rabbit:queue name="remoting.queue" />
<rabbit:listener-container connection-factory="connectionFactory" concurrency="2" prefetch="3">
    <rabbit:listener ref="listener" queue-names="remoting.queue" />
</rabbit:listener-container>
```

listener-container可以设置消费者在监听Queue的时候的各种参数，其中concurrency和prefetch是本篇文章比较关心的两个参数，以下是spring-amqp文档的解释：

> prefetchCount(prefetch)
>  The number of messages to accept from the broker in one socket frame.  The higher this is the faster the messages can be delivered, but the  higher the risk of non-sequential processing. Ignored if the  acknowledgeMode
>  is NONE. This will be increased, if necessary, to match the txSize

> concurrentConsumers(concurrency)

The number of concurrent consumers to initially start for each listener.

简单解释下就是concurrency设置的是对每个listener在初始化的时候设置的并发消费者的个数，prefetch是每次从一次性从broker里面取的待消费的消息的个数，上面的配置在监控后台看到的效果如下：

 

![img](https://upload-images.jianshu.io/upload_images/1528900-ea7ec17528517a40.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/700)

图中可以看出有两个消费者同时监听Queue，但是注意这里的消息只有被一个消费者消费掉就会自动ack，另外一个消费者就不会再获取到此消息，Prefetch   Count为配置设置的值3，意味着每个消费者每次会预取3个消息准备消费。每个消费者对应的listener有个Exclusive参数，默认为false,  如果设置为true，concurrency就必须设置为1，即只能单个消费者消费队列里的消息，适用于必须严格执行消息队列的消费顺序（先进先出）。

## 源码剖析

这里concurrency的实现方式不看源码也能猜到，肯定是用多线程的方式来实现的，此时同一进程下打开的本地端口都是56278.下面看看listener-contaner对应的org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer的源码：

```
protected int initializeConsumers() {
  int count = 0;
  synchronized (this.consumersMonitor) {
    if (this.consumers == null) {
        this.cancellationLock.reset();
        this.consumers = new HashMap<BlockingQueueConsumer, Boolean>(this.concurrentConsumers);
        for (int i = 0; i < this.concurrentConsumers; i++) {
            BlockingQueueConsumer consumer = createBlockingQueueConsumer();
            this.consumers.put(consumer, true);
            count++;
        }
    }
  }
  return count;
}
```

container启动的时候会根据设置的concurrency的值（同时不超过最大值）创建n个BlockingQueueConsumer。

```
protected void doStart() throws Exception {
  //some code
  synchronized (this.consumersMonitor) {
    int newConsumers = initializeConsumers();

    //some code
    Set<AsyncMessageProcessingConsumer> processors = new HashSet<AsyncMessageProcessingConsumer>();
    for (BlockingQueueConsumer consumer : this.consumers.keySet()) {
        AsyncMessageProcessingConsumer processor = new AsyncMessageProcessingConsumer(consumer);
        processors.add(processor);
        this.taskExecutor.execute(processor);
    }
    //some code
  }
}
```

在doStart()方法中调用initializeConsumers来初始化所有的消费者，AsyncMessageProcessingConsumer作为真实的处理器包装了BlockingQueueConsumer，而AsyncMessageProcessingConsumer其实实现了Runnable接口，由this.taskExecutor.execute(processor)来启动消费者线程。

```
private final class AsyncMessageProcessingConsumer implements Runnable {
  private final BlockingQueueConsumer consumer;
  private final CountDownLatch start;
  private volatile FatalListenerStartupException startupException;

  private AsyncMessageProcessingConsumer(BlockingQueueConsumer consumer) {
    this.consumer = consumer;
    this.start = new CountDownLatch(1);
  }

  //some code
  @Override
  public void run() {
     //some code
  }
}
```

那么prefetch的值意味着什么呢？其实从名字上大致能看出，BlockingQueueConsumer内部应该维护了一个阻塞队列BlockingQueue，prefetch应该是这个阻塞队列的长度，看下BlockingQueueConsumer内部有个queue，这个queue不是对应RabbitMQ的队列，而是Consumer自己维护的内存级别的队列，用来暂时存储从RabbitMQ中取出来的消息：

```
private final BlockingQueue<Delivery> queue;

public BlockingQueueConsumer(ConnectionFactory connectionFactory,
            MessagePropertiesConverter messagePropertiesConverter,
            ActiveObjectCounter<BlockingQueueConsumer> activeObjectCounter, AcknowledgeMode acknowledgeMode,
            boolean transactional, int prefetchCount, boolean defaultRequeueRejected,
            Map<String, Object> consumerArgs, boolean exclusive, String... queues) {
  //some code
  this.queue = new LinkedBlockingQueue<Delivery>(prefetchCount);
}
```

BlockingQueueConsumer的构造函数清楚说明了每个消费者内部的队列大小就是prefetch的大小。

## 业务问题

前面说过，设置并发的时候，要考虑具体的业务场景，对那种对消息的顺序有苛刻要求的场景不适合并发消费，而对于其他场景，比如用户注册后给用户发个提示短信，是不太在意哪个消息先被消费，哪个消息后被消费，因为每个消息是相对独立的，后注册的用户先收到短信也并没有太大影响。

设置并发消费除了能提高消费的速度，还有另外一个好处：当某个消费者长期阻塞，此时在当前消费者内部的BlockingQueue的消息也会被一直阻塞，但是新来的消息仍然可以投递给其他消费者消费，这种情况顶多会导致prefetch个数目的消息消费有问题，而不至于单消费者情况下整个RabbitMQ的队列会因为一个消息有问题而全部堵死。所有在合适的业务场景下，需要合理设置concurrency和prefetch值。