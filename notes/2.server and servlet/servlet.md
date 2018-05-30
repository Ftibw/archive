以下内容中，tomcat的版本为8.0.28。

Tomcat中对应的`ServletContext`实现是`ApplicationContext`，其注释内容为：

```
/**
 * Standard implementation of <code>ServletContext</code> that represents
 * a web application's execution environment.  An instance of this class is
 * associated with each instance of <code>StandardContext</code>.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 */
```

​	`Tomcat`惯用`Facade`方式（`Facade`外观设计模式，此时感觉和`wrapper`装饰模式差不多），因此在`web`应用程序中获取到`ServletContext`实际上是一个`ApplicationContextFacade`对象，对`ApplicationContext`实例进行了封装。而`ApplicationContext` 实例中含有`Tomcat`的`Context`容器实例（`StandardContext`实例，也就是在`server.xml`中配置的`Context`节点），以此来获取/操作`Tomcat`容器内部的一些信息，例如获取/注册`servlet`等。

```
ServletContext instance's class is org.apache.catalina.core.ApplicationContextFacade

ApplicationContext instance's class is org.apache.catalina.core.ApplicationContext

StandardContext instance's class is org.apache.catalina.core.StandardContext
```

​	`servlet`中的`getServletContext()`所得到的是一个`ApplicationContextFacade`对象，`ApplicationContextFacade`类是`ApplicationContext`类的包装类，而`ApplicationContext`类又是`StandardContext`类的包装类。本质上`servlet`中的`context`是从`Catalina`容器中的`context`包装演化而来的。
	`servlet`中的`context`主要作用是保存全局参数和`servlet`通信，`Catalina`中的`context`是`wrapper`容器，`wrapper`又与`servlet`一一对应。`Catalina`容器中的`context`是给`servlet`中的`context`提供服务的。













