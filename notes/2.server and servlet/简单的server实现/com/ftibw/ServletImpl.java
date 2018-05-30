package com.ftibw;

public class ServletImpl implements Servlet, ServletConfig {

    private String servletName;
    private String servletInfo;
    private ServletContext servletContext;

    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    public void setServletInfo(String servletInfo) {
        this.servletInfo = servletInfo;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void init(ServletConfig servletConfig) {

    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public void service(Request request, Response response) {

    }

    @Override
    public String getServletInfo() {
        return servletInfo;
    }

    @Override
    public void destroy() {

    }

    @Override
    public String getServletName() {
        return servletName;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(String paramName) {
        return null;
    }

    @Override
    public String[] getInitParameterNames() {
        return new String[0];
    }
}
