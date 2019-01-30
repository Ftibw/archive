package com.ftibw;

import java.io.IOException;
import java.io.InputStream;

public class Request {
    private InputStream input;
    private String uri;

    public Request(InputStream input) {
        this.input = input;
    }

    public String getUri() {
        return uri;
    }

    /*
    GET /servlet.md HTTP/1.1
    Host: 192.168.3.104:8080
    User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64; rv:61.0) Gecko/20100101 Firefox/61.0
    Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*[这里什么也没有]/*;q=0.8
    Accept-Language: zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2
    Accept-Encoding: gzip, deflate
    Connection: keep-alive
    Upgrade-Insecure-Requests: 1
    */
    private String parseUri(String requestString) {
        //截取前两个' '之间的内容,即url
        int index1, index2;
        index1 = requestString.indexOf(' ');
        if (index1 != -1) {
            index2 = requestString.indexOf(' ', index1 + 1);
            if (index2 > index1)
                return requestString.substring(index1 + 1, index2);
        }
        return null;
    }

    public void parse() {
        // Read a set of characters from the socket
        StringBuilder request = new StringBuilder(2048);
        int i;
        byte[] buffer = new byte[2048];
        try {
            i = input.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            i = -1;
        }
        for (int j = 0; j < i; j++) {
            request.append((char) buffer[j]);
        }
        System.out.print(request.toString());
        uri = parseUri(request.toString());
    }


/*    public static void main(String args[]) throws IOException{
        ServerSocket server = new ServerSocket(2222);
        while(true){
            try{
                Socket client = server.accept();
                OutputStream out = client.getOutputStream();
                DataOutputStream outStream = new DataOutputStream(client.getOutputStream());
                outStream.write("Hello".getBytes());
                InputStream in = client.getInputStream();
                byte[] buf = new byte[1024];
                in.read(buf);
                in.close();
                System.out.println("request from client " + client.getInetAddress().getHostAddress());
                System.out.println(new String(buf));
                client.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }*/
}
