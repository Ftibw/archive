package com.bjsxt.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ftp协议传输工具
 */
public class FtpUtils {

    /**
     * 通过ftp协议上传文件
     *
     * @param host     Nginx服务器所在主机的地址
     * @param port     Nginx服务器的端口号
     * @param username Nginx服务器的账户名
     * @param password Nginx服务器的账户密码
     * @param pathname 服务器端存储文件的目录
     * @param remote   服务器端文件的名称
     * @param filename 客户端文件的名称
     * @param dns      服务器所在域名
     * @return         服务器中的资源路径
     */
    public static String upload(
            String host,
            Integer port,
            String username,
            String password,
            String pathname,
            String remote,
            String filename,
            String dns
    ) {
        try (InputStream is = new FileInputStream(filename)) {

            //创建ftp客户端对象
            FTPClient client = new FTPClient();

            //建立和vsftpd服务的连接
            client.connect(host, port);

            //身份验证
            client.login(username, password);

            //切换工作目录
            client.changeWorkingDirectory(pathname);

            //设置文件上传的数据类型
            client.setFileType(FTP.BINARY_FILE_TYPE);

            //存储上传的文件
            client.storeFile(remote, is);

            return dns + remote;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
/*
	String host = "192.168.30.40";
	Integer port = 21;
	String username = "ftpuser";
	String password = "ftpuser";
	String pathname = "/home/ftpuser";
	String remote = "ftibw2.png";
	String filename = "E:/2.png";
	String dns = "http://www.ftibw.com/";
	FtpUtils.upload(host, port, username, password, pathname, remote, filename, dns);
*/
