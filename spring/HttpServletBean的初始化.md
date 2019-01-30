##1.HttpServletBean�ĳ�ʼ��

�����ò���ע�뵽`servlet`�������ϣ�����ʼ������(`FrameworkServlet`�еĳ�ʼ������`context`��

---`��ô���඼��Ϊ��DispatchServlet�ĳ�ʼ��������ͬ�ĳ�ʼ�����̷ֽ⵽�˸���������`

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
		������ServletConfig�ж�ȡweb.xml��DispatchServlet��ĳ�ʼ������
		(��ѧϰServlet��Filter��ʱ��һ��)
         ����ʼ��������װ��HttpServletBean�ľ�̬�ڲ���ServletConfigPropertyValues��,
         ���ظ�PropertyValues�ӿ�
		*/
		PropertyValues pvs = new ServletConfigPropertyValues(getServletConfig(), this.requiredProperties);
		if (!pvs.isEmpty()) {
			try {
				/*
				ͨ���򵥹������÷���return new BeanWrapperImpl(httpServletBean)
				���ذ�װ��BeanWrapper,����ʵ��JavaBeans��ר������
				*/
				BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(this);
				
				//��ȡ��Դ������(�ṩclasspathǰ׺�Լ��������)
				ResourceLoader resourceLoader = new ServletContextResourceLoader(getServletContext());
				
				//BeanWrapperע�����Ա༭��(�ṩ���Ե�����ת������)
				bw.registerCustomEditor(Resource.class, new ResourceEditor(resourceLoader, getEnvironment()));
				
				//initBeanWrapper(bw);�շ�����������ʵ��
				initBeanWrapper(bw);
				
				//����DispatchServlet������
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
		//initServletBean();�շ�����������FrameworkServlet����ʵ��
		//���ڳ�ʼ��Context���󶨵�Servlet��
		initServletBean();

		if (logger.isDebugEnabled()) {
			logger.debug("Servlet '" + getServletName() + "' configured successfully");
		}
	}
```

