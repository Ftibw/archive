Packages

--------

 org.eclipse.paho.client.mqttv3
====================

## `[Interfaces]`

### IMqttActionListener

```java
	当异步操作完成时，将会通知该接口的实现者。监听器在MqttToken上注册，令牌与连接或发布等操作相关联。
    当与MqttAsyncClient上的令牌一起使用时，侦听器将在MQTT客户端的线程上被回调。 如果操作成功或失败，监听器将被通知。
    监听器快速返回控制非常重要，否则MQTT客户端的操作将会停止。
    void onSuccess(IMqttToken asyncActionToken)
    void onFailure(IMqttToken asyncActionToken, java.lang.Throwable exception)
```

### IMqttAsyncClient 

`[implements IMqttAsyncClient]`

```java
    使应用程序能够使用非阻塞方法与MQTT服务器进行通信。使用非阻塞客户端允许应用程序使用阻塞和非阻塞样式的混合。
```

### IMqttClient

```java
    使用阻塞客户端只允许应用程序使用一种样式。 阻塞客户端提供了与早期版本的MQTT客户端的兼容性。
```

### IMqttDeliveryToken 

[extends IMqttToken]

```java
    IMqttToken的子类，跟踪消息的传递。
    与IMqttToken交付令牌的实例不同，可以在连接和客户端重新启动时使用。 这样可以在发送失败后跟踪消息。
```

### IMqttMessageListener

​    消息到达时会通知此接口的实现者。

### IMqttToken

​    提供跟踪异步任务完成的机制。

### MqttCallback

```java
    当发生与客户端相关的异步事件时，使应用程序得到通知。实现此接口的类可以在两种类型的客户端上注册：
    IMqttClient.setCallback(MqttCallback)和IMqttAsyncClient.setCallback(MqttCallback)
    void 	connectionLost (java.lang.Throwable cause)         当与服务器的连接丢失时调用此方法。
    void	deliveryComplete ( IMqttDeliveryToken token)    在消息传递完成时调用，并且已收到所有确认。
    void	messageArrived (java.lang.String topic, MqttMessage message)    当消息从服务器到达时调用此方法。
```

### MqttCallbackExtended 

[extends MqttCallback]

```java
    扩展MqttCallback以允许新的回调，而不会中断现有应用程序的API。 实现此接口的类可以在两种类型的客户端上注册：
    IMqttClient.setCallback(MqttCallback)和IMqttAsyncClient.setCallback(MqttCallback)
    void	connectComplete (boolean reconnect, java.lang.String serverURI)     当与服务器的连接成功完成时调用。
    继承的方法   connectionLost , deliveryComplete , messageArrived
```

### MqttClientPersistence

```java
    表示一个持久性数据存储，用于存储出站和入站邮件，以便传送到指定的QoS。 
    可使用MqttClient.MqttClient(String, String, MqttClientPersistence)指定该接口的实现， MqttClient将使用该MqttClient来保持QoS 1 和 2 消息。
    如果定义的方法抛出MqttPersistenceException，那么数据持久化的状态应保持为被调用方法之前的状态。 
    例如，如果put(String, MqttPersistable)在任何时候抛出异常，那么数据将被假定为不在持久存储中。
    同样，如果remove(String)抛出一个异常，那么数据将被认为仍然保存在持久性存储中。
    持久性接口由记录在诊断持续性故障时可能需要的任何异常或错误信息。
```

### MqttPersistable

​    表示一个对象，用于传递要在MqttClientPersistence接口上持久保存的数据。
    当数据通过接口传递时，标题和有效载荷是分开的，以避免不必要的消息拷贝。

### MqttPingSender

```java
    表示每个保持活动时间间隔用于向MQTT代理发送ping数据包的对象。
    void	init ( ClientComms comms)   初始方法。
    void	schedule (long delayInMilliseconds)     安排下一个ping在一定的延迟。
    void	start ()    开始ping发送者。
    void	stop ()     停止ping发件人。
```



-----

## `[Classes]`

### BufferedMessage

​    一个BufferedMessage包含一个MqttWire消息和令牌，它允许当客户端处于静止状态时缓冲消息和令牌
DisconnectedBufferOptions
    保存用于管理离线（或断开）缓冲消息行为的选项集

### MqttAsyncClient 

​    轻量级客户端，使用允许在后台运行操作的非阻塞方法与MQTT服务器交互。
    默认情况下，使用MqttDefaultFilePersistence将消息存储到文件中。 如果将持久性设置为null，则消息将存储在内存中。

### MqttClient 

[implements IMqttClient]
    轻量级客户端，使用阻塞操作完成的方法与MQTT服务器交互。

### MqttConnectOptions

```java
    保存一组控制客户端连接到服务器的选项。
    static boolean	CLEAN_SESSION_DEFAULT   如果没有指定默认清理会话设置
    static int	CONNECTION_TIMEOUT_DEFAULT  如果没有指定默认的连接超时时间（以秒为单位）
    static int	KEEP_ALIVE_INTERVAL_DEFAULT 如果未指定，则以秒为单位的默认保持活动时间间隔
    static int	MAX_INFLIGHT_DEFAULT    如果没有指定，则默认为最大航程
    static int	MQTT_VERSION_3_1
    static int	MQTT_VERSION_3_1_1
    static int	MQTT_VERSION_DEFAULT    首先默认的MqttVersion是3.1.1 ，如果失败，则返回到3.1
```

### MqttDeliveryToken 

[extends MqttToken implements IMqttDeliveryToken]
	提供跟踪消息传递进度的机制。用于以非阻塞方式（在后台运行）跟踪消息的发送进度。

### MqttMessage

​	MQTT消息包含应用程序有效负载和选项，指定如何传递消息。消息包含表示为byte []的“payload”（消息体）。

### MqttToken

[implements IMqttToken]

​	提供跟踪异步操作完成的机制。实现ImqttToken接口的令牌从所有非阻塞方法返回，发布除外。

### MqttTopic      

​	表示主题目标，用于发布/订阅消息传递。

### TimerPingSender 

[implements MqttPingSender]

```java
    默认ping发送者执行
    这个类实现了IMqttPingSender pinger接口，允许应用程序在每个活动时间间隔IMqttPingSender服务器发送ping数据包。
```

----

## `[Exception]`

### MqttExceptionn 

[extends Exception]

### MqttPersistenceException 

[extends MqttException]

### MqttSecurityException 

[extends MqttException]

---

  org.eclipse.paho.client.mqttv3.internal 
=========================
## `[Interfaces]`

### DestinationProvider

```java
    这个接口可以作为MqttClient和MqttMIDPClient的一个通用类型，所以它们可以传递给ClientComms而不需要客户端类需要知道另一个。
    具体而言，这允许MIDP客户端在没有非MIDP MqttClient / MqttConnectOptions类的情况下工作。
    MqttTopic	getTopic (java.lang.String topic)
```

### IDisconnectedBufferCallback

```java
    void	publishBufferedMessage ( BufferedMessage bufferedMessage)
```

### NetworkModule

```java
    java.io.InputStream	getInputStream ()
    java.io.OutputStream	getOutputStream ()
    java.lang.String	getServerURI ()
    void  start ()
    void  stop ()
```

---

## `[Classes]`

### ClientComms

​    处理与服务器的客户端通信。 发送和接收MQTT V3消息。

### ClientDefaults

```java
    public static final int MAX_MSG_SIZE = 1024 * 1024 * 256; // 256 MB
```

### ClientState

```java
    客户端的核心，它保存待处理和正在进行的消息的状态信息。 已经被接受发送的消息在被递送的同时在几个对象之间移动。 
    1）当客户端没有运行时，消息被存储在一个实现了MqttClientPersistent接口的持久性存储中。
    默认是MqttDefaultFilePersistence，它可以跨越故障和系统重新启动安全地存储消息。
    如果没有指定持久性，则返回到MemoryPersistence，这将在Mqtt客户端实例化时维护消息。
    2）当客户端或特定的ClientState被实例化时，将消息从持久性存储中读取到：
    - outboundqos2哈希表，如果QoS 2 PUBLISH或PUBREL
    - outboundqos1哈希表，如果QoS 1 PUBLISH（参见restoreState）
    3）On Connect，复制消息以messageid顺序将出站哈希表添加到pendingMessages或pendingFlows向量中。
    - 初始消息发布进入pendingmessages缓冲区。 - PUBREL进入待处理缓冲区（参见restoreInflightMessages）
    4）发送程序线程从待处理流和待处理消息缓冲区中读取消息。 该消息将从pendingbuffer中删除，但保留在出站*哈希表。
    哈希表是将全部未完成消息存储在内存中的地方。 （持久性仅在启动时使用）
    5）接收器线程 - 接收有线消息： - 如果QoS 1则从持久性中移除，outboundqos1 - 如果QoS 2 PUBREC发送PUBREL。
    使用PUBREL更新outboundqos2条目并更新持久性。
    - 如果QoS 2 PUBCOMP从持久性和outboundqos2中移除注意：由于客户端的多线程特性，对这个类的任何修改都考虑到并发性至关重要。
    例如，只要流量/消息放在线路上，接收线程就可以接收确认并在发送方完成处理之前处理响应。 
    例如连接可能被发送，在连接通知发送之前收到的conack已经被处理！
```

### CommsCallback 

[implements Runnable]

```
    Receiver和外部API之间的桥梁。 此类由Receiver调用，然后将以通信为中心的MQTT消息对象转换为由外部API理解的对象。
```

### CommsReceiver 

[implements Runnable]

```
    接收来自服务器的MQTT数据包。
```

### CommsSender 

[implements Runnable]

### CommsTokenStore

```java
    提供基于“令牌”的系统来存储和跟踪跨多个线程的操作。发送消息时，令牌与消息相关联，并使用saveToken(MqttToken, MqttWireMessage)方法保存。
    任何对该状态感兴趣的，都可以调用令牌上的一个等待方法，或者在操作上使用异步监听器回调方法。
    另一个线程上的CommsReceiver类从网络读取响应。 它使用响应来查找相关令牌，然后可以通知它。
    注：Ping，连接和断开连接没有唯一的消息ID，因为每种类型的只有一个未决的请求被允许未完成
```

### ConnectActionListener 

[implements IMqttActionListener]

```java
    这个类处理AsyncClient到一个可用URL的连接。这些URL在创建客户端时作为单例提供，或作为连接选项中的列表提供。
    这个类使用自己的onSuccess和onFailure回调优先于用户提供的回调。尝试连接到列表中的每个URL，直到连接尝试成功或尝试了所有URL
    如果连接成功，则会通知用户令牌，并调用用户onSuccess回调。
    如果连接失败，则尝试列表中的另一个URL，否则将通知用户令牌，并调用用户onFailure回调
    void	connect ()  开始连接处理
    void	onFailure ( IMqttToken token, java.lang.Throwable exception) 连接失败，请尝试列表中的下一个URI。
    void	onSuccess ( IMqttToken token)   如果连接成功，则调用用户onSuccess回调
    void	setMqttCallbackExtended ( MqttCallbackExtended mqttCallbackExtended)    设置MqttCallbackExtened回调接收connectComplete回调
```

### DisconnectedMessageBuffer 

[implements Runnable]

### ExceptionHelper 

​    工具类可帮助创建正确类型的例外。

### FileLock

### LocalNetworkModule 

[implements NetworkModule]

```java
    特殊的comms类，允许MQTT客户机在与MQTT服务器运行在同一JRE实例中时，使用非TCP /优化机制与MQTT服务器交谈。
    这个类检查是否存在优化的通信协议类，即提供优化的通信机制的类。 如果不可用，则使用优化机制进行连接的请求将被拒绝。 
    实现这一点的唯一已知的服务器是微型经纪人： - 一个与许多IBM产品一起提供的MQTT服务器。
public abstract class MessageCatalog
    private static MessageCatalog INSTANCE = null;
    人类可读的错误信息目录。
```

### MqttPersistentData 

[implements MqttPersistable]

### ResourceBundleCatalog 

[extends MessageCatalog]

### SSLNetworkModule 

[extends TCPNetworkModule]

```
    用于通过SSL连接的网络模块。
```



### TCPNetworkModule 

[implements NetworkModule]

```
     一个通过TCP连接的网络模块。
```

Token

---

org.eclipse.paho.client.mqttv3.internal.security
==============================
## `[Classes]`

### SimpleBase64Encoder

```java
    private static final String PWDCHARS_STRING = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private static final char[] PWDCHARS_ARRAY = PWDCHARS_STRING.toCharArray();
    public static String encode(byte[] bytes)
    public static byte[] decode(String string)
    private final static String to64(long input, int size)
    private final static long from64(byte[] encoded, int idx, int size)
```

### SSLSocketFactoryFactory

```java
    一个SSLSocketFactoryFactory提供了一个套接字工厂和一个服务器套接字工厂，然后可以用来创建SSL客户端套接字或SSL服务器套接字。
    SSLSocketFactoryFactory使用IBM SSL属性进行配置，即格式为“com.ibm.ssl.propertyName”的属性，例如“com.ibm.ssl.keyStore”。
    该类支持多种配置，每种配置都使用名称或配置ID进行标识。 使用“null”的配置ID作为默认配置。 当为给定配置创建套接字工厂时，首先会选择该配置的属性。 
    如果在那里没有定义属性，那么在默认配置中查找该属性。 最后，如果还没有找到属性元素，则检查相应的系统属性，即javax.net.ssl.keyStore。 
    如果系统属性没有设置，那么使用系统的默认值（如果可用）或引发异常。SSLSocketFacotryFactory可以在任何时候重新配置。 重新配置不会影响现有套接字工厂。
    所有的属性共享相同的密钥空间; 即配置ID不是属性键的一部分。应按以下顺序调用方法：
    isSupportedOnJVM（） ：检查此类是否在运行时平台上受支持。 并非所有运行时支持SSL / TLS。
    SSLSocketFactoryFactory（） ：构造函数。 客户端（在同一个JVM中）可能共享一个SSLSocketFactoryFactory，或者每个都有一个。
    initialize（properties，configID） ：用配置所需的SSL属性初始化此对象。 这可能会被调用多次，每次需要配置一次。它可能会被再次调用来更改特定配置所需的SSL属性。
    getEnabledCipherSuites（configID） ：稍后在套接字上设置启用的密码套件[见下文]。
    对于MQTT服务器：
    getKeyStore（configID） ：或者，要检查是否没有密钥库，那么所有启用的密码套件都是匿名的。
    createServerSocketFactory（configID） ：创建一个SSLServerSocketFactory。
    getClientAuthentication（configID） ：稍后在SSLServerSocket（自身从SSLServerSocketFactory创建）上设置是否需要客户端身份验证。
    对于MQTT客户端：createSocketFactory（configID） ：创建一个SSLSocketFactory。
```

---

org.eclipse.paho.client.mqttv3.internal.websocket
================================
## `[Classes]`

### Base64

```java
	private static final Base64 instance = new Base64();
	private static final Base64Encoder encoder = instance.new Base64Encoder();
    public static String encode (String s)
    public static String encodeBytes (byte[] b)
    public class Base64Encoder extends AbstractPreferences
```

### WebSocketFrame  

公共类

### WebSocketHandshake  

公共类。Helper类执行WebSocket握手。

### WebSocketNetworkModule 

[extends TCPNetworkModule]

### WebSocketReceiver 

[implements Runnable]

### WebSocketSecureNetworkModule 

[extends SSLNetworkModule]

---

## `[Exceptions]`

### HandshakeFailedException 

[extends Exception]

```java
{ private static final long serialVersionUID = 1L; }
```

---

org.eclipse.paho.client.mqttv3.internal.wire
==============================
## `[Classes]`

### CountingInputStream 

[extends InputStream]

```java
    计数从中读取的字节的输入流。
   	private InputStream in;
	private int counter;
    public CountingInputStream(InputStream in)
    public int read() throws IOException
    public int getCounter() {		return counter;	}
    public void resetCounter() {		counter = 0;	}
```

### MqttAck 

[extends MqttWireMessage]

```java
    所有确认消息的抽象超类。
   	public MqttAck(byte type) {	super(type);	}
	protected byte getMessageInfo() {		return 0;	}
```

### MqttConnack 

[extends MqttAck]

```java
    MQTT CONNACK的在线表示。
    public static final String KEY = "Con";
    private int returnCode;
    private boolean sessionPresent;
```

### MqttConnect 

[extends MqttWireMessage]

```java
	private String clientId;
	private boolean cleanSession;
	private MqttMessage willMessage;
	private String userName;
	private char[] password;
	private int keepAliveInterval;
	private String willDestination;
	private int MqttVersion;
```

### MqttDisconnect 

[extends MqttWireMessage]

### MqttInputStream 

[extends InputStream]

```java
    一个MqttInputStream让应用程序读取MqttWireMessage实例。
    private static final String CLASS_NAME = MqttInputStream.class.getName();
	private static final Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);
	private ClientState clientState = null;
	private DataInputStream in;
```

### MqttOutputStream 

[extends OutputStream]

```java
    一个MqttOutputStream让应用程序编写MqttWireMessage实例。
    private static final String CLASS_NAME = MqttOutputStream.class.getName();
	private static final Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);
	private ClientState clientState = null;
	private BufferedOutputStream out;
```

### MqttPersistableWireMessage 

[extends MqttWireMessage implements MqttPersistable]

### MqttPingReq 

[extends MqttWireMessage]

### MqttPingResp 

[extends MqttAck]

### MqttPubAck 

[extends MqttAck]

### MqttPubComp 

[extends MqttAck]

### MqttPublish 

[extends MqttPersistableWireMessage]

### MqttPubRec 

[extends MqttAck]

### MqttPubRel 

[extends MqttPersistableWireMessage]

### MqttReceivedMessage 

[extends MqttMessage]

### MqttSuback 

[extends MqttAck]

### MqttSubscribe 

[extends MqttWireMessage]

### MqttUnsubAck 

[extends MqttAck]

### MqttUnsubscribe 

[extends MqttWireMessage]

### public abstract class MqttWireMessage

### MultiByteArrayInputStream 

[extends InputStream]

```java
	private byte[] bytesA;
	private int offsetA;
	private int lengthA;
	private byte[] bytesB;
	private int offsetB;
	private int lengthB;	
	private int pos = 0;	
	public MultiByteArrayInputStream(byte[] bytesA, int offsetA, int lengthA, byte[] bytesB, int offsetB, int lengthB){ }
    public int read() throws IOException{ }
MultiByteInteger
    表示由MQTT V3规范定义的多字节整数（MBI）。
    private long value;
	private int length;
    public MultiByteInteger(long value)
    public MultiByteInteger(long value, int length)
    public int getEncodedLength()
    public long getValue()
```

---

org.eclipse.paho.client.mqttv3.logging
========================
## `[Interfaces]` 

### Logger

---

## `[Classes]`

```java
JSR47Logger implements Logger
    使用java.uti.logging的记录器接口的实现使用Java内置日志记录工具的记录器 - java.util.logging。
    提供了一个示例java.util.logging属性文件 - jsr47min.properties，演示了如何使用基于内存的跟踪工具运行，该工具以最低的性能开销运行。
    当记录/跟踪记录被写入与MemoryHandler触发器级别匹配或在MemoryHandler上调用push方法时，可以转储内存缓冲区。
    Debug提供的方法可以很容易地转储内存缓冲区以及其他有用的调试信息。
```



### LoggerFactory

```java
    返回记录器以供MQTT客户端使用的工厂。 默认的日志和跟踪工具使用Java在日志工具中的构建：
    - java.util.logging。 对于不可用的系统或需要替代日志框架的系统，可以使用setLogger(String)来替换日志工具，该日志工具需要实现Logger接口。
```

### SimpleLogFormatter 

[extends Formatter]

```java
    以可读形式打印单行日志记录。
```

---

org.eclipse.paho.client.mqttv3.persist
========================

## `[Classes]`

### MemoryPersistence 

[implements MqttClientPersistence]

```java
    使用内存的持久性，在客户端或设备不需要可靠性，重新启动内存的情况下，可以使用该内存持久性。
    在需要可靠性的情况下，例如当clean session设置为false时，应使用非易失性形式的持久性。
```

### MqttDefaultFilePersistence 

[implements MqttClientPersistence]

```java
    提供基于文件的持久性的MqttClientPersistence接口的实现。 当创建Persistence对象时指定一个目录。
    当持久性被打开时（见open(String, String) ），在这个客户端ID和连接键的基础下创建一个子目录。
    这允许一个持久性基目录被多个客户共享。 
    子目录的名称是通过将客户端ID和连接键与'/'，'\\'，'：'或''的任何实例连接而创建的。
```

---

org.eclipse.paho.client.mqttv3.util
======================
## `[Classes]`

### Debug

```java
    用于帮助调试Paho MQTT客户端问题的实用程序一旦初始化对dumpClientDebug的调用，将强制将任何内存跟踪与相关客户端和系统状态一起加载到主日志设施。
    转储进行时，不会执行客户端宽锁。 这意味着客户端状态的集合可能不一致，因为客户端在转储过程中仍可能正在处理工作。
```

### Strings

​    字符串助手

---

终焉