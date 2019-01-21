### 基于MQTT协议的 org.eclipse.paho.client.mqttv3 源码学习(二)										

**一、主要类介绍**

![](.\pic\main_mqtt_class.jpg)

**二、重点类代码分析**

对于长连接，一般是直接从消息的接收和发送类开始读，上面知道paho中消息发送和接收是在CommsSender和CommsReceiver实现的，

```java
ftibw注释:
pingOutstanding即ping未完成(0/大于0 未完成/完成)
quiesce	使停滞(使静默)
quiescing n.停顿
ClientState的send方法只是为了发送[接受到消息(接受到发布的消息/确认接受的消息)的响应]
```

所以直接看CommsSender代码。

```java
public void run() {
		final String methodName = "run";
		MqttWireMessage message = null;
		while (running && (out != null)) {
			try {
				message = clientState.get();
				log("sender 802:begin->" + message.toString());
				if (message != null) {
					// @TRACE 802=network send key={0} msg={1}
					log.fine(className, methodName, "802", new Object[] { message.getKey(), message });
                    //如果是确认消息，直接写回
					if (message instanceof MqttAck) {
						out.write(message);
						out.flush();
					} 
                    //不是确认消息，则获取消息的追踪token，同步后发送消息
                    else {
						MqttToken token = tokenStore.getToken(message);
						// While quiescing the tokenstore can be cleared so need
						// to check for null for the case where clear occurs
						// while trying to send a message.
                          //当停滞发生时，token仓库会被清除，清除发生的情况下发送消息时，需要检查token是							 否为null(是否被清除了)
						if (token != null) {
							synchronized (token) {
								out.write(message);
								try {
									out.flush();
								} catch (IOException ex) {
									// The flush has been seen to fail on
									// disconnect of a SSL socket
									// as disconnect is in progress this should
									// not be treated as an error
									if (!(message instanceof MqttDisconnect))
										throw ex;
								}
								clientState.notifySent(message);
							}
						}
					}
					log("sender 805:send success.");
				} else { // null message
					// @TRACE 803=get message returned null, stopping}
					log.fine(className, methodName, "803");
					running = false;
					log("sender 805:send empty.");
				}
			} catch (MqttException me) {
				log("sender 804：MqttException-> " + me.getLocalizedMessage());
				handleRunException(message, me);
			} catch (Exception ex) {
				log("sender 804：exception-> " + ex.getLocalizedMessage());
				handleRunException(message, ex);
			}
		} // end while
		// @TRACE 805=<
		log.fine(className, methodName, "805");
	}
```

代码可以看到，是直接一个线程无效循环获取消息然后发送，
 message = clientState.get();进入查看消息获取代码

```java
protected MqttWireMessage get() throws MqttException {
		final String methodName = "get";
		MqttWireMessage result = null;
		synchronized (queueLock) {
			while (result == null) {
				// If there is no work wait until there is work.
				// If the inflight window is full and no flows are pending
                  // wait until space is freed.
                
				// In both cases queueLock will be notified.
				if ((pendingMessages.isEmpty() && pendingFlows.isEmpty()) || 
					(pendingFlows.isEmpty() && actualInFlight >= this.maxInflight)) {
					try {
						long ttw = getTimeUntilPing();
						//@TRACE 644=wait for {0} ms for new work or for space in the inflight 							//window 
						log.fine(className,methodName, "644", new Object[] {new Long(ttw)});							queueLock.wait(getTimeUntilPing());
					} catch (InterruptedException e) {
					}
				}
				// Handle the case where not connected. This should only be the case if: 
				// - in the process of disconnecting / shutting down
				// - in the process of connecting
                 // connected为false时即断开连接，此时pendingFlows为空或者只有MqttConnect对象时
                 // 说明消息发送完毕，可以断开连接了
				if (!connected && 
	 				(pendingFlows.isEmpty() || !((MqttWireMessage)pendingFlows.elementAt(0) 						instanceof MqttConnect))) {
					//@TRACE 621=no outstanding flows and not connected
					log.fine(className,methodName,"621");
					return null;
				}
				// Check if there is a need to send a ping to keep the session alive. 
				// Note this check is done before processing messages. If not done first
				// an app that only publishes QoS 0 messages will prevent keepalive processing
				// from functioning.
				//	checkForActivity(); //Use pinger, don't check here
                
				// Now process any queued flows or messages
				if (!pendingFlows.isEmpty()) {
					// Process the first "flow" in the queue
					result = (MqttWireMessage)pendingFlows.elementAt(0);
					pendingFlows.removeElementAt(0);
					if (result instanceof MqttPubRel) {
						inFlightPubRels++;
						//@TRACE 617=+1 inflightpubrels={0}
						log.fine(className,methodName,"617", new Object[]{new 										Integer(inFlightPubRels)});
					}
                     //检查停顿锁，如果处于停顿状态就唤醒
					checkQuiesceLock();
				} else if (!pendingMessages.isEmpty()) {
					// If the inflight window is full then messages are not 
					// processed until the inflight window has space. 
					if (actualInFlight < this.maxInflight) {
						// The in flight window is not full so process the 
						// first message in the queue
						result = (MqttWireMessage)pendingMessages.elementAt(0);
						pendingMessages.removeElementAt(0);
						actualInFlight++;
						//@TRACE 623=+1 actualInFlight={0}
						log.fine(className,methodName,"623",new Object[]{new 										Integer(actualInFlight)});
					} else {
						//@TRACE 622=inflight window full
						log.fine(className,methodName,"622");				
					}
				}			
			}
		}
		return result;
	}
```

大致就是阻塞式获取消息 。//过时：在一个心跳时间内如果没有消息就一直阻塞，超过心跳间隔，自动往队列中加入心跳包 MqttPingReq.

```lua
fitbw注释:checkForActivity方法中检测存活状态，但是现在的get方法中已经注释掉了
1.ping发送成功但是响应超时认为服务器已经挂起，并且TCP层没有注意到 ，抛出异常
2.ping仍在飞行，未发送成功，且写超时超过2倍心跳间隔，认为服务器已经挂起，并且TCP层没有注意到，抛出异常
3.ping仍在飞行，未发送成功，但是刚刚超时不久，将ping命令消息放到[等待流队列pendingFlows]头部，然后通知get继续获取message返回给sender执行run方法。
```

由此可以看出 CommsSender 发送的消息主要是从 ClientState 这个类中get 出来，
`具体说就是从pendingMessages或pendingFlows这两个列表中获取出来的message。这些message都是在ClientState的send()方法中分别加入这两个列表的，除开send()方法之外，仅有checkForActivity()方法实现的心跳机制为了补发的刚刚超时的消息而加入pendingFlows列表`。

```java
//ClientState的send()方法不做实际的socket操作IO，只是将新的message加入等待队列。是在CommsSender的run()方法中进行实际socket操作IO---通过ClientState的get()方法将等待队列中的消息获取出来，完成实际发送。
```

而ClientState 这个类的作用在上面也说过：

保存正在发布的消息和将要发布的消息的状态信息，对应状态下消息进行必要的处理。

处理方式参见MQTT协议中客户端与服务器connent public unsubscribe，subscribe等消息的交互方式，

我们来看下这个类的主要成员 ：

![](.\pic\client_state.jpg)

查看ClientState 类说明 

```java
/**
 * The core of the client, which holds thestate information for pending and
 * in-flight messages.
 *
 * Messages that have been accepted fordelivery are moved between several objects
 * while being delivered.
 *
 * 1) When the client is not running messagesare stored in a persistent store that
 * implements the MqttClientPersistent Interface.The default is MqttDefaultFilePersistencew
 * which stores messages safely across failuresand system restarts. If no persistence
 * is specified there is a fall back toMemoryPersistence which will maintain the messages
 * while the Mqtt client isinstantiated.
 *
 * 2) When the client or specificallyClientState is instantiated the messages are
 * read from the persistent store into:
 * - outboundqos2 hashtable if a QoS 2 PUBLISH orPUBREL
 * - outboundqos1 hashtable if a QoS 1 PUBLISH
 * (see restoreState)
 *
 * 3) On Connect, copy messages from the outboundhashtables to the pendingMessages or
 * pendingFlows vector in messageidorder.
 * - Initial message publish goes onto the pendingmessagesbuffer.
 * - PUBREL goes onto the pendingflows buffer
 * (see restoreInflightMessages)
 *
 * 4) Sender thread reads messages from the pendingflowsand pendingmessages buffer
 * one at a time.  The message is removed from the pendingbufferbut remains on the
 * outbound* hashtable.  The hashtable is the place where thefull set of outstanding
 * messages are stored in memory. (Persistenceis only used at start up)
 * 
 * 5) Receiver thread - receives wire messages:
 *  - if QoS 1 thenremove from persistence and outboundqos1
 *  - if QoS 2 PUBRECsend PUBREL. Updating the outboundqos2 entry with the PUBREL
 *    andupdate persistence.
 *  - if QoS 2 PUBCOMPremove from persistence and outboundqos2 
 *
 * Notes:
 * because of the multithreaded natureof the client it is vital that any changes to this
 * class take concurrency into account.  For instance as soon as a flow / message isput on
 * the wire it is possible for the receivingthread to receive the ack and to be processing
 * the response before the sending side hasfinished processing.  For instance aconnect may
 * be sent, the conack received beforethe connect notify send has been processed!
 *
 */
```

大致意思就是,程序已运行，但是消息链路还没有开启的情况下，我们从通过MqttClientPersistent 这个接口读取缓存信息;
 Qos 2 的PUBLISH消息和 PUBREL 存储到outboundqos2 中，Qos 1的消息存到 outboundqos1 中。消息通过messgeId 作为key来缓存，
 messgeId的范围是1-65535，所以当缓存的值超做这个，消息就会替换掉。
每次发送的时候，客户端读取 pendingMessages，pendingFlows这两个vertor中的数据，MqttPublish消息存pendingMessages,非MqttPublish消息存储到 pendingFlows中，消息发送完成后移除,但是outboundQoS2 outboundQoS1等队列中的消息会保留直到接收线程中收到消息回应。
如果Qos 1 移除持久化数据和 outboundqos1 数据。
如果Qos 为2 的 PUBREC 则返回 PUBREL响应，更新持久化数据与outboundqos1中消息状态为 PUBREL
如果Qos 2 的PUBCOMP则移除持久化中数据和 outboundqos2中消息。
具体流程可以对比查看 ClientState send函数。

```java
public void send(MqttWireMessage message, MqttToken token) throws MqttException {
		final String methodName = "send";
		if (message.isMessageIdRequired() && (message.getMessageId() == 0)) {
			message.setMessageId(getNextMessageId());
		}
		if (token != null ) {
			try {
				token.internalTok.setMessageID(message.getMessageId());
			} catch (Exception e) {
			}
		}
			
		if (message instanceof MqttPublish) {
			synchronized (queueLock) {
				if (actualInFlight >= this.maxInflight) {
					//@TRACE 613= sending {0} msgs at max inflight window
					log.fine(className, methodName, "613", new Object[]{new Integer(actualInFlight)});
 
					throw new MqttException(MqttException.REASON_CODE_MAX_INFLIGHT);
				}
				
				MqttMessage innerMessage = ((MqttPublish) message).getMessage();
				//@TRACE 628=pending publish key={0} qos={1} message={2}
				log.fine(className,methodName,"628", new Object[]{new Integer(message.getMessageId()), new Integer(innerMessage.getQos()), message});
 
				switch(innerMessage.getQos()) {
					case 2:
						outboundQoS2.put(new Integer(message.getMessageId()), message);
						persistence.put(getSendPersistenceKey(message), (MqttPublish) message);
						break;
					case 1:
						outboundQoS1.put(new Integer(message.getMessageId()), message);
						persistence.put(getSendPersistenceKey(message), (MqttPublish) message);
						break;
				}
				tokenStore.saveToken(token, message);
				pendingMessages.addElement(message);
				queueLock.notifyAll();
			}
		} else {
			//@TRACE 615=pending send key={0} message {1}
			log.fine(className,methodName,"615", new Object[]{new Integer(message.getMessageId()), message});
			
			if (message instanceof MqttConnect) {
				synchronized (queueLock) {
					// Add the connect action at the head of the pending queue ensuring it jumps
					// ahead of any of other pending actions.
					tokenStore.saveToken(token, message);
					pendingFlows.insertElementAt(message,0);
					queueLock.notifyAll();
				}
			} else {
				if (message instanceof MqttPingReq) {
					this.pingCommand = message;
				}
				else if (message instanceof MqttPubRel) {
					outboundQoS2.put(new Integer(message.getMessageId()), message);
					persistence.put(getSendConfirmPersistenceKey(message), (MqttPubRel) message);
				}
				else if (message instanceof MqttPubComp)  {
					persistence.remove(getReceivedPersistenceKey(message));
				}
				
				synchronized (queueLock) {
					if ( !(message instanceof MqttAck )) {
						tokenStore.saveToken(token, message);
					}
					pendingFlows.addElement(message);
					queueLock.notifyAll();
				}
			}
		}
	}
```

```java
接下来我们在来查看下CommsReceiver接收端的代码 
```

```java
public void run() {
		final String methodName = "run";
		MqttToken token = null;
 
		while (running && (in != null)) {  //无限循环
			try {
				// @TRACE 852=network read message
				log.fine(className, methodName, "852");
				//阻塞式读取消息
				MqttWireMessage message = in.readMqttWireMessage();
				log("Receiver 852 message:" + message.toString());
				if (message instanceof MqttAck) {
					token = tokenStore.getToken(message);
					if (token != null) {
						synchronized (token) {
							// Ensure the notify processing is done under a lock
							// on the token
							// This ensures that the send processing can
							// complete before the
							// receive processing starts! ( request and ack and
							// ack processing
							// can occur before request processing is complete
							// if not!
							//通知回复确认消息
							clientState.notifyReceivedAck((MqttAck) message);
						}
					} else {
						// It its an ack and there is no token then something is
						// not right.
						// An ack should always have a token assoicated with it.
						throw new MqttException(MqttException.REASON_CODE_UNEXPECTED_ERROR);
					}
				} else {
					//通知有新的消息达到，我们进入此查看
					// A new message has arrived
					clientState.notifyReceivedMsg(message);
				}
			} catch (MqttException ex) {
				// @TRACE 856=Stopping, MQttException
				log.fine(className, methodName, "856", null, ex);
 
				log("Receiver 856：exception->" + ex.toString());
				running = false;
				// Token maybe null but that is handled in shutdown
				clientComms.shutdownConnection(token, ex);
			} catch (IOException ioe) {
				// @TRACE 853=Stopping due to IOException
				log.fine(className, methodName, "853");
 
				log("Receiver 853：exception->" + ioe.getLocalizedMessage());
				log("Receiver 853：exception->" + ioe.toString());
				running = false;
				// An EOFException could be raised if the broker processes the
				// DISCONNECT and ends the socket before we complete. As such,
				// only shutdown the connection if we're not already shutting
				// down.
				if (!clientComms.isDisconnecting()) {
					clientComms.shutdownConnection(token, new MqttException(
							MqttException.REASON_CODE_CONNECTION_LOST, ioe));
				} // else {
			}
		}
 
		// @TRACE 854=<
		log.fine(className, methodName, "854");
	}
```

MqttInputStream的readMqttWireMessage()方法详情：
只读取流中的第一个字节，实际上就是固定头部的byte1部分，然后无符号右移4位，即只保留了message type的4个字节，范围0~15(0和15保留)，一共代表了14中消息类型

```java
	/**
	 * Reads an <code>MqttWireMessage</code> from the stream.
	 */
	public MqttWireMessage readMqttWireMessage() throws IOException, MqttException {
		final String methodName ="readMqttWireMessage";
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		byte first = in.readByte();
		clientState.notifyReceivedBytes(1);
		
		byte type = (byte) ((first >>> 4) & 0x0F);
		if ((type < MqttWireMessage.MESSAGE_TYPE_CONNECT) ||
			(type > MqttWireMessage.MESSAGE_TYPE_DISCONNECT)) {
			// Invalid MQTT message type...
			throw ExceptionHelper.createMqttException(MqttException.REASON_CODE_INVALID_MESSAGE);
		}
		long remLen = MqttWireMessage.readMBI(in).getValue();
		bais.write(first);
		// bit silly, we decode it then encode it
		bais.write(MqttWireMessage.encodeMBI(remLen));
		byte[] packet = new byte[(int)(bais.size()+remLen)];
		readFully(packet,bais.size(),packet.length - bais.size());
		
		byte[] header = bais.toByteArray();
		System.arraycopy(header,0,packet,0, header.length);
		MqttWireMessage message = MqttWireMessage.createWireMessage(packet);
		// @TRACE 501= received {0} 
		log.fine(CLASS_NAME, methodName, "501",new Object[] {message});
		return message;
	}
```

点击进入  clientState.notifyReceivedMsg(message)

```java
protected void notifyReceivedMsg(MqttWireMessage message) throws MqttException {
		final String methodName = "notifyReceivedMsg";
		this.lastInboundActivity = System.currentTimeMillis();
 
		// @TRACE 651=received key={0} message={1}
		log.fine(className, methodName, "651", new Object[] {
				new Integer(message.getMessageId()), message });
		
		if (!quiescing) {
			if (message instanceof MqttPublish) {
				MqttPublish send = (MqttPublish) message;
				switch (send.getMessage().getQos()) {
				case 0:
				case 1:
					if (callback != null) {
						callback.messageArrived(send);
					}
					break;
				case 2:
					persistence.put(getReceivedPersistenceKey(message),
							(MqttPublish) message);
					inboundQoS2.put(new Integer(send.getMessageId()), send);
					this.send(new MqttPubRec(send), null);
				}
			} else if (message instanceof MqttPubRel) {
				MqttPublish sendMsg = (MqttPublish) inboundQoS2
						.get(new Integer(message.getMessageId()));
				if (sendMsg != null) {
					if (callback != null) {
						callback.messageArrived(sendMsg);
					}
				} else {
					// Original publish has already been delivered.
					MqttPubComp pubComp = new MqttPubComp(message
							.getMessageId());
					this.send(pubComp, null);
				}
			}
		}
	}
```

```
 这里可以看出，如果是Qos 1的消息或者Qos 2 MqttPubRel，我们直接回调告诉消息已到达，点击进入 callback.messageArrived(sendMsg);	
```

```java
public void messageArrived(MqttPublish sendMessage) {
		final String methodName = "messageArrived";
		if (mqttCallback != null) {
			// If we already have enough messages queued up in memory, wait
			// until some more queue space becomes available. This helps
			// the client protect itself from getting flooded by messages
			// from the server.
			synchronized (spaceAvailable) {
				if (!quiescing && messageQueue.size() >= INBOUND_QUEUE_SIZE) {
					try {
						// @TRACE 709=wait for spaceAvailable
						log.fine(className, methodName, "709");
						spaceAvailable.wait();
					} catch (InterruptedException ex) {
					}
				}
			}
			if (!quiescing) {
				messageQueue.addElement(sendMessage);
				// Notify the CommsCallback thread that there's work to do...
				synchronized (workAvailable) {
					// @TRACE 710=new msg avail, notify workAvailable
					log.fine(className, methodName, "710");
					workAvailable.notifyAll();
				}
			}
		}
	}
```

```
所做的操作就是将数据插入到 消息队列中，然后唤醒 workAvailable 这个锁，在 CommsCallback类中所有这个锁对应的地方，可以查看到
```

```java
 	public void run() {
		final String methodName = "run";
		while (running) {
			try {
				// If no work is currently available, then wait until there is
				// some...
				try {
					synchronized (workAvailable) {
						if (running & messageQueue.isEmpty() && completeQueue.isEmpty()) {
							// @TRACE 704=wait for workAvailable
							log.fine(className, methodName, "704");
							workAvailable.wait();
						}
					}
				} catch (InterruptedException e) {
				}
 
				if (running) {
					// Check for deliveryComplete callbacks...
					if (!completeQueue.isEmpty()) {
						// First call the delivery arrived callback if needed
						MqttToken token = (MqttToken) completeQueue.elementAt(0);
						handleActionComplete(token);
						completeQueue.removeElementAt(0);
					}
 
					// Check for messageArrived callbacks...
					if (!messageQueue.isEmpty()) {
						// Note, there is a window on connect where a publish
						// could arrive before we've
						// finished the connect logic.
						MqttPublish message = (MqttPublish) messageQueue.elementAt(0);
 
						handleMessage(message);
						messageQueue.removeElementAt(0);
					}
				}
 
				if (quiescing) {
					clientState.checkQuiesceLock();
				}
 
				synchronized (spaceAvailable) {
					// Notify the spaceAvailable lock, to say that there's now
					// some space on the queue...
 
					// @TRACE 706=notify spaceAvailable
					log.fine(className, methodName, "706");
					spaceAvailable.notifyAll();
				}
			} catch (Throwable ex) {
				// Users code could throw an Error or Exception e.g. in the case
				// of class NoClassDefFoundError
				// @TRACE 714=callback threw exception
				log.fine(className, methodName, "714", null, ex);
				running = false;
				clientComms.shutdownConnection(null, new MqttException(ex));
			}
		}
	}
```

```java
MqttClient是对MqttAsyncClient的包装类，MqttClient构造时即构造了MqttAsyncClient，MqttAsyncClient的构造方法中创建了ClientComms对象
```

```java
	/* (non-Javadoc)
	 * @see org.eclipse.paho.client.mqttv3.IMqttAsyncClient#connect(org.eclipse.paho.client.mqttv3.MqttConnectOptions, java.lang.Object, org.eclipse.paho.client.mqttv3.IMqttActionListener)
	 */
	public IMqttToken connect(MqttConnectOptions options, Object userContext, IMqttActionListener callback)
			throws MqttException, MqttSecurityException {
		final String methodName = "connect";
		if (comms.isConnected()) {
			throw ExceptionHelper.createMqttException(MqttException.REASON_CODE_CLIENT_CONNECTED);
		}
		if (comms.isConnecting()) {
			throw new MqttException(MqttException.REASON_CODE_CONNECT_IN_PROGRESS);
		}
		if (comms.isDisconnecting()) {
			throw new MqttException(MqttException.REASON_CODE_CLIENT_DISCONNECTING);
		}
		if (comms.isClosed()) {
			throw new MqttException(MqttException.REASON_CODE_CLIENT_CLOSED);
		}

		this.connOpts = options;
		this.userContext = userContext;
		final boolean automaticReconnect = options.isAutomaticReconnect();

		// @TRACE 103=cleanSession={0} connectionTimeout={1} TimekeepAlive={2} userName={3} password={4} will={5} userContext={6} callback={7}
		log.fine(CLASS_NAME,methodName, "103",
				new Object[]{
				Boolean.valueOf(options.isCleanSession()),
				new Integer(options.getConnectionTimeout()),
				new Integer(options.getKeepAliveInterval()),
				options.getUserName(),
				((null == options.getPassword())?"[null]":"[notnull]"),
				((null == options.getWillMessage())?"[null]":"[notnull]"),
				userContext,
				callback });
		comms.setNetworkModules(createNetworkModules(serverURI, options));
		comms.setReconnectCallback(new MqttCallbackExtended() {
			
			public void messageArrived(String topic, MqttMessage message) throws Exception {
			}
			public void deliveryComplete(IMqttDeliveryToken token) {
			}
			public void connectComplete(boolean reconnect, String serverURI) {
			}

			public void connectionLost(Throwable cause) {
				if(automaticReconnect){
						// Automatic reconnect is set so make sure comms is in resting state
						comms.setRestingState(true);
						reconnecting = true;
						startReconnectCycle();
					}
			}
		});
		
		// Insert our own callback to iterate through the URIs till the connect succeeds
		MqttToken userToken = new MqttToken(getClientId());
		ConnectActionListener connectActionListener = new ConnectActionListener(this, persistence, comms, options, userToken, userContext, callback, reconnecting);
		userToken.setActionCallback(connectActionListener);
		userToken.setUserContext(this);

		// If we are using the MqttCallbackExtended, set it on the connectActionListener
		if(this.mqttCallback instanceof MqttCallbackExtended){
			connectActionListener.setMqttCallbackExtended((MqttCallbackExtended)this.mqttCallback);
		}

		comms.setNetworkModuleIndex(0);
		connectActionListener.connect();

		return userToken;
	}
```

ConnectActionListener的connect()方法中创建了token，最终调用的是ClientComms中的connect(..)方法传入了options与token

```java
	/**
	 * Sends a connect message and waits for an ACK or NACK.
	 * Connecting is a special case which will also start up the
	 * network connection, receive thread, and keep alive thread.
	 */
	public void connect(MqttConnectOptions options, MqttToken token) throws MqttException {
		final String methodName = "connect";
		synchronized (conLock) {
			if (isDisconnected() && !closePending) {
				//@TRACE 214=state=CONNECTING
				log.fine(CLASS_NAME,methodName,"214");

				conState = CONNECTING;

				conOptions = options;

                MqttConnect connect = new MqttConnect(client.getClientId(),
                        conOptions.getMqttVersion(),
                        conOptions.isCleanSession(),
                        conOptions.getKeepAliveInterval(),
                        conOptions.getUserName(),
                        conOptions.getPassword(),
                        conOptions.getWillMessage(),
                        conOptions.getWillDestination());

                this.clientState.setKeepAliveSecs(conOptions.getKeepAliveInterval());
                this.clientState.setCleanSession(conOptions.isCleanSession());
                this.clientState.setMaxInflight(conOptions.getMaxInflight());

				tokenStore.open();
				ConnectBG conbg = new ConnectBG(this, token, connect);
				conbg.start();
			}
			else {
				// @TRACE 207=connect failed: not disconnected {0}
				log.fine(CLASS_NAME,methodName,"207", new Object[] {new Byte(conState)});
				if (isClosed() || closePending) {
					throw new MqttException(MqttException.REASON_CODE_CLIENT_CLOSED);
				} else if (isConnecting()) {
					throw new MqttException(MqttException.REASON_CODE_CONNECT_IN_PROGRESS);
				} else if (isDisconnecting()) {
					throw new MqttException(MqttException.REASON_CODE_CLIENT_DISCONNECTING);
				} else {
					throw ExceptionHelper.createMqttException(MqttException.REASON_CODE_CLIENT_CONNECTED);
				}
			}
		}
	}
```

MqttAsyncClient的createNetworkModule方法完成socket连接broker(network module)，ClientComms的私有内部类ConnectBG开启后台线程，然后启动receiver、sender、callback线程实现mqtt协议，其中receiver、sender的run方法中获取的在线状态消息都是从ClientState中获取的，而internalSend()方法本质还是调用的ClientState的send方法。

```java
// Kick off the connect processing in the background so that it does not block. For instance
	// the socket could take time to create.
	private class ConnectBG implements Runnable {
		ClientComms 	clientComms = null;
		Thread 			cBg = null;
		MqttToken 		conToken;
		MqttConnect 	conPacket;

		ConnectBG(ClientComms cc, MqttToken cToken, MqttConnect cPacket) {
			clientComms = cc;
			conToken 	= cToken;
			conPacket 	= cPacket;
			cBg = new Thread(this, "MQTT Con: "+getClient().getClientId());
		}

		void start() {
			cBg.start();
		}

		public void run() {
			final String methodName = "connectBG:run";
			MqttException mqttEx = null;
			//@TRACE 220=>
			log.fine(CLASS_NAME, methodName, "220");

			try {
				// Reset an exception on existing delivery tokens.
				// This will have been set if disconnect occured before delivery was
				// fully processed.
				MqttDeliveryToken[] toks = tokenStore.getOutstandingDelTokens();
				for (int i=0; i<toks.length; i++) {
					toks[i].internalTok.setException(null);
				}

				// Save the connect token in tokenStore as failure can occur before send
				tokenStore.saveToken(conToken,conPacket);

				// Connect to the server at the network level e.g. TCP socket and then
				// start the background processing threads before sending the connect
				// packet.
				NetworkModule networkModule = networkModules[networkModuleIndex];
				networkModule.start();
				receiver = new CommsReceiver(clientComms, clientState, tokenStore, networkModule.getInputStream());
				receiver.start("MQTT Rec: "+getClient().getClientId());
				sender = new CommsSender(clientComms, clientState, tokenStore, networkModule.getOutputStream());
				sender.start("MQTT Snd: "+getClient().getClientId());
				callback.start("MQTT Call: "+getClient().getClientId());				
				internalSend(conPacket, conToken);
			} catch (MqttException ex) {
				//@TRACE 212=connect failed: unexpected exception
				log.fine(CLASS_NAME, methodName, "212", null, ex);
				mqttEx = ex;
			} catch (Exception ex) {
				//@TRACE 209=connect failed: unexpected exception
				log.fine(CLASS_NAME, methodName, "209", null, ex);
				mqttEx =  ExceptionHelper.createMqttException(ex);
			}

			if (mqttEx != null) {
				shutdownConnection(conToken, mqttEx);
			}
		}
	}
```

```java
所有的消息发送过程：
web层自定义sendMsg方法 ↓ 
MqttClient#publish(String topic, MqttMessage message) ↓
MqttAsyncClient#publish(String topic, MqttMessage message) ↓
MqttAsyncClient#publish(String topic, MqttMessage message, Object null, IMqttActionListener null) ↓ ClientComms#sendNoWait(MqttWireMessage message, MqttToken token) ↓
ClientComms#internalSend(MqttWireMessage message, MqttToken token) ↓ 
ClientState#send(MqttWireMessage message, MqttToken token)
```

骚操作介绍

```java
		mqttClient.setCallback(new MqttCallbackExtended() {
			@Override
			public void connectComplete(boolean reconnect, String serverURI) {
				// when connect success,do sub topic
				System.out.println("clientCallbackReciver connect success");
				try {
					final String topicFilter[] = { topic + "/p2p" };
					final int[] qos = { MQconf.qosLevel };
   //MqttCallbackExtended有个连接完成的回调...在连接成功的回调中进行主题订阅666,找了半天了在哪订阅的...
					mqttClient.subscribe(topicFilter, qos);
				} catch (MqttException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void connectionLost(Throwable throwable) {
				System.out.println("reciver LOSS CONNECT");
				throwable.printStackTrace();
			}

			@Override
			public void messageArrived(String s, MqttMessage mqttMessage) {
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
			}
```

**三、总结**
 ClientState 类中, pendingMessages容器存放MqttPubish消息，而pendingFlows消息则存放 MqttPubRel，MqttConnect，MqttPingReq，MqttAck等
 Send 方法将消息放入到容器中，同时唤醒等待发送的线程。
 CommsSender 这个发送线程 通过 ClientStatele类get() 方法等待 pendingMessages和pendingFlows 这个这两个队列中放入消息，
 如果有消息放入，且同时被唤醒，那么就执行消息发送操作。
 CommsSender 接收线程中阻塞式获取消息根据不通的消息类型已经Qos level,通过CommsCallback及ClientState中notifyReceivedMsg 来执行相应的操作。