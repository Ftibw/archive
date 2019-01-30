package com.ftibw;

import java.io.InputStream;
import java.net.URL;
import java.util.Set;

public interface ServletContext {

    String getContextPath();

    ServletContext getContext(String contextClass);

    String getMimeType(String fileName);

    Set<String> getResourcePaths(String dirUrl);

    URL getResource(String fileUrl);

    InputStream getResourceAsStream(String path);

    String getRealPath(String var1);

    String getServerInfo();

    String getInitParameter(String paramName);

    String[] getInitParameterNames();

    boolean setInitParameter(String key, String value);

    Object getAttribute(String key);

    String[] getAttributeNames();

    void setAttribute(String key, Object value);

    void removeAttribute(String key);

    String getServletContextName();

    <T extends Servlet> T createServlet(Class<T> clazz);

    ClassLoader getClassLoader();
}
