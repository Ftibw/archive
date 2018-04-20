/*
Page 19/308
程序清单1-1 非线程安全的数值序列生成器
*/
@NotThreadSafe
public class UnsafeSequence{
	private int value;
	/**返回一个唯一的数值。*/
	public int getNext(){
		return value++;
	}
}
/*
对于value++操作每个线程需要：
1.将主存中的value读入线程独有的工作空间（高速缓存或者累加器）
2.工作空间中将拷贝的数据加1
3.将工作空间的新值写入主存

若果有线程A和线程B“同时调用getNext()方法”,
当操作到共享变量value时,以如下执行流运行：
1）线程A将value拷贝到工作空间,线程A的时间片结束。
2）线程B将value拷贝到工作空间,线程B的时间片结束。
3）线程A的工作空间中缓存的value副本更新,线程A的时间片结束。
4）线程B的工作空间中缓存的value副本更新,线程B的时间片结束。
5）线程A将工作空间中的value新值写入主存,线程A时间片结束。
6）线程B将工作空间中的value新值写入主存,线程B时间片结束。
最终结果就是线程A与线程B写入到主存中的值相同,后者覆盖前者,
两个线程调用同一个计数方法得到了相同的值。

书中注释：由于存在指令重排序的可能,实际情况可能更糟糕。
*/
--程序清单1-1中演示的这类常见的并发安全问题,称为竞态条件(Race Condition)

--通过给getNext方法加锁可以保证value++操作的三个步骤只会在同一个线程中完成,避免了多线程的竞态条件


=====================================================================================================

要编写线程安全的代码,其核心在于要对状态访问操作进行管理,特别是对共享(Shared)状态和可变(Mutable)状态的访问。

[非正式意义上来说???]对象的状态是指存储在状态变量(例如实例或静态域)中的数据。对象的状态可能包括其他依赖对象的域。

对象的状态中包含了任何可能影响其外部可见行为的数据。

/*
状态：就是指数据
状态变量：就是指用于存储数据的对象成员属性,或者类的静态变量
对象的状态：就是指对象的成员变量或者静态变量中,存储的数据的总称,
对象的数据中可能包括该对象使用到的其他对象中的数据,
例如HashMap的数据不仅存储在HashMap对象本身中,同时也可以说是存储在许多Map.Entry对象中。

目前理解：
对象数据包含关系,不是说存储的数据量或者位置不同,仅仅是看待的角度不同。
而Map.Entry对象在外部被使用时,改变数据时也是对HashMap对象的数据的改变。

共享---变量中存储的数据可以被多个线程读取
可变---变量中存储的数据可以被多个线程修改

一个对象是否线程安全,取决于对象被多个线程访问的方式。
要使对象线程安全,需要采用同步机制来协同对对象数据的读取操作和写入操作

当多个线程读取某个用于存储数据的变量,并且有一个线程执行写入操作时,必须采用同步机制来协同这些线程对变量的读取。
*/

=====================================================================================================
如果当多个线程访问同一个可变的状态变量/*存储数据的变量*/时没有使用合适的同步,那么程序就会出现错误。
有三种方式可以修复这个问题:
1.不在线程之间共享该状态变量
2.将状态变量修改为不可变的变量
3.在访问状态变量时使用同步
程序的状态/*数据*/封装的越好，就越容易实现程序的线程的安全，并且代码的维护人员也越容易保持这种方式。
在编写并发代码时，应始终遵循一个原则:首先使代码正确运行，然后再提高代码的速度。

当多个线程访问某个类时，这个类始终都能表现出正确的行为，那么就称这个类是线程安全的。
/*
什么才叫正确呢？
又是书中的概念...正确性的含义是，某个类的行为与其规范完全一致(规范又是啥???)
*/

完全由线程安全类构成的程序不一定就是线程安全的，而线程安全类中也可以包含有非线程安全的类。
在任何情况中，只有当类中仅包含自己的状态/*数据*/时，线程安全类才是有意义的。
/*
个人理解：
对象的状态中包含了任何可能影响其外部可见行为的数据
如果对象对应的类中包含其他对象的数据
该类始终都能表现出正确的行为也无法保证其包含对象是否始终都能表现出正确的行为
*/

无状态对象一定是线程安全的
/*
因为没有存储数据的属性,
也不包含对其他类中数据属性的引用，
因此在被多个线程访问时不会有竞态条件
*/

++count这么一个操作是“读取-修改-写入”的序列操作,并且其结果数据依赖于之前的数据。

最常见的竞态条件类型就是:“先检查后执行(Check-Then-Act)”操作,即通过一个可能失效的观测结果来决定下一步的动作

使用“先检查后执行”的一种常见清空就是延迟初始化。
延迟初始化的目的是将对象的初始化操作推迟到实际被使用时才进行，同时确保只被初始化一次。

/*
Page 17/308
程序清单2-3 延时初始化中的竞态条件(不要这么做)
*/
@NotThreadSafe
public class LazyInitRace(){
	private ExpensiveObject instance = null;
	public ExpensiveObject getInstance(){
		if(instance==null)
			instance=new ExpensiveObject();
		return instance;
	}	
}

/*
A看到instance为null,因而创建了一个新的ExpensiveObject实例,而创建实例的过程开销大时间长,执行时时间片结束,
而此时B也看到了instance为null,因而再次创建了一个新的ExpensiveObject实例
*/

我们将“先检查后执行”以及“读取-修改-写入”等操作统称为复合操作。
为了确保线程安全性，上述复合操作必须以原子方式执行。

/*
一个无状态类中加入一个状态变量后的情况,最好使用线程安全对象替代状态变量

Page 18/308
程序清单2-4 使用AtomicLong类型的变量来统计已处理请求的数量
*/
@ThreadSafe
public class CountingFactorizer implements Servlet{
	private final AtomicLong count = new AtomicLong(0);
	public long getCount(){return count.get();}
	public void service(ServletRequest req,ServletResponse resp){
		BigInteger i = extractFromRequest(req);
		BigInteger[] factors =factor(i);
		count.incrementAndGet();
		encodeIntoResponse(resp,factors);
	}
}
在java.util.concurrent.atomic包中包含了一些原子变量类,用于实现在数值和对象引用上的原子状态转换。
用AtomicLong替代long类型的计数器，能够确保所有对计数器状态的访问操作都是原子的。
由于CountingFactorizer的状态就是计数器的状态，并且计数器是线程安全的，因此CountingFactorizer也是线程安全的。

/*
分别使用AtomicReference类型变量保存一个数值以及一个数组引用,该数组存储了数值的所有因子
并不能保证线程安全

Page 33/308
程序清单2-5 
*/
@NotThreadSafe
public class UnsafeCachingFactorizer implements Servlet{
	private final AtomicReference<BigInteger> lastNumber
		= new AtomicReference<BigInteger>();
	private final AtomicReference<BigInteger> lastFactors
		= new AtomicReference<BigInteger>();
		
		public void service(ServletRequest req,ServletResponse resp){
			BigInteger i = extractFromRequest(req);
			if(i.equals(lastNumber.get()))
				encodeIntoResponse(resp,lastFactors().get());
			else{
				BigInteger[] factors =factor[i];
				lastNumber.set(i);
				lastFactors.set(factors);
				encodeIntoResponse(resp,factors);
			}
		}
	
}
/*
else中
lastNumber.set(i);调用后
lastFactors.set(factors);调用前
可能有其他线程在
if中
执行lastFactors().get()获取到一个过期的值
*/
=====================================================================================================

/*
首先，看里面的 doSomething(e) 方法，这个方法应该是在 ThisEscape 中，不然就无法解释。也就是说，通过 doSomething(e) 方法可以修改 ThisEscape 中的属性或者调用 ThisEscape 中的其他方法。
例子中的代码，在多线程环境下，会出现这样一种情况：
线程 A 和线程 B 同时访问 ThisEscape 构造方法，这时线程 A 访问构造方法还为完成(可以理解为 ThisEscape 为初始化完全)，此时由于 this 逸出，导致 this 在 A 和 B 中都具有可见性，线程 B 就可以通过 this 访问 doSomething(e) 方法，导致修改 ThisEscape 的属性。也就是在 ThisEscape 还为初始化完成，就被其他线程读取，导致出现一些奇怪的现象。
这也就是 this 逸出。
通过 《Java 并发编程实战》 官网的书本 example 源码包，也证实了 doSomething 的确是 ThisEscape 中的方法。
 */
 
package net.jcip.examples;
 
/**
 * ThisEscape
 * <p/>
 * Implicitly allowing the this reference to escape
 *
 * @author Brian Goetz and Tim Peierls
 */
public class ThisEscape {
    public ThisEscape(EventSource source) {
        source.registerListener(new EventListener() {
            public void onEvent(Event e) {
                doSomething(e);
            }
        });
    }
 
    void doSomething(Event e) {
    }
 
 
    interface EventSource {
        void registerListener(EventListener e);
    }
 
    interface EventListener {
        void onEvent(Event e);
    }
 
    interface Event {
    }
}



