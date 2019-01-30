package com.yufone.dmbd.action.client.activity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {

    private static final int BUFFER_SIZE = 1024;

    private static final int READ_TIMEOUT_MILLIS = 5000;

    private static final int READ_TIMEOUT_LIMIT = 3;

    public String sendRequest(String host, int port, String req) {
        if (null == host || null == req) {
            throw new NullPointerException();
        }
        Socket client = null;
        OutputStream os = null;
        InputStream is = null;
        String respStr = null;
        try {
            client = new Socket(host, port);
            os = client.getOutputStream();
            is = client.getInputStream();
            os.write(req.getBytes(StandardCharsets.UTF_8));
            client.shutdownOutput();
            respStr = SocketUtils.revcRequest(client, BUFFER_SIZE, READ_TIMEOUT_MILLIS, READ_TIMEOUT_LIMIT);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            SocketUtils.close(is, os, client);
        }
        return respStr;
    }


}