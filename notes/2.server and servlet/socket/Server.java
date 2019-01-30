package com.yufone.dmbd.action.client.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class Server implements Runnable {

    public interface Handler<P, R> {
        R handle(P request);
    }

    public static final int BUFFER_SIZE = 1024;

    public static final int PROCESSORS = Runtime.getRuntime().availableProcessors();

    private static final Logger LOGGER = LoggerFactory.getLogger("Server");

    private final int PORT;

    private final Handler<String, String> handler;

    private final ExecutorService SERVER_THREAD = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            final Server that = Server.this;
            Thread t = new Thread(r, "server@" + that.PORT);
            t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    LOGGER.info("accept()任务异常,重启任务中...", e);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {
                    }
                    that.SERVER_THREAD.execute(that);
                }
            });
            return t;
        }
    });

    //tomcat默认socket的backlog队列大小是100
    private static final LinkedBlockingQueue<Socket> CLIENT_SOCKET_QUEUE = new LinkedBlockingQueue<>(200);

    private static final ThreadPoolExecutor pool = new ThreadPoolExecutor(PROCESSORS / 2, PROCESSORS,
            100, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    private ServerSocket server;

    public Server(int port, Handler<String, String> handler) {
        this.PORT = port;
        this.handler = handler;
        try {
            this.server = new ServerSocket(port);
            LOGGER.info("socket服务启动成功,正在监听端口" + port);
        } catch (IOException e) {
            LOGGER.info("socket服务启动失败", e);
            SocketUtils.close(server);
        }
    }

    //这里run()是worker线程中调用的同步方法,
    //worker线程可以通过interrupt停止run()中的死循环任务
    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                try {
                    CLIENT_SOCKET_QUEUE.put(this.server.accept());
                } catch (InterruptedException e) {
                    LOGGER.info("CLIENT_SOCKET_QUEUE.put(client-socket) 异常", e);
                }
                pool.execute(new Runnable() {
                    @Override
                    public void run() {
                        int readTimeoutMillis = 3000;//读一次3秒超时
                        int readTimeoutLimit = 3;    //读超时三次,关闭socket
                        Socket client = null;
                        InputStream is = null;
                        OutputStream os = null;
                        try {
                            client = CLIENT_SOCKET_QUEUE.take();
                            is = client.getInputStream();
                            String reqStr = SocketUtils.revcRequest(client, BUFFER_SIZE, readTimeoutMillis, readTimeoutLimit);
                            String respStr = null;
                            if (null != reqStr) {
                                respStr = handler.handle(reqStr);
                            }
                            if (null != respStr) {
                                os = client.getOutputStream();
                                os.write(respStr.getBytes(StandardCharsets.UTF_8));
                            }
                        } catch (IOException e) {
                            LOGGER.info("IO处理任务异常", e);
                        } catch (InterruptedException e) {
                            LOGGER.info("CLIENT_SOCKET_QUEUE.take(client-socket) 异常", e);
                        } finally {
                            SocketUtils.close(is, os, client);
                        }
                    }
                });

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        this.SERVER_THREAD.execute(this);
    }
}