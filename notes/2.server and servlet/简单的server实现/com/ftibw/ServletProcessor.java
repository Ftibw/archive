package com.ftibw;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;

public class ServletProcessor {

    public void process(Request request, Response response) {

        String uri = request.getUri();
        String servletName = uri.substring(uri.lastIndexOf("/") + 1);
        URLClassLoader loader = null;

        try {
            // create a URLClassLoader
            URL[] urls = new URL[1];
            URLStreamHandler streamHandler = null;
            File classPath = new File(Constants.WEB_ROOT);
            // the forming of repository is taken from the createClassLoader method in
            // org.apache.catalina.startup.ClassLoaderFactory
            String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString();
            // the code for forming the URL is taken from the addRepository method in
            // org.apache.catalina.loader.StandardClassLoader class.
            System.err.println("classPath:" + repository.substring(repository.indexOf(':')));

            urls[0] = new URL(null, repository, streamHandler);
            loader = new URLClassLoader(urls);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
        Class myClass = null;
        try {
            myClass = loader.loadClass(getPackage() + "." + servletName);
        } catch (ClassNotFoundException e) {
            System.out.println(e.toString());
        }

        Servlet servlet = null;
        try {
            servlet = (Servlet) myClass.newInstance();
            ServletImpl impl = (ServletImpl) servlet;
            impl.setServletInfo(impl.getClass().getName());
            impl.setServletName(servletName);
            servlet.init(impl);
            servlet.service(request, response);
        } catch (Throwable e) {
            System.out.println(e.toString());
        }
    }


    public static String getClassPath() {
        String str = ServletProcessor.class.getResource("/").toString();
        return str.substring(str.indexOf('/') + 1, str.length() - 1);
    }

    public static String getPackage() {
        return ServletProcessor.class.getPackage().getName();
    }

    public static void main(String[] args) {
        System.out.println("classPath:" + getClassPath());
        System.out.println("package:" + getPackage());
    }
}
