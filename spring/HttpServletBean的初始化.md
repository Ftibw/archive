##1.HttpServletBean的初始化

将配置参数注入到`servlet`的属性上，并初始化子类(`FrameworkServlet`中的初始化并绑定`context`）

---`这么多类都是为了DispatchServlet的初始化，将不同的初始化过程分解到了各个子类中`

```
/**
	 * Map config parameters onto bean properties of this servlet, and
	 * invoke subclass initialization.
	 * @throws ServletException if bean properties are invalid (or required
	 * properties are missing), or if subclass initialization fails.
	 */
	@Override
	public final void init() throws ServletException {
		if (logger.isDebugEnabled()) {
			logger.debug("Initializing servlet '" + getServletName() + "'");
		}

		// Set bean properties from init parameters.
		/*
		从配类ServletConfig中读取web.xml中DispatchServlet里的初始化参数
		(和学习Servlet的Filter的时候一样)
         将初始化参数封装到HttpServletBean的静态内部类ServletConfigPropertyValues中,
         返回给PropertyValues接口
		*/
		PropertyValues pvs = new ServletConfigPropertyValues(getServletConfig(), this.requiredProperties);
		if (!pvs.isEmpty()) {
			try {
				/*
				通过简单工厂调用方法return new BeanWrapperImpl(httpServletBean)
				返回包装类BeanWrapper,用于实现JavaBeans的专属操作
				*/
				BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(this);
				
				//获取资源加载器(提供classpath前缀以及类加载器)
				ResourceLoader resourceLoader = new ServletContextResourceLoader(getServletContext());
				
				//BeanWrapper注册属性编辑器(提供属性的类型转换功能)
				bw.registerCustomEditor(Resource.class, new ResourceEditor(resourceLoader, getEnvironment()));
				
				//initBeanWrapper(bw);空方法留给子类实现
				initBeanWrapper(bw);
				
				//设置DispatchServlet的属性
				bw.setPropertyValues(pvs, true);
			}
			catch (BeansException ex) {
				if (logger.isErrorEnabled()) {
					logger.error("Failed to set bean properties on servlet '" + getServletName() + "'", ex);
				}
				throw ex;
			}
		}
		// Let subclasses do whatever initialization they like.
		//initServletBean();空方法，在子类FrameworkServlet真正实现
		//用于初始化Context并绑定到Servlet上
		initServletBean();

		if (logger.isDebugEnabled()) {
			logger.debug("Servlet '" + getServletName() + "' configured successfully");
		}
	}
```

