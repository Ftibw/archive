package com.ftibw;


/**
 * 负责接受网络请求,提供服务,响应结果
 */
public interface Servlet {
    /**
     * servlet容器的初始化方法，该方法只会被调用一次
     *
     * @param servletConfig Servlet的配置信息
     */
    void init(ServletConfig servletConfig);

    /**
     * 该方法用于取得<servlet> <init-param>配置的参数
     *
     * @return Servlet的配置信息
     */
    ServletConfig getServletConfig();

    /**
     * 对每一次网络请求提供服务并响应结果
     *
     * @param request  请求
     * @param response 响应
     */
    void service(Request request, Response response);

    /**
     * 该方法提供有关servlet的信息
     *
     * @return 作者、版本、版权等信息
     */
    String getServletInfo();

    /**
     * 当servlet实例要被移除时，destroy方法将被调用
     */
    void destroy();
}
