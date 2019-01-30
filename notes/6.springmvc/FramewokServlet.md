1.

��д`HttpServletBean`��`initServletBean()`������`init()`��������bean������ע����ɺ���ø��߼���

��`servlet`��������`WebApplicationContext`

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
			��ʼ��WebApplicationContext(��һ���̳���ApplicationContext�ӿڵĽӿ�)
             �����Ե�ʵ������spring����������
             FrameworkServlet�����þ���servlet��context����
			*/
			this.webApplicationContext = initWebApplicationContext();
			//����һ���շ�������������д,��������DispatchServletû����д
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
`initWebApplicationContext()`���������ã���ʼ��`WebApplicationContext`�Ĳ���servlet����

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
		//�õ��������ģ�����context����id��parent�����������ͽṹ(�ƺ��������ģʽ)
		//����ĸ���������web.xml�����õ�ContextLoaderListener��������
		//����contextConfigLocation·�����ɵ������ġ�
		WebApplicationContext rootContext =
				WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		WebApplicationContext wac = null;

		/*
		DispatchServlet�и���WebApplicationContextΪ�����Ĺ��캯��
		��DispatchServletʹ�ù��캯�������ʱ��ִ��if�����
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
             ��contextAttribute����(FrameworkServlet��String���͵�����)Ϊkey
             ��ServletContext�л�ȡWebApplicationContext
             һ�㲻������contextAttribute���ԣ�Ҳ����˵������ҽ��ͨ��Ϊnull
             */
			wac = findWebApplicationContext();
		}
		if (wac == null) {
			// No context instance is defined for this servlet -> create a local one
			/*
			��ȡthis������ʹ��BeanUtils���䴴��WebApplicationContextʵ��
			Ȼ��rootContext���ø��ڵ㲢ע��һЩ��Ҫ���ԣ�
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
			ģ�巽����WebApplicationContext�����ɹ�֮�����е���
			FrameworkServlet���ǿշ�������DispatchServlet������ʵ��
			*/
			onRefresh(wac);
		}

		/*
		���´������������������õ���ǰ���е�ServletContext��
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



3.`ApplicationContext`��`getId`��`getParent`��������������ģʽ��ʵ����

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

