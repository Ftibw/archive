package com.ftibw;

import org.omg.CORBA.portable.InvokeHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer implements Serializable {
    // shutdown command
    private static final String SHUTDOWN_COMMAND = "/SHUTDOWN";

    // the shutdown command received
    private boolean shutdown = false;

    public static void main(String[] args) {
        HttpServer server = new HttpServer();
        server.await();
    }

    public void await() {
        ServerSocket server = null;
        int port = 8080;
        try {
            //backlog指定了server中client请求队列的容量
            server = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Loop waiting for a request
        while (!shutdown) {
            Socket client = null;
            InputStream input = null;
            OutputStream output = null;

            try {
                client = server.accept();
                input = client.getInputStream();
                output = client.getOutputStream();
                // create Request object and parse
                Request request = new Request(input);
                request.parse();


                Response response = new Response(output);
                response.setRequest(request);
                // check if this is a request for a servlet or a static resource
                // a request for a servlet begins with "/servlet/"
                if (request.getUri().startsWith("/servlet/")) {
                    ServletProcessor processor = new ServletProcessor();
                    processor.process(request, response);
                } else {
                    StaticResourceProcessor processor = new StaticResourceProcessor();
                    processor.process(request, response);
                }

                // Close the socket
                output.close();
                input.close();
                client.close();
                //check if the previous URI is a shutdown command
                shutdown = request.getUri().equals(SHUTDOWN_COMMAND);


            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }


}
