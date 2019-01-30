package com.ftibw;

import java.io.IOException;

public class TestServlet extends ServletImpl {

    @Override
    public void init(ServletConfig config) {
        System.out.println(config.getServletName());
        System.out.println(this.getServletInfo());
    }

    @Override
    public void service(Request request, Response response) {
        try {
            response.getWriter().println("test servlet+我擦");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
