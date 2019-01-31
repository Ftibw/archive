CGLib核心类：

1、`net.sf.cglib.proxy.Enhancer`：主要增强类，通过字节码技术动态创建委托类的子类实例；

2、`net.sf.cglib.proxy.MethodInterceptor`：常用的方法拦截器接口，需要实现intercept方法，实现具体拦截处理；

`public java.lang.Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable`
`obj`：动态生成的代理对象
`method` ： 实际调用的方法
`args`：调用方法入参
`proxy`：
`net.sf.cglib.proxy.MethodProxy`：`Method`类的代理类，可以实现委托类对象的方法的调用；常用方法：`methodProxy.invokeSuper(proxy, args)`；在拦截方法内可以调用多次

> #### 我们继续以java动态代理实例的需求演示CGLib动态代理（调用外部类方法是，打印入参，完成后，打印结果），假设外部类没有基于接口编程，我们无法再使用java的动态代理实现这个需求，只能使用CGLib实现：

```
/**
 * 简单的外部类
 */
public class Target {
    /**
     * 字符串翻转
     * @param str
     * @return
     */
    public String methodOne(String str) {
        return new StringBuilder(str).reverse().toString();
    }
 
    /**
     * 两个整数乘积
     * @param a
     * @param b
     * @return
     */
    public int methodSecond(int a, int b) {
        return a*b;
    }
}
```

```

/**
 * 代理方法拦截器
 */
public class TestInterceptor implements MethodInterceptor {

    private Enhancer enhancer = new Enhancer();

    public Object getProxy(Class clazz) {
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(this);
        return enhancer.create();
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args,
            MethodProxy proxy) throws Throwable {
        //打印入参日志
        StringBuilder builder = new StringBuilder();
        if (args != null) {
            for (int i=0; i<args.length; ++i) {
                builder.append(",arg" + i + "=" + args[i].toString());
            }
        }
        System.out.println("Enter " + method.toString() + ",args:" + builder.toString());
        /**
        * 直接使用obj调用方法，会发生和java动态代理一样的无限循环调用
        * 因为这里obj和jdk的动态代理一样,就是生成的代理对象
        * 而jdk的代理对象没有invokeSuper方法,为了避免死循环,只能将被代理的target对象用构造器传入调用
        * 如果在构造时将原对象target传入,在这里调用,则调用的方法中如果用的了类中其他方法就不会被拦截。
        * 只有cglib通过代理对象去调用，同一个对象中嵌套的方法才会每个都被拦截
        */
        //Object result = method.invoke(obj, args); 
        Object result = proxy.invokeSuper(obj, args);
        //打印结果日志
        System.out.println("Leave " + method.toString() + ",result=" + result.toString());
        return result;
    }
}
```

```

/**
 * 测试类
 */
public class CglibProxyTest {

    public static void main(String[] args) {
        TestInterceptor ti = new TestInterceptor();
        Target proxy = (OtherOuterClass) ti.getProxy(Target.class);
        proxy.methodOne("abcdef");
        proxy.methodSecond(2, 4);
    }
}
```

