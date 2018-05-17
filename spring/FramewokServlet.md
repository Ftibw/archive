1.

重写`HttpServletBean`的`initServletBean()`方法，`init()`方法中在bean的属性注入完成后调用该逻辑：

给`servlet`创建并绑定`WebApplicationContext`

```
/**
	 * Overridden method of {@link HttpServletBean}, invoked after any bean properties
	 * have been set. Creates this servlet's WebApplicationContext.
	 */
	@Override
	protected final void initServletBean() throws ServletException {
		getServletContext().log("Initializing Spring FrameworkServlet '" + getServletName() + "'");
		if (this.logger.isInfoEnabled()) {
			this.logger.info("FrameworkServlet '" + getServletName() + "': initialization started");
		}
		long startTime = System.currentTimeMillis();

		try {
			/*
			初始化WebApplicationContext(是一个继承了ApplicationContext接口的接口)
             该属性的实例就是spring容器上下文
             FrameworkServlet的作用就是servlet与context关联
			*/
			this.webApplicationContext = initWebApplicationContext();
			//这是一个空方法留给子类重写,但是子类DispatchServlet没有重写
			initFrameworkServlet();
		}
		catch (ServletException | RuntimeException ex) {
			this.logger.error("Context initialization failed", ex);
			throw ex;
		}

		if (this.logger.isInfoEnabled()) {
			long elapsedTime = System.currentTimeMillis() - startTime;
			this.logger.info("FrameworkServlet '" + getServletName() + "': initialization completed in " +
					elapsedTime + " ms");
		}
	}
```




2.
`initWebApplicationContext()`方法的作用：初始化`WebApplicationContext`文并向servlet发布

```
/**
	 * Initialize and publish the WebApplicationContext for this servlet.
	 * <p>Delegates to {@link #createWebApplicationContext} for actual creation
	 * of the context. Can be overridden in subclasses.
	 * @return the WebApplicationContext instance
	 * @see #FrameworkServlet(WebApplicationContext)
	 * @see #setContextClass
	 * @see #setContextConfigLocation
	 */
	protected WebApplicationContext initWebApplicationContext() {
		//得到根上下文，所有context都有id和parent，构成了树型结构(似乎不是组合模式)
		//这里的根上下文是web.xml中配置的ContextLoaderListener监听器中
		//根据contextConfigLocation路径生成的上下文。
		WebApplicationContext rootContext =
				WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		WebApplicationContext wac = null;

		/*
		DispatchServlet有个以WebApplicationContext为参数的构造函数
		当DispatchServlet使用构造函数构造的时候执行if代码块
		*/
		if (this.webApplicationContext != null) {
			// A context instance was injected at construction time -> use it
			wac = this.webApplicationContext;
			if (wac instanceof ConfigurableWebApplicationContext) {
				ConfigurableWebApplicationContext cwac = (ConfigurableWebApplicationContext) wac;
				if (!cwac.isActive()) {
					// The context has not yet been refreshed -> provide services such as
					// setting the parent context, setting the application context id, etc
					if (cwac.getParent() == null) {
						// The context instance was injected without an explicit parent -> set
						// the root application context (if any; may be null) as the parent
						cwac.setParent(rootContext);
					}
					configureAndRefreshWebApplicationContext(cwac);
				}
			}
		}
		if (wac == null) {
			// No context instance was injected at construction time -> see if one
			// has been registered in the servlet context. If one exists, it is assumed
			// that the parent context (if any) has already been set and that the
			// user has performed any initialization such as setting the context id
			/*
             以contextAttribute属性(FrameworkServlet中String类型的属性)为key
             从ServletContext中获取WebApplicationContext
             一般不会设置contextAttribute属性，也就是说这里查找结果通常为null
             */
			wac = findWebApplicationContext();
		}
		if (wac == null) {
			// No context instance is defined for this servlet -> create a local one
			/*
			获取this的类名使用BeanUtils反射创建WebApplicationContext实例
			然后将rootContext设置父节点并注入一些必要属性：
                wac.setEnvironment(getEnvironment());
                wac.setParent(parent);
                String configLocation = getContextConfigLocation();
                if (configLocation != null) {
                    wac.setConfigLocation(configLocation);
                }
                configureAndRefreshWebApplicationContext(wac);
                return wac;
			*/
			wac = createWebApplicationContext(rootContext);
		}

		if (!this.refreshEventReceived) {
			// Either the context is not a ConfigurableApplicationContext with refresh
			// support or the context injected at construction time had already been
			// refreshed -> trigger initial onRefresh manually here.
			/*
			模板方法，WebApplicationContext创建成功之后会进行调用
			FrameworkServlet中是空方法，在DispatchServlet中真正实现
			*/
			onRefresh(wac);
		}

		/*
		将新创建的容器上下文设置到当前运行的ServletContext中
		*/
		if (this.publishContext) {
			// Publish the context as a servlet context attribute.
			String attrName = getServletContextAttributeName();
			/*
			getServletContext
			Returns a reference to the {@link ServletContext} in which this servlet
         	 is running
			*/
			getServletContext().setAttribute(attrName, wac);
			if (this.logger.isDebugEnabled()) {
				this.logger.debug("Published WebApplicationContext of servlet '" + getServletName() +
						"' as ServletContext attribute with name [" + attrName + "]");
			}
		}

		return wac;
	}
```



3.`ApplicationContext`有`getId`和`getParent`方法，这个是组合模式的实践吗？

```
public interface ApplicationContext extends EnvironmentCapable, ListableBeanFactory, HierarchicalBeanFactory,
		MessageSource, ApplicationEventPublisher, ResourcePatternResolver {
         String getId();
         ApplicationContext getParent();
         String getApplicationName();
         String getDisplayName();
         long getStartupDate();
         AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException;
	}
```

