###1.异步io中将数据从内核缓冲区复制到用户缓冲区难道不需要`cpu`资源吗？

 ![img](https://pic4.zhimg.com/50/v2-41d3c97c5eb44c74e0d41cea90fb38fa_hd.jpg)

 这个问题要讨论清楚其实挺复杂，因为情况太多了。

首先，传统意义上来说，数据从内核到用户（或者反过来），确实是要经过CPU资源来实现对内存数据的拷贝的。
但是呢，也正是因为这种开销很常见，而且也不小，所以现在已经有很多“零拷贝（zero-copy）”方案出来了。而且很多io相关的场景都已经能用得上了，例如说direct io、mmap、sendfile（splice）等，甚至readv/writev也可以在一定角度上看作是类似方向上努力的方案。

然后再说回你这个图。
从一般角度来说，异步io把任务提交给内核之后，说：“进程继续执行”，是没有问题的。因为从逻辑上说，这个进程确实是继续执行了。
哪怕不考虑多核/多cpu的情况，即使是单cpu单核，这么说也是ok的。

再说一下，你在思考这类问题时，最好明确区分“逻辑层面”和“实现层面”。
例如说你那个图说的“进程继续执行”，是在逻辑层面说的。而你考虑的内核到用户拷贝数据而使得进程丧失cpu资源，更多的是在实现层面考虑问题。
其实你说内核也要消耗cpu资源去做这个做那个，和“进程继续执行”无关，因为这纯粹是内核的进程调度和cpu资源管理的问题。任何一个支持多进程的操作系统内核，都会随时因为各种原因而把进程挂起或者唤醒，但是这并不代表这些进程（在逻辑层面）就不在执行了。
所以，如果你认为这个进程因为被内核占用来拷贝内存，因此被挂起（不再占有cpu时间）而觉得它“不在继续执行”。那即使不存在这个情况，这个进程也可能因为其他进程的抢占而被挂起。因此，如果使用同样的逻辑，那么你实际上不可以宣称进程在执行任何操作之后，依然能够确保“继续执行”——因为（在实现层面）你无法保证你的进程在下一个tick不会被内核挂起。

------------

###2.IO 的底层实现问题

IO涉及到的底层的概念大致如下：

1) 缓冲区操作。2) 内核空间与用户空间。3) 虚拟内存。4) 分页技术。

一，虚拟存储器

虚拟存储器是硬件异常(缺页异常)、硬件地址翻译、主存、磁盘文件和内核软件的完美交互，它为每个进程提供了一个大的、一致的和私有的地址空间。
 虚拟存储器的三大能力：①将主存看成是一个存储在磁盘上的地址空间的高速缓存。②为每个进程提供了一个一致的地址空间。③保护每个进程的地址空间不被其他进程破坏。
 虚拟内存的两大好处：① 一个以上的虚拟地址可指向同一个物理内存地址。② 虚拟内存空间可大于实际可用的硬件内存。

二，用户空间与内核空间
 设虚拟地址为32位，那么虚拟地址空间的范围为0~4G。操作系统将这4G分为二部分，将最高的1G字节(虚拟地址范围为：0xC0000000-0xFFFFFFFF)供内核使用，称为内核空间。而将较低的3G字节供各个进程使用，称为用户空间。
 每个进程可以通过系统调用进入内核，因为内核是由所有的进程共享的。对于每一个具体的进程，它看到的都是4G大小的虚拟地址空间，即相当于每个进程都拥有一个4G大小的虚拟地址空间。

三，IO操作
 一般IO缓冲区操作：
 1) 用户进程使用read()系统调用，要求其用户空间的缓冲区被填满。
 2) 内核向磁盘控制器硬件发命令，要求从磁盘读入数据。
 3) 磁盘控制器以DMA方式(数据不经过CPU)把数据复制到内核缓冲区。
 4) 内核将数据从内核缓冲区复制到用户进程发起read()调用时指定的用户缓冲区。

![img](https://img-blog.csdn.net/20160118192644751?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQv/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

从上图可以看出：磁盘中的数据是先读取到内核的缓冲区中。然后再从内核的缓冲区复制到用户的缓冲区。为什么会这样呢？
 因为用户空间的进程是不能直接硬件的(操作磁盘控制器)。磁盘是基于块存储的硬件设备，它一次操作固定大小的块，而用户请求请求的可能是任意大小的数据块。因此，将数据从磁盘传递到用户空间，由内核负责数据的分解、再组合。
 内存映射IO：就是复用一个以上的虚拟地址可以指向同一个物理内存地址。将内核空间的缓冲区地址(内核地址空间)映射到物理内存地址区域，将用户空间的缓冲区地址(用户地址空间)也映射到相同的物理内存地址区域。从而数据不需要从内核缓冲区映射的物理内存地址移动到用户缓冲区映射的物理内存地址了。
 要求：①用户缓冲区与内核缓冲区必须使用相同的页大小对齐。②缓冲区的大小必须是磁盘控制器块大小(512字节磁盘扇区)的倍数---因为磁盘是基于块存储的硬件设备，一次只能操作固定大小的数据块。
 用户缓冲区按页对齐，会提高IO的效率---这也是为什么在JAVA中new 一个字节数组时，指定的大小为2的倍数(4096)的原因吧。

![img](https://img-blog.csdn.net/20160118192743878?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQv/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

四，JAVA中的IO，本质上是把数据移进或者移出缓冲区。
 read()和write()系统调用完成的作用是：把内核缓冲区映射的物理内存空间中的数据 拷贝到 用户缓冲区映射的物理内存空间中。
 因此，当使用内存映射IO时，可视为：用户进程直接把文件数据当作内存，也就不需要使用read()或write()系统调用了。
 当发起一个read()系统调用时，根据待读取的数据的位置生成一个虚拟地址(用户进程使用的是虚拟地址)，由MMU转换成物理地址，若内核中没有相应的数据，产生一个缺页请求，内核负责页面调入从而将数据从磁盘读取到内核缓冲区映射的物理内存中。对用户程序而言，这一切都是在不知不觉中进行。
 总之，从根本上讲数据从磁盘装入内存是以页为单位通过分页技术装入内存的。

五，JAVA NIO中的直接缓存和非直接缓存

直接缓存：不是分配于堆上的存储，位于JVM之外，它不受JAVA的GC管理，相当于内核缓冲区。非直接缓存：建立在JAVA堆上的缓存，受JVM管理，相当于用户缓冲区。

根据上面第三点，将直接缓存中的数据写入通道的速度要快于非直接缓存。因为，连接到通道的另一端是文件(磁盘，FileChannel)或者网络(Socket通道)，这些都是某种形式上的硬件。那么，对于非直接缓存而言，数据从缓冲区传递到硬件，要经过内核缓冲区中转。而对于直接缓存而言，就不需要了，因为直接缓存已经直接映射到内核缓冲区了。

[直接buffer和非直接buffer区别。](http://eyesmore.iteye.com/blog/1133335)

---------------------------------------

###3.`zorecopy`

零拷贝技术

```
We can further reduce the data duplication done by the kernel if the underlying network interface card supports 
gather operations. In Linux kernels 2.4 and later, the socket buffer descriptor was modified to accommodate this requirement. 
This approach not only reduces multiple context switches but also eliminates the duplicated data copies that 
require CPU involvement. 
```

**如果底层的网络硬件以及操作系统支持**，还可以进一步减少数据拷贝次数 以及 CPU干预次数。

![img](https://images2015.cnblogs.com/blog/715283/201608/715283-20160804161321559-485177985.png)

从上图看出：这里一共只有两次拷贝 和 两次上下文切换。而且这两次拷贝都是`DMA` `copy`，并不需要CPU干预(严谨一点的话就是不完全需要吧.)。

整个过程如下：

用户程序执行 `transferTo()`方法，导致一次系统调用，从用户态切换到内核态。完成的动作是：`DMA`将数据从磁盘中拷贝到`Read buffer`

用一个描述符标记此次待传输数据的地址以及长度（文件描述符表中，文件描述符为索引，记录了文件结构体指针），可以通过改变文件描述符而不改变表项（把同一个data从一个文件描述符传输到另一个文件描述符 ），让`DMA`直接把数据从`Read buffer` 传输到 `NIC(network interface card) buffer`。数据拷贝过程都不用CPU干预了。

最后从内核态上下文切换回到用户进程(用户进程与内核空间的切资源换耗远超过用户空间中的函数调用)

###4.IO多路复用

IO多路复用模型是建立在内核提供的多路分离函数select基础之上的，使用select函数可以避免同步非阻塞IO模型中轮询等待的问题。

![img](https://images0.cnblogs.com/blog/405877/201411/142332187256396.png)

图3 多路分离函数select

如图3所示，用户首先将需要进行IO操作的socket添加到select中，然后阻塞等待select系统调用返回。当数据到达时，socket被激活，select函数返回。用户线程正式发起read请求，读取数据并继续执行。

从流程上来看，使用select函数进行IO请求和同步阻塞模型没有太大的区别，甚至还多了添加监视socket，以及调用select函数的额外操作，效率更差。但是，使用select以后最大的优势是用户可以在一个线程内同时处理多个socket的IO请求。用户可以注册多个socket，然后不断地调用select读取被激活的socket，即可达到在**同一个线程内同时处理多个IO请求的目的**。而在同步阻塞模型中，必须通过多线程的方式才能达到这个目的。

用户线程使用select函数的伪代码描述为：

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
{

select(socket);

while(1) {

sockets = select();

for(socket in sockets) {

if(can_read(socket)) {

read(socket, buffer);

process(buffer);

}

}

}

}
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

 

其中while循环前将socket添加到select监视中，然后在while内一直调用select获取被激活的socket，一旦socket可读，便调用read函数将socket中的数据读取出来。

 

然而，使用select函数的优点并不仅限于此。虽然上述方式允许单线程内处理多个IO请求，但是每个IO请求的过程还是阻塞的（在select函数上阻塞），平均时间甚至比同步阻塞IO模型还要长。如果用户线程只注册自己感兴趣的socket或者IO请求，然后去做自己的事情，等到数据到来时再进行处理，则可以提高CPU的利用率。

IO多路复用模型使用了Reactor设计模式实现了这一机制。

![img](https://images0.cnblogs.com/blog/405877/201411/142332350853195.png)

图4 Reactor设计模式

如图4所示，EventHandler抽象类表示IO事件处理器，它拥有IO文件句柄Handle（通过get_handle获取），以及对Handle的操作handle_event（读/写等）。继承于EventHandler的子类可以对事件处理器的行为进行定制。Reactor类用于管理EventHandler（注册、删除等），并使用handle_events实现事件循环，不断调用同步事件多路分离器（一般是内核）的多路分离函数select，只要某个文件句柄被激活（可读/写等），select就返回（阻塞），handle_events就会调用与文件句柄关联的事件处理器的handle_event进行相关操作。

![img](https://images0.cnblogs.com/blog/405877/201411/142333254136604.png)

图5 IO多路复用

如图5所示，通过Reactor的方式，可以将用户线程轮询IO操作状态的工作统一交给handle_events事件循环进行处理。用户线程注册事件处理器之后可以继续执行做其他的工作（异步），而Reactor线程负责调用内核的select函数检查socket状态。当有socket被激活时，则通知相应的用户线程（或执行用户线程的回调函数），执行handle_event进行数据读取、处理的工作。由于select函数是阻塞的，因此多路IO复用模型也被称为异步阻塞IO模型。注意，这里的所说的阻塞是指select函数执行时线程被阻塞，而不是指socket。一般在使用IO多路复用模型时，socket都是设置为NONBLOCK的，不过这并不会产生影响，因为用户发起IO请求时，数据已经到达了，用户线程一定不会被阻塞。

用户线程使用IO多路复用模型的伪代码描述为：

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
void UserEventHandler::handle_event() {

if(can_read(socket)) {

read(socket, buffer);

process(buffer);

}

}

 

{

Reactor.register(new UserEventHandler(socket));

}
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

 

用户需要重写EventHandler的handle_event函数进行读取数据、处理数据的工作，用户线程只需要将自己的EventHandler注册到Reactor即可。Reactor中handle_events事件循环的伪代码大致如下。

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
Reactor::handle_events() {

while(1) {

sockets = select();

for(socket in sockets) {

get_event_handler(socket).handle_event();

}

}

}
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

 

事件循环不断地调用select获取被激活的socket，然后根据获取socket对应的EventHandler，执行器handle_event函数即可。

IO多路复用是最常使用的IO模型，但是其异步程度还不够“彻底”，因为它使用了会阻塞线程的select系统调用。因此IO多路复用只能称为异步阻塞IO，而非真正的异步IO。

--------------------------

### 5.java nio API

```
传统IO特点
阻塞点
server.accept();
inputStream.read(bytes);
单线程情况下只能有一个客户端
用线程池可以有多个客户端连接，但是非常消耗性能
```

NIO的特点

ServerSocketChannel	ServerSocket

SocketChannel		Socket

Selector

SelectionKey			`.*Channel`实例的`key`，`.*Channel`实例本身作为`value`

NIO的一些疑问

1、客户端关闭的时候会抛出异常，死循环
解决方案
		int read = channel.read(buffer);
		if(read > 0){
			byte[] data = buffer.array();
			String msg = new String(data).trim();
			System.out.println("服务端收到信息：" + msg);
			//回写数据
			ByteBuffer outBuffer = ByteBuffer.wrap("好的".getBytes());
			channel.write(outBuffer);// 将消息回送给客户端
		}else{
			System.out.println("客户端关闭");
			key.cancel();
		}

2、selector.select();阻塞，那为什么说nio是非阻塞的IO？

	selector.select()
	selector.select(1000);不阻塞
	selector.wakeup();也可以唤醒selector
	selector.selectNow();也可以立马返还，视频里忘了讲了，哈，这里补上


3、SelectionKey.OP_WRITE是代表什么意思

OP_WRITE表示底层缓冲区是否有空间，是则响应返还true



### 6.一些重点概念的解析

1、`Selector`接口的实现类`SelectorImpl`中有`selectionKeys`集合（`HashSet`）	

2、`SelectionKey`的子类`SelectionKeyImpl`中有`channel`成员和`selector`成员

3、`Selector`的`select()`函数由操作系统底层实现，系统调用后进入内核态，功能上类似从队列中读取消息的消费者，无消息时阻塞的等待生产者发布消息，发现消息后结束阻塞，带着文件描述符（很小的int值）返回用户态。用户线程拿到文件描述符后开始执行真正的读写（大多数情况下只>>>监听<<<读消息，因为数据往往不能及时到达，而写操作是基于可写消息，可写消息指的是缓冲区未满，可以写入数据，写入缓冲区基本上不可能写满，因为一但写入往往就被传输走了）

4、注意到3中的“监听”二字，只有监听了的`channel`才能生产和消费消息。监听是如何实现的？仍然是操作系统调度的，这个就暂不关心了，那如何开启监听功能呢？需要给`channel`所在的通道选择器`selector`注册指定事件。这样，当事件发生时才会被监听，产生消息，让`select()`函数返回事件对应的文件描述符。

5、等等，到了这里可能会疑惑，这个`channel`准确定义到底是什么？这里的channel泛指`ServerSocketChannel`、`SocketChannel`的实例，这些`channel`就是对`socket`的包装和改造。`channel`的核心成员属性是套接字`socket`和文件描述符`fd`，`socket`中有流，而流的本质就是通过流对象的成员文件描述符`fd`，映射内核中文件结构体，然后由操作系统完成`IO`操作。

![IO_df.PNG](https://github.com/Ftibw/archive/blob/memo-/IO_df.PNG?raw=true)

```
package NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * NIO服务端
 */
public class NIOServer {
	// 通道管理器
	private Selector selector;

	/**
	 * 获得一个ServerSocket通道，并对该通道做一些初始化的工作
	 * 
	 * @param port
	 *            绑定的端口号
	 * @throws IOException
	 */
	public void initServer(int port) throws IOException {
		// 获得一个ServerSocket通道
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		// 设置通道为非阻塞
		serverChannel.configureBlocking(false);
		// 将该通道对应的ServerSocket绑定到port端口
		serverChannel.socket().bind(new InetSocketAddress(port));
		// 获得一个通道管理器
		this.selector = Selector.open();
		// 将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_ACCEPT事件,注册该事件后，
		// 当该事件到达时，selector.select()会返回，如果该事件没到达selector.select()会一直阻塞。
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
	}

	/**
	 * 采用轮询的方式监听selector上是否有需要处理的事件，如果有，则进行处理
	 * 
	 * @throws IOException
	 */
	public void listen() throws IOException {
		System.out.println("服务端启动成功！");
		// 轮询访问selector
		while (true) {
			// 当注册的事件到达时，方法返回；否则,该方法会一直阻塞
			selector.select();
			// 获得selector中选中的项的迭代器，选中的项为注册的事件
			Iterator<?> ite = this.selector.selectedKeys().iterator();
			while (ite.hasNext()) {
				SelectionKey key = (SelectionKey) ite.next();
				// 删除已选的key,以防重复处理
				ite.remove();

				handler(key);
			}
		}
	}

	/**
	 * 处理请求
	 * 
	 * @param key
	 * @throws IOException
	 */
	public void handler(SelectionKey key) throws IOException {
		
		// 客户端请求连接事件
		if (key.isAcceptable()) {
			handlerAccept(key);
			// 获得了可读的事件
		} else if (key.isReadable()) {
			handelerRead(key);
		}
	}

	/**
	 * 处理连接请求
	 * 
	 * @param key
	 * @throws IOException
	 */
	public void handlerAccept(SelectionKey key) throws IOException {
		ServerSocketChannel server = (ServerSocketChannel) key.channel();
		// 获得和客户端连接的通道
		SocketChannel channel = server.accept();
		// 设置成非阻塞
		channel.configureBlocking(false);

		// 在这里可以给客户端发送信息哦
		System.out.println("新的客户端连接");
		// 在和客户端连接成功之后，为了可以接收到客户端的信息，需要给通道设置读的权限。
		channel.register(this.selector, SelectionKey.OP_READ);
	}

	/**
	 * 处理读的事件
	 * 
	 * @param key
	 * @throws IOException
	 */
	public void handelerRead(SelectionKey key) throws IOException {
		// 服务器可读取消息:得到事件发生的Socket通道
		SocketChannel channel = (SocketChannel) key.channel();
		// 创建读取的缓冲区
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		int read = channel.read(buffer);
		if(read > 0){
			byte[] data = buffer.array();
			String msg = new String(data).trim();
			System.out.println("服务端收到信息：" + msg);
			
			//回写数据
			ByteBuffer outBuffer = ByteBuffer.wrap("好的".getBytes());
			channel.write(outBuffer);// 将消息回送给客户端
		}else{
			System.out.println("客户端关闭");
			key.cancel();
		}
	}

	/**
	 * 启动服务端测试
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		NIOServer server = new NIOServer();
		server.initServer(8000);
		server.listen();
	}

}
```

​	