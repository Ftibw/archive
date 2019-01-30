package com.ftibw;

/**
 * 负责存储Servlet的配置信息
 */
public interface ServletConfig {
    /**
     * 获取Servlet类名
     *
     * @return ServletName
     */
    String getServletName();

    /**
     * 获取Servlet上下文
     *
     * @return ServletContext
     */
    ServletContext getServletContext();

    /**
     * 获取配置文件中指定名称参数的值
     *
     * @param paramName 参数名
     * @return 参数值
     */
    String getInitParameter(String paramName);

    /**
     * 获取配置文件中所有的参数名
     *
     * @return 配置文件中的参数名数组
     */
    String[] getInitParameterNames();

}
