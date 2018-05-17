###1.CGLIB反编译生成源码

```
public class HelloWorld {

    public void hello() {
        System.out.println("hello world");
    }
}
```

```
public class HelloInterceptor implements MethodInterceptor {

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println("before");
        Object ret = methodProxy.invokeSuper(o, objects);
        System.out.println("after");
        return ret;
    }
}
```

```
public static void main(String[] args) {
        System.setProperty(
                DebuggingClassWriter
                        .DEBUG_LOCATION_PROPERTY
                , "D:\\\\Code\\\\classes"	//反编译生成的class文件存放的路径
        );
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(HelloWorld.class);
        enhancer.setCallback(new HelloInterceptor());
        HelloWorld helloWorld = (HelloWorld) enhancer.create();
        helloWorld.hello();
    }
```

