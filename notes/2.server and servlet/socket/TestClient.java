package com.yufone.dmbd;

import com.yufone.dmbd.action.client.activity.Client;
import com.yufone.dmbd.action.client.activity.JaxbUtil;
import com.yufone.dmbd.action.client.activity.ReqBean;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author : Ftibw
 * @date : 2019/1/28 14:25
 */
public class TestClient {

    public static void main(String[] args) {
        final String host = "127.0.0.1";
        final int port = 8000;
        String url;
        String method;

        url = "/client/activity/validPhone";
        method = "POST";

        url = "/client/activity/getCurrentActivity";
        method = "GET";

//        url = "/client/activity/drawPrize";
//        method = "POST";
//
//        url = "/client/activity/getBaseUrl";
//        method = "GET";

        String tel = "15927544743";
        String aid = "5c4ee5fb16f49fb6d4c1e79f";
        String token = "45e70631c6544be9b145adeacd256ea6";
        String nonce = UUID.randomUUID().toString().replace("-", "");
        ReqBean rb = new ReqBean(url, method, tel, aid, token, nonce);
        final String req = JaxbUtil.convertToXml(rb);
        ExecutorService service = Executors.newFixedThreadPool(8);
        //用8个线程循环发500个请求
        for (int i = 0; i < 500; i++) {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println(new Client().sendRequest(host, port, req));
                }
            });
        }
    }
}
/*
####################################
/client/activity/getCurrentActivity
####################################
<?xml version="1.0"?>
<response>
    <code>1</code>
    <content>
        <activity>
            <id>5c4ee5fb16f49fb6d4c1e79f</id>
            <name>测试活动1</name>
            <joinNum>200</joinNum>
            <chance>0.8</chance>
            <type>1</type>
            <startTime>20190128</startTime>
            <endTime>20190129</endTime>
            <prizes>
                <prize>
                    <id>5c4ee5fb16f49fb6d4c1e7a2</id>
                    <name>保温杯</name>
                    <level>3</level>
                    <amount>30</amount>
                    <hitCount>0</hitCount>
                    <aid>5c4ee5fb16f49fb6d4c1e79f</aid>
                </prize>
                <prize>
                    <id>5c4ee5fb16f49fb6d4c1e7a1</id>
                    <name>纸巾</name>
                    <level>2</level>
                    <amount>80</amount>
                    <hitCount>0</hitCount>
                    <aid>5c4ee5fb16f49fb6d4c1e79f</aid>
                </prize>
                <prize>
                    <id>5c4ee5fb16f49fb6d4c1e7a0</id>
                    <name>百事可乐</name>
                    <level>1</level>
                    <amount>100</amount>
                    <hitCount>0</hitCount>
                    <aid>5c4ee5fb16f49fb6d4c1e79f</aid>
                </prize>
            </prizes>
        </activity>
        <records />
    </content>
</response>
*/
