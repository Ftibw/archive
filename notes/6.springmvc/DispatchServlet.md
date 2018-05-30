##1.DIspatchServlet

`DispatcherServlet`覆写了`FrameworkServlet中的onRefresh`方法 

```
	/**
	 * Initialize the strategy objects that this servlet uses.
	 * <p>May be overridden in subclasses in order to initialize further strategy objects.
	 */
	 //初始化各种策略接口的实现类
	protected void initStrategies(ApplicationContext context) {
		initMultipartResolver(context);
		initLocaleResolver(context);
		initThemeResolver(context);
		initHandlerMappings(context);
		initHandlerAdapters(context);
		
		//异常处理初始化initHandlerExceptionResolvers方法
		initHandlerExceptionResolvers(context);
		
		//请求映射处理初始化initHandlerMappings方法
		initRequestToViewNameTranslator(context);
		
		//视图处理初始化initViewResolvers方法
		initViewResolvers(context);
		initFlashMapManager(context);
	}
```

### 总结

```
各个Servlet的作用：
1. HttpServletBean
　　主要做一些初始化的工作，将web.xml中配置的参数设置到Servlet中。比如servlet标签的子标签init-param标签中配置的参数。

2. FrameworkServlet
　　将Servlet与Spring容器上下文关联。其实也就是初始化FrameworkServlet的属性webApplicationContext，这个属性代表SpringMVC上下文，它有个父类上下文，既web.xml中配置的ContextLoaderListener监听器初始化的容器上下文。

3. DispatcherServlet 
　　初始化各个功能的实现类。比如异常处理、视图处理、请求映射处理等。
```



