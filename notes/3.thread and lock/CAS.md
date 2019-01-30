##自底向上解析CAS

##1.`cmpxchg`汇编指令

<`cmpxchg a,b`>指令，a是首操作数，b是第二操作数

1.1.将累加寄存器中的值与首操作数比较，如果相等，则将第二操作数的值装载到首操作数，`zf`标志位置为1。

如果不等，首操作数的值装载到累加寄存器中然后将`zf`标志位置为0

```
大多数情况下，运算结果对ZF的修改，由结果是否为0决定。
若运算结果为0，则ZF=1;
若运算结果非0，则ZF=0.
```

1.2.`cmpxchg`  `dword  ptr  [edx],  ecx` 	`//解读`

```
inline jint Atomic::cmpxchg (jint exchange_value, volatile jint* dest, jint compare_value) {
	int mp = os::isMP(); 			//判断是否是多处理器，0代表单处理器，1代表多处理器 
    _asm { 
        mov edx, dest 	 			//将主内存地址写入数据寄存器
        mov ecx, exchange_value 	 //将新值(本线程修改后的值)写入计数寄存器
        mov eax, compare_value 		 //将旧值(本线程缓存的旧主存值)写入累加寄存器
        LOCK_IF_MP(mp) 				//根据处理器数量决定是否给下一条指令加主内存块的锁（本质是硬件上锁住此内存块的控制总线，解锁前不允许出现相同的电信号）
        cmpxchg dword ptr [edx], ecx //将主存的值与线程缓存的旧值比较
        						   //若相等，则将新值写入主内存
                                   	 //若不等，则将主存值写入累加寄存器
                                   	 //C语言的函数返回值，存放在eax中。环境是32位的。
								  //如果是64位，那么就是存放在eax和edx中了，高位在edx，低位在eax。
    } 
} 
// Adding a lock prefix to an instruction on MP machine 
// VC++ doesn't like the lock prefix to be on a single line 
// so we can't insert a label after the lock prefix. 
// By emitting a lock prefix, we can define a label after it. 
#define LOCK_IF_MP(mp) __asm cmp mp, 0 \ 
                       __asm je L0 \ 		//若是单线程则直接在L0指令处执行
                       __asm _emit 0xF0 \ 	//若是多线程则在L0指令前插入指令0xF0(内存加锁)，然后再执行
                       __asm L0:
//tip1：若cmp a,b指令判断a==b则ZF标志位置为1,a!=b则ZF标志位置为0
//tip2：JE指令是汇编里面的一个跳转指令,功能是在ZF等于1时进行跳转,也就是相等的时候跳转

```



##2.`JNI`库对`CAS`的支持

```
boolean compareAndSwapInt(
    Object paramObject,		//java的Atomic类实例
    long paramLong,			//实例的偏移地址
    int paramInt1,			//本线程中指定变量缓存的旧值
    int paramInt2			//本线程中指定变量修改后的值
);
```

`Unsafe`类中的`compareAndSwapInt`，是一个本地方法，该方法的实现位于`unsafe.cpp`中 。

`unsafe.cpp` 中的方法实现：

```
UNSAFE_ENTRY(
    jboolean, 
    Unsafe_CompareAndSwapInt(
        JNIEnv *env, 
        jobject unsafe,
        jobject obj,	
        jlong offset, 	
        jint e,
        jint x
    )
)
    UnsafeWrapper("Unsafe_CompareAndSwapInt");
    oop p = JNIHandles::resolve(obj);								 //似乎是解析对象获取段地址
    jint* addr = (jint *) index_oop_from_field_offset_long(p, offset);	//段地址+偏移地址定位内存地址
    return (jint)(Atomic::cmpxchg(x, addr, e)) == e;	 //调用cmpxchg函数然值与本线程缓存的旧值比较
UNSAFE_END
```



## 3.`java`中`AtomicInteger`的累加

自旋锁（循环的调用`CAS`直到成功为止）

```
public final int getAndAdd(int delta) { 
	return unsafe.getAndAddInt(this, valueOffset, delta); 
} 
//unsafe.getAndAddInt
public final int getAndAddInt(
    Object var1,	//AtomicInteger实例
    long var2,		//偏移地址
    int var4		//增量（正增量或负增量）
) {
    int var5;
    do {
        var5 = this.getIntVolatile(var1, var2);	//获取本线程缓存的旧值
    //若主存中的值与本线程缓存值不等则继续循环，再次读取缓存的值（此时缓存值已被更新为上次比较中主存的值了）
    } while(!this.compareAndSwapInt(var1, var2, var5, var5 + var4));	
    return var5;
}
```



##以下为CAS的扩展

## 4.`ABA`问题

当`A`线程操作变量`X`执行到`CAS`前挂起，

`B`线程操作`x=x+y`成功，

`c`线程操作`x=x-y`成功，

此时`A`才继续执行，那么主存中的`x`值对于`A`线程来说并没有改变，仍等于缓存中的`x`值，`CAS`将会执行成功，会造成无法预料的后果。

```
例如有一个选择结构基于x的状态执行逻辑：
使用单向链表实现栈S，存放了x与y，其中x在栈顶
判断如果栈顶为x将栈顶的x弹栈，使y成为栈顶。
A线程CAS前挂起，B线程将x、y弹栈然后将z、x入栈
然后A继续执行CAS成功，将x弹栈，y入栈。
此时因为y的next指针为null，z将丢失
```

解决方案：扩展`x`的数据宽度（比如从32位扩展到64位），使用多余的位来保存一个版本号，从而保证这个线程能正确的监测到值的更改。对于对象可以使用辅助对象来记录其版本号，例如`java`中的版本戳`AtomicStampedReference` 

## 5.无锁（lock_free）

如果`ABA`问题中`x`变量保存的不是数值，而是一个指针变量（对象地址），指向了我们实际使用的对象，那么基于`CAS`，我们就可以实现一个无锁的且线程安全的对象。

实现思路极其简单：创建（只修改部分状态时从主内存复制旧对象再微调，修改全部状态时可以新new一个对象）一个新对象保存修改后的状态，`CAS`执行成功后，则将主内存`x`值改为新对象的地址。（思路和并发实战编程中的

`final volatile+`克隆`/new`实现线程有异曲同工之妙）

