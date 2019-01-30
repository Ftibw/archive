package com.yufone.dmbd.action.client.activity;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * @author : Ftibw
 * @date : 2019/1/28 14:26
 */
public class SocketUtils {

    public static final String TIMEOUT_RESPONSE = "<response><code>0</code><message>请求超时,请稍后重试</message></response>";

    public static final String INVALID_PARAMS_RESPONSE = "<response><code>0</code><message>参数格式错误,请重新发送</message></response>";

    private static final String PACKAGE_OUT_OF_SIZE_EXCEPTION = "数据包body大小超出上限131071个字节(127.999KB)";

    private static final int MAX_BODY_SIZE = 0x1ffff;//128KB-1B

    public static void close(Closeable... closeables) {
        for (Closeable c : closeables)
            if (null != c) {
                try {
                    c.close();
                } catch (IOException ignored) {
                }
            }
    }

    /**
     * 请求+响应模式,短连接,不用考虑粘包分包
     * 设置超时时间,revc网络字符串,UTF8编码
     *
     * @param socket            socket
     * @param bufferSize        用户读缓冲字节数组大小
     * @param readTimeoutMillis 一次读操作的超时毫秒值。
     *                          小于等于0时不设超时,此时设置readTimeoutLimit无效。
     *                          不设读超时,另一端socket如果一直不关流和连接,这边就一直阻塞
     * @param readTimeoutCount  读超时次数
     *                          readTimeoutMillis大于0时生效,readTimeoutCount小于-1或等于0时默认可读一次
     * @return requestString    返回请求字符串
     * @throws IOException
     */
    public static String revcRequest(Socket socket, int bufferSize, int readTimeoutMillis, int readTimeoutCount) throws IOException {
        int rtm = readTimeoutMillis;
        int rtc = readTimeoutCount;
        if (rtm <= 0) {
            rtm = 0;
            rtc = -1;
        } else if (rtc < -1 || rtc == 0) {
            rtc = 1;
        }
        socket.setSoTimeout(rtm);
        InputStream is = socket.getInputStream();

        byte[] buf = new byte[bufferSize];
        int len = 0;
        int total = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (rtc > 0 || rtc == -1) {
            try {
                len = is.read(buf);
            } catch (SocketTimeoutException ignored) {
                rtc--;
            }
            if (len == -1) {
                break;
            } else if (len > 0) {
                total += len;
                baos.write(buf, 0, len);
            } else {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {
                }
            }
        }
        if (total == 0) {
            return INVALID_PARAMS_RESPONSE;
        }
        //read and then decode
        byte[] reqBody = new byte[total];
        System.arraycopy(baos.toByteArray(), 0, reqBody, 0, total);
        return rtc > 0 || rtc == -1 ? new String(reqBody, StandardCharsets.UTF_8) : TIMEOUT_RESPONSE;
    }


    /**
     * 前3个字节, RRRSSSSS,RRSSSSSS,RRSSSSSS:其中R为保留位,S为数据位
     * 高3位+高2位+高2位为保留位(RRR,RR,RR)0~0x7f
     * 低5位+低6位+低6位为数据大小空间(SSSSS,SSSSSS,SSSSSS)0~0x1ffff
     *
     * @param bodySize 数据长度
     * @param reversed 保留位7bit,超出7位的bit舍弃
     * @return 包头3byte组成的组数
     */
    public static byte[] setPacketHeader(int bodySize, int reversed) {
        if (bodySize > MAX_BODY_SIZE) {
            throw new RuntimeException(PACKAGE_OUT_OF_SIZE_EXCEPTION);
        }
        reversed &= 0x7f;
        int r1 = (reversed & 0x70) << 1;
        int r2 = (reversed & 0x0c) << 4;
        int r3 = (reversed & 0x03) << 6;
        int first = (bodySize >> 12) | r1;
        int second = ((bodySize & 0xfff) >> 6) | r2;
        int third = (bodySize & 0x3f) | r3;
        return new byte[]{(byte) first, (byte) second, (byte) third};
    }

    /**
     * 获取包首部的保留位,数据位,转化为int值返回
     */
    public static int[] getPacketHeader(int first, int second, int third) {
        int s = (first & 0x1f) << 12 | (second & 0x3f) << 6 | (third & 0x3f);
        int r1 = first & 0xe0;
        int r2 = second & 0xc0;
        int r3 = third & 0xc0;
        int r = r1 >> 1 | r2 >> 4 | r3 >> 6;
        return new int[]{s, r};
    }

    public static void main(String[] args) {
        System.out.println("send before: bodySize     ---" + MAX_BODY_SIZE + " ....reversed---" + 0x7f);
        byte[] bs = setPacketHeader(MAX_BODY_SIZE, 0x7f);
        int[] hs = getPacketHeader(bs[0], bs[1], bs[2]);
        System.out.println("after received: bodySize  ---" + hs[0] + " ....reversed---" + hs[1]);
    }
}
